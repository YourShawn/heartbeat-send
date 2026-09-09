import type { ReactNode } from "react";
import { useEffect, useMemo, useRef, useState } from "react";
import { LocalHeartbeatPlayer } from "../audio/player";
import { useI18n } from "../i18n-context";
import type { Recording } from "../types";
import { OriginBadge } from "./Shell";

export function Player({
  recording,
  extra,
}: {
  recording: Recording;
  extra?: ReactNode;
}) {
  const { copy, lang } = useI18n();
  const player = useRef(new LocalHeartbeatPlayer());
  const [playing, setPlaying] = useState(false);
  const [bpm, setBpm] = useState(recording.bpmNominal);
  const [beat, setBeat] = useState(false);

  useEffect(() => () => player.current.stop(), []);

  useEffect(() => {
    setBpm(recording.bpmNominal);
    setPlaying(false);
  }, [recording]);

  const path = useMemo(() => curvePath(recording), [recording]);

  async function toggle() {
    if (playing) {
      player.current.stop();
      setPlaying(false);
      return;
    }
    setPlaying(true);
    await player.current.start(recording, (next) => {
      setBpm(Math.round(next));
      setBeat(true);
      window.setTimeout(() => setBeat(false), 180);
      if (!player.current.isRunning) {
        setPlaying(false);
      }
    });
  }

  return (
    <section className="player">
      <div className={`rings ${beat ? "beating" : ""}`}>
        <div className="bpm-read">
          {bpm}
          <small>BPM</small>
        </div>
      </div>
      <div>
        <h2 style={{ fontFamily: "var(--display)", margin: "0 0 8px" }}>{recording.title}</h2>
        <div className="meta" style={{ justifyContent: "center" }}>
          <OriginBadge tag={recording.originTag} zh={recording.originTagZh} en={recording.originTagEn} />
          {!recording.sensorOrigin && <span className="warn-tag">{copy.notSensor}</span>}
          <span className="warn-tag">{copy.medical}</span>
          <span className="tag CUSTOM">{copy.listenLocal}</span>
        </div>
        <p style={{ color: "var(--muted)", marginTop: 10 }}>
          {recording.bpmNominal} BPM · {copy.timbres[recording.timbreCode]} · {recording.durationSeconds}s · {copy.capture[recording.captureMode]}
        </p>
        {recording.nonSensorLabel && (
          <p style={{ color: "var(--gold)", fontSize: "0.88rem" }}>{recording.nonSensorLabel}</p>
        )}
      </div>
      <svg className="curve" viewBox="0 0 300 84" preserveAspectRatio="none" aria-hidden>
        <path d={path} fill="none" stroke="#e45b4a" strokeWidth="2.4" />
      </svg>
      <div className="row" style={{ justifyContent: "center" }}>
        <button className="primary" type="button" onClick={() => void toggle()}>
          {playing ? copy.pause : copy.play}
        </button>
        {extra}
      </div>
      <p style={{ color: "var(--muted)", fontSize: "0.8rem" }}>
        {lang === "zh"
          ? "声音在浏览器本地合成，服务器不保存音频文件。"
          : "Audio is synthesized in the browser. The server never stores an audio file."}
      </p>
    </section>
  );
}

function curvePath(recording: Recording): string {
  const pts = recording.curve.length
    ? recording.curve
    : [
        { tSeconds: 0, bpm: recording.bpmNominal },
        { tSeconds: recording.durationSeconds, bpm: recording.bpmNominal },
      ];
  const maxT = Math.max(recording.durationSeconds, ...pts.map((p) => p.tSeconds), 1);
  const minB = Math.min(...pts.map((p) => p.bpm), recording.bpmNominal) - 8;
  const maxB = Math.max(...pts.map((p) => p.bpm), recording.bpmNominal) + 8;
  return pts
    .map((p, i) => {
      const x = (p.tSeconds / maxT) * 300;
      const y = 76 - ((p.bpm - minB) / Math.max(1, maxB - minB)) * 64;
      return `${i === 0 ? "M" : "L"} ${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(" ");
}
