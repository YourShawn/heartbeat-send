import { useState, type FormEvent } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../auth";
import { useI18n } from "../i18n-context";

export function LoginPage() {
  const { copy, lang, setLang } = useI18n();
  const { profile, login } = useAuth();
  const [username, setUsername] = useState("demo");
  const [password, setPassword] = useState("demo123");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (profile) {
    return <Navigate to="/" replace />;
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      await login(username, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : copy.error);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-wrap">
      <form className="card login-card" onSubmit={onSubmit}>
        <div className="row" style={{ justifyContent: "space-between" }}>
          <div className="brand">
            <strong>{copy.brand}</strong>
            <span>{copy.brandEn}</span>
          </div>
          <button className="lang-toggle" type="button" onClick={() => setLang(lang === "zh" ? "en" : "zh")}>
            {lang === "zh" ? <><b>中</b> / EN</> : <>中 / <b>EN</b></>}
          </button>
        </div>
        <div className="hero">
          <h1>{copy.loginTitle}</h1>
          <p>{copy.tagline}</p>
        </div>
        <p className="disclaimer" style={{ margin: "0 0 16px" }}>{copy.disclaimer}</p>
        <label className="field">
          <span>{copy.username}</span>
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        </label>
        <label className="field">
          <span>{copy.password}</span>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
        </label>
        {error && <p className="error">{error}</p>}
        <button className="primary" type="submit" disabled={busy}>{copy.signIn}</button>
        <p style={{ color: "var(--muted)", fontSize: "0.85rem" }}>{copy.loginHint}</p>
      </form>
    </div>
  );
}
