import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useI18n } from "../i18n-context";
import type { WearableStatus } from "../types";

export function WearablePage() {
  const { copy, lang } = useI18n();
  const navigate = useNavigate();
  const [status, setStatus] = useState<WearableStatus | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api.wearableStatus().then(setStatus).catch((err: Error) => setError(err.message));
  }, []);

  async function capture() {
    setBusy(true);
    setError("");
    try {
      const saved = await api.createWearableMock({ durationSeconds: 30 });
      navigate(`/play/${saved.recordingId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : copy.error);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="card">
      <div className="hero">
        <h1>{copy.wearableTitle}</h1>
        <p>{copy.wearableBody}</p>
      </div>
      {status && (
        <p style={{ color: "var(--muted)" }}>
          {lang === "zh" ? status.messageZh : status.messageEn}
          <br />
          provider: {status.providerName} · {status.capturePath}
        </p>
      )}
      {error && <p className="error">{error}</p>}
      <p className="disclaimer">{copy.disclaimer}</p>
      <button className="primary" type="button" disabled={busy} onClick={() => void capture()}>
        {copy.mockCapture}
      </button>
    </div>
  );
}
