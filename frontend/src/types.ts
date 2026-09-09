export type OriginTag = "CUSTOM" | "GENERATED" | "MEASURED";
export type SourceKind = "CUSTOM" | "SYNTH" | "WEARABLE";
export type CaptureMode =
  | "USER_DEFINED"
  | "RULE_SYNTH"
  | "OPENAI_SYNTH"
  | "WEARABLE_MOCK"
  | "WEARABLE_LIVE";
export type TimbreCode = "SINE" | "HEART" | "DRUM" | "SOFT";

export interface CurvePoint {
  tSeconds: number;
  bpm: number;
}

export interface UserProfile {
  userId: number;
  username: string;
  displayName: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  profile: UserProfile;
}

export interface Recording {
  recordingId: number;
  title: string;
  originTag: OriginTag;
  originTagZh: string;
  originTagEn: string;
  sourceKind: SourceKind;
  captureMode: CaptureMode;
  sensorOrigin: boolean;
  nonSensorLabel: string | null;
  bpmNominal: number;
  timbreCode: TimbreCode;
  curve: CurvePoint[];
  durationSeconds: number;
  situationCode: string | null;
  moodCode: string | null;
  intensityCode: string | null;
  questionnaireNote: string | null;
  synthModel: string | null;
  shareEnabled: boolean;
  shareToken: string | null;
  shareUrl: string | null;
  createdAt: string;
}

export interface ShareResponse {
  shareEnabled: boolean;
  shareToken: string | null;
  shareUrl: string | null;
}

export interface WearableStatus {
  liveDeviceConnected: boolean;
  providerName: string;
  capturePath: string;
  messageZh: string;
  messageEn: string;
}
