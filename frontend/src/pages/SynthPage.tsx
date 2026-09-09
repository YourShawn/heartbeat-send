import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useI18n } from "../i18n-context";

const SITUATIONS = ["REST", "WALKING", "EXERCISE", "ANXIOUS", "SLEEP", "TENDER", "CUSTOM"] as const;
const MOODS = ["CALM", "EXCITED", "STRESSED", "TENDER", "ENERGETIC"] as const;
const INTENSITIES = ["LOW", "MEDIUM", "HIGH"] as const;

export function SynthPage() {
  const { copy } = useI18n();
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [situation, setSituation] = useState<(typeof SITUATIONS)[number]>("REST");
  const [mood, setMood] = useState<(typeof MOODS)[number]>("CALM");
  const [intensity, setIntensity] = useState<(typeof INTENSITIES)[number]>("MEDIUM");
  const [duration, setDuration] = useState(40);
  const [note, setNote] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const saved = await api.createSynth({
        title: title || undefined,
        situationCode: situation,
        moodCode: mood,
        intensityCode: intensity,
        durationSeconds: duration,
        note: note || undefined,
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
        <h1>{copy.synth}</h1>
        <p>{copy.synthBanner}</p>
      </div>
      <label className="field">
        <span>{copy.title}</span>
        <input value={title} onChange={(e) => setTitle(e.target.value)} maxLength={120} />
      </label>
      <label className="field">
        <span>{copy.situation}</span>
        <select value={situation} onChange={(e) => setSituation(e.target.value as typeof situation)}>
          {SITUATIONS.map((code) => <option key={code} value={code}>{copy.situations[code]}</option>)}
        </select>
      </label>
      <label className="field">
        <span>{copy.mood}</span>
        <select value={mood} onChange={(e) => setMood(e.target.value as typeof mood)}>
          {MOODS.map((code) => <option key={code} value={code}>{copy.moods[code]}</option>)}
        </select>
      </label>
      <label className="field">
        <span>{copy.intensity}</span>
        <select value={intensity} onChange={(e) => setIntensity(e.target.value as typeof intensity)}>
          {INTENSITIES.map((code) => <option key={code} value={code}>{copy.intensities[code]}</option>)}
        </select>
      </label>
      <label className="field">
        <span>{copy.duration}: {duration}</span>
        <input type="range" min={10} max={180} value={duration} onChange={(e) => setDuration(Number(e.target.value))} />
      </label>
      <label className="field">
        <span>{copy.note}</span>
        <textarea rows={3} value={note} onChange={(e) => setNote(e.target.value)} maxLength={500} />
      </label>
      {error && <p className="error">{error}</p>}
      <p className="disclaimer" style={{ margin: "16px 0" }}>{copy.disclaimer}</p>
      <button className="primary" type="submit" disabled={busy}>{copy.generate}</button>
    </form>
  );
}
