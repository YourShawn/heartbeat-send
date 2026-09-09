import { useEffect, useState } from "react";
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

  useEffect(() => {
    let cancelled = false;
    const tag = filter === "ALL" ? undefined : filter;
    setLoading(true);
    setItems([]);
    api.list(tag)
      .then((data) => {
        if (cancelled) {
          return;
        }
        setItems(Array.isArray(data) ? data : []);
        setError("");
      })
      .catch((err: Error) => {
        if (cancelled) {
          return;
        }
        setItems([]);
        setError(err.message);
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [filter, location.key]);

  useEffect(() => {
    function onFocus() {
      const tag = filter === "ALL" ? undefined : filter;
      api.list(tag)
        .then((data) => {
          setItems(Array.isArray(data) ? data : []);
          setError("");
        })
        .catch((err: Error) => setError(err.message));
    }
    window.addEventListener("focus", onFocus);
    return () => window.removeEventListener("focus", onFocus);
  }, [filter]);

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
      {loading && <p style={{ color: "var(--muted)" }}>{copy.loading}</p>}
      {!loading && !items.length && !error && <p style={{ color: "var(--muted)" }}>{copy.empty}</p>}
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
