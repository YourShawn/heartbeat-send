import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api";
import { OriginBadge } from "../components/Shell";
import { useI18n } from "../i18n-context";
import type { OriginTag, Recording } from "../types";

const FILTERS: Array<OriginTag | "ALL"> = ["ALL", "CUSTOM", "GENERATED", "MEASURED"];

export function LibraryPage() {
  const { copy, lang } = useI18n();
  const [filter, setFilter] = useState<OriginTag | "ALL">("ALL");
  const [items, setItems] = useState<Recording[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const tag = filter === "ALL" ? undefined : filter;
    api.list(tag).then(setItems).catch((err: Error) => setError(err.message));
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
      {!items.length && <p style={{ color: "var(--muted)" }}>{copy.empty}</p>}
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
