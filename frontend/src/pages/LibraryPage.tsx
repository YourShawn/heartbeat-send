import { useCallback, useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { api } from "../api";
import { OriginBadge } from "../components/Shell";
import { useI18n } from "../i18n-context";
import type { OriginTag, Recording } from "../types";

const FILTERS: Array<OriginTag | "ALL"> = ["ALL", "CUSTOM", "GENERATED", "MEASURED"];

export function LibraryPage() {
  const { copy, lang } = useI18n();
  const location = useLocation();
  const [filter, setFilter] = useState<OriginTag | "ALL">("ALL");
  const [items, setItems] = useState<Recording[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const load = useCallback(async (origin: OriginTag | "ALL") => {
    setLoading(true);
    setError("");
    try {
      const tag = origin === "ALL" ? undefined : origin;
      const rows = await api.list(tag);
      setItems(Array.isArray(rows) ? rows : []);
    } catch (err) {
      setItems([]);
      setError(err instanceof Error ? err.message : copy.error);
    } finally {
      setLoading(false);
    }
  }, [copy.error]);

  useEffect(() => {
    void load(filter);
  }, [filter, load, location.key]);

  useEffect(() => {
    const onFocus = () => {
      void load(filter);
    };
    window.addEventListener("focus", onFocus);
    return () => window.removeEventListener("focus", onFocus);
  }, [filter, load]);

  return (
    <div>
      <div className="hero">
        <h1>{copy.library}</h1>
        <p>{copy.tagline}</p>
      </div>
      <div className="filters">
        {FILTERS.map((key) => (
          <button
            key={key}
            className={`chip ${filter === key ? "on" : ""}`}
            type="button"
            onClick={() => setFilter(key)}
          >
            {key === "ALL" ? copy.all : key === "CUSTOM" ? copy.custom : key === "GENERATED" ? copy.synth : copy.wearable}
          </button>
        ))}
      </div>
      {error && <p className="error">{error}</p>}
      {loading && !items.length ? <p style={{ color: "var(--muted)" }}>{copy.loading}</p> : null}
      {!loading && !items.length && !error ? <p style={{ color: "var(--muted)" }}>{copy.empty}</p> : null}
      <div className="grid cards">
        {items.map((item) => (
          <Link key={item.recordingId} to={`/play/${item.recordingId}`} className="card pulse-card" style={{ textDecoration: "none" }}>
            <OriginBadge tag={item.originTag} zh={item.originTagZh} en={item.originTagEn} />
            <h3>{item.title}</h3>
            <p>{item.bpmNominal} BPM · {item.durationSeconds}s · {copy.timbres[item.timbreCode]}</p>
            <div className="meta">
              {!item.sensorOrigin && <span className="warn-tag">{copy.notSensor}</span>}
              <span className="tag CUSTOM">{lang === "zh" ? "本机播放" : "Local play"}</span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
