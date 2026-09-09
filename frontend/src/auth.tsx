import { createContext, createElement, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api, getToken, setToken } from "./api";
import type { UserProfile } from "./types";

interface AuthValue {
  token: string | null;
  profile: UserProfile | null;
  ready: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const existing = getToken();
    if (!existing) {
      setReady(true);
      return;
    }
    api.me()
      .then((me) => setProfile(me))
      .catch(() => {
        setToken(null);
        setTokenState(null);
      })
      .finally(() => setReady(true));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const result = await api.login(username, password);
    setToken(result.accessToken);
    setTokenState(result.accessToken);
    setProfile(result.profile);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setTokenState(null);
    setProfile(null);
  }, []);

  const value = useMemo<AuthValue>(
    () => ({ token, profile, ready, login, logout }),
    [token, profile, ready, login, logout],
  );

  return createElement(AuthContext.Provider, { value }, children);
}

export function useAuth(): AuthValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth outside provider");
  }
  return ctx;
}
