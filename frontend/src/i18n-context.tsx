import { createContext, createElement, useContext, useMemo, useState, type ReactNode } from "react";
import { readLang, t, writeLang, type Copy, type Lang } from "./i18n";

interface I18nValue {
  lang: Lang;
  copy: Copy;
  setLang: (lang: Lang) => void;
}

const I18nContext = createContext<I18nValue | null>(null);

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>(readLang);
  const value = useMemo<I18nValue>(() => ({
    lang,
    copy: t(lang),
    setLang: (next) => {
      writeLang(next);
      setLangState(next);
      document.documentElement.lang = next === "zh" ? "zh-Hans" : "en";
    },
  }), [lang]);
  return createElement(I18nContext.Provider, { value }, children);
}

export function useI18n(): I18nValue {
  const ctx = useContext(I18nContext);
  if (!ctx) {
    throw new Error("useI18n outside provider");
  }
  return ctx;
}
