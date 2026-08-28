export const APP_VERSION = "0.1.0";
export const DONATE_URL = "https://github.com/sponsors";

export function aboutSummary(): string {
  return `golden-path ${APP_VERSION} donate ${DONATE_URL}`;
}

export function aboutPayload(): { version: string; donate: string; summary: string } {
  return { version: APP_VERSION, donate: DONATE_URL, summary: aboutSummary() };
}
