import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useI18n } from "../i18n-context";
import type { CurvePoint, TimbreCode } from "../types";

const TIMBRES: TimbreCode[] = ["HEART", "SOFT", "SINE", "DRUM"];
const MIN_BPM = 40;
const MAX_BPM = 180;
const MIN_DURATION = 10;
const MAX_DURATION = 180;

export function CustomPage() {
  const { copy } = useI18n();
  const navigate = useNavigate();
  const [title, setTitle] = useState("My pulse · 72 BPM");
  const [bpm, setBpm] = useState(72);
  const [timbre, setTimbre] = useState<TimbreCode>("HEART");
  const [duration, setDuration] = useState(45);
  const [curve, setCurve] = useState<CurvePoint[]>([
    { tSeconds: 0, bpm: 72 },
    { tSeconds: 45, bpm: 72 },
  ]);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  function updatePoint(index: number, patch: Partial<CurvePoint>) {
    setCurve((current) => current.map((p, i) => {
      if (i !== index) {
        return p;
      }
      const next = { ...p, ...patch };
      return {
        tSeconds: clamp(next.tSeconds, 0, duration),
        bpm: Math.round(clamp(next.bpm, 40, 220)),
      };
    }));
  }

  function onBpmChange(next: number) {
    const value = Math.round(clamp(next, MIN_BPM, MAX_BPM));
    setBpm(value);
    setTitle((current) => syncTitleBpm(current, value));
    setCurve((current) => (isFlatCurve(current)
      ? current.map((p) => ({ ...p, bpm: value }))
      : current));
  }

  function onDurationChange(next: number) {
    const value = Math.round(clamp(next, MIN_DURATION, MAX_DURATION));
    setDuration(value);
    setCurve((current) => current.map((p, i) => ({
      ...p,
      tSeconds: i === current.length - 1
        ? value
        : clamp(p.tSeconds, 0, value),
    })));
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const saved = await api.createCustom({
        title: syncTitleBpm(title, bpm),
        bpmNominal: bpm,
        timbreCode: timbre,
        durationSeconds: clamp(duration, MIN_DURATION, MAX_DURATION),
        curve: isFlatCurve(curve) ? curve.map((p) => ({ ...p, bpm })) : curve,
      });
      navigate(`/play/${saved.recordingId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : copy.error);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="card" onSubmit={onSubmit}>
      <div className="hero">
        <h1>{copy.custom}</h1>
        <p>{copy.curveHint}</p>
      </div>
      <label className="field">
        <span>{copy.title}</span>
        <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={120} />
      </label>
      <label className="field">
        <span>{copy.bpm}: {bpm}</span>
        <input type="range" min={MIN_BPM} max={MAX_BPM} value={bpm} onChange={(e) => onBpmChange(Number(e.target.value))} />
      </label>
      <label className="field">
        <span>{copy.timbre}</span>
        <select value={timbre} onChange={(e) => setTimbre(e.target.value as TimbreCode)}>
          {TIMBRES.map((code) => <option key={code} value={code}>{copy.timbres[code]}</option>)}
        </select>
      </label>
      <label className="field">
        <span>{copy.duration}: {duration}</span>
        <input type="range" min={MIN_DURATION} max={MAX_DURATION} value={duration} onChange={(e) => onDurationChange(Number(e.target.value))} />
      </label>
      <div className="curve-editor">
        {curve.map((point, index) => (
          <div className="curve-row" key={index}>
            <input
              type="number"
              min={0}
              max={duration}
              step={0.5}
              value={point.tSeconds}
              onChange={(e) => updatePoint(index, { tSeconds: Number(e.target.value) })}
            />
            <input
              type="number"
              min={40}
              max={220}
              value={point.bpm}
              onChange={(e) => updatePoint(index, { bpm: Number(e.target.value) })}
            />
            <button className="ghost" type="button" onClick={() => setCurve((c) => c.filter((_, i) => i !== index))} disabled={curve.length <= 2}>
              ×
            </button>
          </div>
        ))}
        <button
          className="secondary"
          type="button"
          onClick={() => setCurve((c) => [...c, { tSeconds: duration, bpm }])}
        >
          {copy.addPoint}
        </button>
      </div>
      {error && <p className="error">{error}</p>}
      <p className="disclaimer" style={{ margin: "16px 0" }}>{copy.disclaimer}</p>
      <button className="primary" type="submit" disabled={busy}>{copy.save}</button>
    </form>
  );
}

function isFlatCurve(points: CurvePoint[]): boolean {
  if (points.length === 0) {
    return true;
  }
  return points.every((p) => p.bpm === points[0].bpm);
}

function syncTitleBpm(title: string, nextBpm: number): string {
  const token = `${nextBpm} BPM`;
  if (/\b\d{2,3}\s*BPM\b/i.test(title)) {
    return title.replace(/\b\d{2,3}\s*BPM\b/i, token);
  }
  const trimmed = title.trim();
  return trimmed ? `${trimmed} · ${token}` : token;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}
