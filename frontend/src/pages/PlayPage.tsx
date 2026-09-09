import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import { Player } from "../components/Player";
import { useI18n } from "../i18n-context";
import type { Recording } from "../types";

export function PlayPage() {
  const { id } = useParams();
  const { copy } = useI18n();
  const navigate = useNavigate();
  const [recording, setRecording] = useState<Recording | null>(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) {
      return;
    }
    api.get(Number(id)).then(setRecording).catch((err: Error) => setError(err.message));
  }, [id]);

  if (error) {
    return <p className="error">{error}</p>;
  }
  if (!recording) {
    return <p>{copy.loading}</p>;
  }

  const current = recording;

  async function share() {
    const result = current.shareEnabled
      ? await api.disableShare(current.recordingId)
      : await api.enableShare(current.recordingId);
    const next = await api.get(current.recordingId);
    setRecording(next);
    if (result.shareEnabled && result.shareToken) {
      const url = `${window.location.origin}/s/${result.shareToken}`;
      await navigator.clipboard.writeText(url);
      setMessage(copy.copied);
    } else {
      setMessage("");
    }
  }

  async function remove() {
    await api.remove(current.recordingId);
    navigate("/");
  }

  return (
    <div className="card">
      <Player
        recording={current}
        extra={(
          <>
            <button className="secondary" type="button" onClick={() => void share()}>
              {current.shareEnabled ? copy.unshare : copy.share}
            </button>
            <button className="danger" type="button" onClick={() => void remove()}>{copy.delete}</button>
          </>
        )}
      />
      {message && <p style={{ textAlign: "center", color: "var(--gold)" }}>{message}</p>}
    </div>
  );
}
