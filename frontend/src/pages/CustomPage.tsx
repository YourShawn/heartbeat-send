import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useI18n } from "../i18n-context";
import type { CurvePoint, TimbreCode } from "../types";

const TIMBRES: TimbreCode[] = ["HEART", "SOFT", "SINE", "DRUM"];

function syncFlatCurve(curve: CurvePoint[], bpm: number, duration: number): CurvePoint[] {
  const clamped = curve
    .map((p) => ({ tSeconds: Math.min(Math.max(0, p.tSeconds), duration), bpm: p.bpm }))
    .sort((a, b) => a.tSeconds - b.tSeconds);
  const flat = clamped.length > 0 && clamped.every((p) => p.bpm === clamped[0].bpm);
  const withBpm = flat ? clamped.map((p) => ({ ...p, bpm })) : clamped;
  if (!withBpm.length) {
    return [
      { tSeconds: 0, bpm },
      { tSeconds: duration, bpm },
    ];
  }
  const points = [...withBpm];
  if (points[0].tSeconds !== 0) {
    points.unshift({ tSeconds: 0, bpm: flat ? bpm : points[0].bpm });
  } else if (flat) {
    points[0] = { ...points[0], bpm };
  }
  const last = points[points.length - 1];
  if (last.tSeconds !== duration) {
    points.push({ tSeconds: duration, bpm: flat ? bpm : last.bpm });
  } else if (flat) {
    points[points.length - 1] = { ...last, bpm };
  }
  return points;
}

export function CustomPage() {
  const { copy } = useI18n();
  const navigate = useNavigate();
  const [title, setTitle] = useState("My pulse");
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
    setCurve((current) => current.map((p, i) => (i === index ? { ...p, ...patch } : p)));
  }

  function onBpmChange(next: number) {
    setBpm(next);
    setTitle((current) => current.replace(/(\d+)\s*BPM/i, `${next} BPM`));
    setCurve((current) => {
      const flat = current.length > 0 && current.every((p) => p.bpm === current[0].bpm);
      return flat ? current.map((p) => ({ ...p, bpm: next })) : current;
    });
  }

  function onDurationChange(next: number) {
    setDuration(next);
    setCurve((current) => syncFlatCurve(current, bpm, next));
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const synced = syncFlatCurve(curve, bpm, duration);
      let saveTitle = title.trim() || "My pulse";
      if (!/\bBPM\b/i.test(saveTitle)) {
        saveTitle = `${saveTitle} · ${bpm} BPM`;
      } else {
        saveTitle = saveTitle.replace(/(\d+)\s*BPM/i, `${bpm} BPM`);
      }
      const saved = await api.createCustom({
        title: saveTitle,
        bpmNominal: bpm,
        timbreCode: timbre,
        durationSeconds: duration,
        curve: synced,
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
        <input type="range" min={40} max={180} value={bpm} onChange={(e) => onBpmChange(Number(e.target.value))} />
      </label>
      <label className="field">
        <span>{copy.timbre}</span>
        <select value={timbre} onChange={(e) => setTimbre(e.target.value as TimbreCode)}>
          {TIMBRES.map((code) => <option key={code} value={code}>{copy.timbres[code]}</option>)}
        </select>
      </label>
      <label className="field">
        <span>{copy.duration}: {duration}</span>
        <input type="range" min={10} max={180} value={duration} onChange={(e) => onDurationChange(Number(e.target.value))} />
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
