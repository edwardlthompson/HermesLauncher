import { getSaveCrashes } from "../feedback/saveCrashes";
import { sanitizeReportText } from "../privacy-report/sanitize";

export const SESSION_KEY = "gp.crash.pending";
export const LOCAL_KEY = "gp.crash.pending.local";

export type PendingCrash = {
  message: string;
  stack: string;
};

export function persistPendingCrash(record: PendingCrash, saveLocal = getSaveCrashes()): boolean {
  try {
    const payload = JSON.stringify({
      message: sanitizeReportText(record.message),
      stack: sanitizeReportText(record.stack, true),
    });
    sessionStorage.setItem(SESSION_KEY, payload);
    if (saveLocal) localStorage.setItem(LOCAL_KEY, payload);
    else localStorage.removeItem(LOCAL_KEY);
    return true;
  } catch {
    return false;
  }
}

export function readPendingCrash(): PendingCrash | null {
  try {
    const raw = sessionStorage.getItem(SESSION_KEY) ?? localStorage.getItem(LOCAL_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as PendingCrash;
    if (typeof parsed.message !== "string" || typeof parsed.stack !== "string") return null;
    return parsed;
  } catch {
    return null;
  }
}

export function clearPendingCrash(): void {
  try {
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem(LOCAL_KEY);
  } catch {
    /* ignore */
  }
}

export function onSaveCrashesChanged(on: boolean): void {
  if (!on) clearPendingCrash();
}
