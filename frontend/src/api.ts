import type {
  LoginResponse,
  OriginTag,
  Recording,
  ShareResponse,
  UserProfile,
  WearableStatus,
} from "./types";

const TOKEN_KEY = "heartbeat.send.jwt";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

async function request<T>(path: string, init: RequestInit = {}, auth = true): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (auth) {
    const token = getToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }
  const response = await fetch(path, { ...init, headers });
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    const message = data?.message || response.statusText;
    throw new Error(message);
  }
  return data as T;
}

export const api = {
  login: (username: string, password: string) =>
    request<LoginResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    }, false),

  me: () => request<UserProfile>("/api/auth/me"),

  list: (originTag?: OriginTag) => {
    const q = originTag ? `?originTag=${originTag}` : "";
    return request<Recording[]>(`/api/heartbeats${q}`);
  },

  get: (id: number) => request<Recording>(`/api/heartbeats/${id}`),

  createCustom: (body: unknown) =>
    request<Recording>("/api/heartbeats/custom", { method: "POST", body: JSON.stringify(body) }),

  createSynth: (body: unknown) =>
    request<Recording>("/api/heartbeats/synth", { method: "POST", body: JSON.stringify(body) }),

  createWearableMock: (body: unknown) =>
    request<Recording>("/api/heartbeats/wearable-mock", { method: "POST", body: JSON.stringify(body) }),

  remove: (id: number) =>
    request<void>(`/api/heartbeats/${id}`, { method: "DELETE" }),

  enableShare: (id: number) =>
    request<ShareResponse>(`/api/heartbeats/${id}/share`, { method: "POST" }),

  disableShare: (id: number) =>
    request<ShareResponse>(`/api/heartbeats/${id}/share`, { method: "DELETE" }),

  getPublic: (token: string) =>
    request<Recording>(`/api/public/heartbeats/${token}`, {}, false),

  wearableStatus: () => request<WearableStatus>("/api/wearable/status"),
};
