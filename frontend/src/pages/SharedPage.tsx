import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api";
import { Player } from "../components/Player";
import { useI18n } from "../i18n-context";
import type { Recording } from "../types";

export function SharedPage() {
  const { token } = useParams();
  const { copy } = useI18n();
  const [recording, setRecording] = useState<Recording | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) {
      return;
    }
    api.getPublic(token).then(setRecording).catch((err: Error) => setError(err.message));
  }, [token]);

  if (error) {
    return (
      <div className="login-wrap">
        <div className="card login-card">
          <p className="error">{error}</p>
        </div>
      </div>
    );
  }
  if (!recording) {
    return <p style={{ padding: 24 }}>{copy.loading}</p>;
  }

  return (
    <div className="login-wrap">
      <div className="card login-card" style={{ width: minWidth() }}>
        <p style={{ color: "var(--muted)" }}>{copy.sharePage}</p>
        <Player recording={recording} />
        <p className="disclaimer">{copy.disclaimer}</p>
      </div>
    </div>
  );
}

function minWidth(): string {
  return "min(640px, 100%)";
}
