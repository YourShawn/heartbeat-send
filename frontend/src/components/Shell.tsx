import { NavLink } from "react-router-dom";
import type { ReactNode } from "react";
import { useAuth } from "../auth";
import { useI18n } from "../i18n-context";

export function Shell({ children }: { children: ReactNode }) {
  const { copy, lang, setLang } = useI18n();
  const { profile, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <NavLink to="/" className="brand">
          <strong>{copy.brand}</strong>
          <span>{copy.brandEn}</span>
        </NavLink>
        <nav className="desktop-nav">
          <NavLink to="/" end>{copy.library}</NavLink>
          <NavLink to="/custom">{copy.custom}</NavLink>
          <NavLink to="/synth">{copy.synth}</NavLink>
          <NavLink to="/wearable">{copy.wearable}</NavLink>
        </nav>
        <div className="top-actions">
          <button className="lang-toggle" type="button" onClick={() => setLang(lang === "zh" ? "en" : "zh")}>
            {lang === "zh" ? <><b>中</b> / EN</> : <>中 / <b>EN</b></>}
          </button>
          {profile && (
            <button className="ghost" type="button" onClick={logout}>
              {profile.displayName} · {copy.signOut}
            </button>
          )}
        </div>
      </header>
      <p className="disclaimer">{copy.disclaimer}</p>
      <main className="main">{children}</main>
      <nav className="nav">
        <NavLink to="/" end>{copy.library}</NavLink>
        <NavLink to="/custom">{copy.custom}</NavLink>
        <NavLink to="/synth">{copy.synth}</NavLink>
        <NavLink to="/wearable">{copy.wearable}</NavLink>
      </nav>
    </div>
  );
}

export function OriginBadge({ tag, zh, en }: { tag: string; zh: string; en: string }) {
  const { lang } = useI18n();
  return <span className={`tag ${tag}`}>{lang === "zh" ? zh : en}</span>;
}
