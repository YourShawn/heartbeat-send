import type { CurvePoint, Recording, TimbreCode } from "../types";

function interpolateBpm(curve: CurvePoint[], t: number, fallback: number): number {
  if (!curve.length) {
    return fallback;
  }
  const sorted = [...curve].sort((a, b) => a.tSeconds - b.tSeconds);
  if (t <= sorted[0].tSeconds) {
    return sorted[0].bpm;
  }
  const last = sorted[sorted.length - 1];
  if (t >= last.tSeconds) {
    return last.bpm;
  }
  for (let i = 0; i < sorted.length - 1; i += 1) {
    const a = sorted[i];
    const b = sorted[i + 1];
    if (t >= a.tSeconds && t <= b.tSeconds) {
      const p = (t - a.tSeconds) / Math.max(0.0001, b.tSeconds - a.tSeconds);
      return a.bpm + (b.bpm - a.bpm) * p;
    }
  }
  return fallback;
}

function makeNoiseBuffer(ctx: AudioContext): AudioBuffer {
  const buffer = ctx.createBuffer(1, ctx.sampleRate * 0.2, ctx.sampleRate);
  const data = buffer.getChannelData(0);
  for (let i = 0; i < data.length; i += 1) {
    data[i] = Math.random() * 2 - 1;
  }
  return buffer;
}

function thump(
  ctx: AudioContext,
  dest: GainNode,
  time: number,
  timbre: TimbreCode,
  noise: AudioBuffer,
  isDub: boolean,
): void {
  const freq = isDub ? 48 : 72;
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.type = timbre === "SINE" ? "sine" : "triangle";
  osc.frequency.setValueAtTime(timbre === "DRUM" ? freq * 0.8 : freq, time);
  osc.frequency.exponentialRampToValueAtTime(Math.max(28, freq * 0.45), time + 0.12);

  const peak = timbre === "SOFT" ? 0.18 : timbre === "SINE" ? 0.22 : 0.32;
  gain.gain.setValueAtTime(0.0001, time);
  gain.gain.exponentialRampToValueAtTime(peak, time + 0.012);
  gain.gain.exponentialRampToValueAtTime(0.0001, time + (timbre === "SOFT" ? 0.28 : 0.16));

  osc.connect(gain);
  gain.connect(dest);
  osc.start(time);
  osc.stop(time + 0.32);

  if (timbre === "HEART" || timbre === "DRUM") {
    const src = ctx.createBufferSource();
    src.buffer = noise;
    const ng = ctx.createGain();
    const filter = ctx.createBiquadFilter();
    filter.type = "lowpass";
    filter.frequency.value = timbre === "DRUM" ? 900 : 420;
    ng.gain.setValueAtTime(timbre === "DRUM" ? 0.12 : 0.06, time);
    ng.gain.exponentialRampToValueAtTime(0.0001, time + 0.08);
    src.connect(filter);
    filter.connect(ng);
    ng.connect(dest);
    src.start(time);
    src.stop(time + 0.1);
  }
}

export type BeatHandler = (bpm: number, elapsed: number) => void;

export class LocalHeartbeatPlayer {
  private ctx: AudioContext | null = null;
  private master: GainNode | null = null;
  private timer = 0;
  private startedAt = 0;
  private nextBeat = 0;
  private noise: AudioBuffer | null = null;
  private running = false;

  get isRunning(): boolean {
    return this.running;
  }

  async start(recording: Recording, onBeat: BeatHandler): Promise<void> {
    this.stop();
    const ctx = new AudioContext();
    await ctx.resume();
    const master = ctx.createGain();
    master.gain.value = 0.9;
    master.connect(ctx.destination);
    this.ctx = ctx;
    this.master = master;
    this.noise = makeNoiseBuffer(ctx);
    this.running = true;
    this.startedAt = ctx.currentTime;
    this.nextBeat = ctx.currentTime + 0.05;
    const tick = () => {
      if (!this.ctx || !this.master || !this.noise || !this.running) {
        return;
      }
      const now = this.ctx.currentTime;
      const elapsed = now - this.startedAt;
      if (elapsed >= recording.durationSeconds) {
        this.stop();
        onBeat(recording.bpmNominal, recording.durationSeconds);
        return;
      }
      while (this.nextBeat < now + 0.12) {
        const t = this.nextBeat - this.startedAt;
        const bpm = interpolateBpm(recording.curve, t, recording.bpmNominal);
        thump(this.ctx, this.master, this.nextBeat, recording.timbreCode, this.noise, false);
        thump(this.ctx, this.master, this.nextBeat + 0.18, recording.timbreCode, this.noise, true);
        onBeat(bpm, t);
        this.nextBeat += 60 / Math.max(40, bpm);
      }
      this.timer = window.setTimeout(tick, 40);
    };
    tick();
  }

  stop(): void {
    this.running = false;
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = 0;
    }
    if (this.ctx) {
      void this.ctx.close();
      this.ctx = null;
    }
    this.master = null;
  }
}

export function bpmAt(recording: Recording, t: number): number {
  return interpolateBpm(recording.curve, t, recording.bpmNominal);
}
