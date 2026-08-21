const KEY_LAST_CHECK = "gp.update.lastCheckAt";
const KEY_LAST_SEEN = "gp.update.lastSeenVersion";
const KEY_DISMISSED = "gp.update.dismissedVersion";

function readNumber(key: string): number | null {
  const raw = localStorage.getItem(key);
  if (!raw) return null;
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
}

export function loadUpdatePrefs() {
  return {
    lastCheckAt: readNumber(KEY_LAST_CHECK),
    lastSeenVersion: localStorage.getItem(KEY_LAST_SEEN),
    dismissedVersion: localStorage.getItem(KEY_DISMISSED),
  };
}

export function markUpdateChecked(now: number, dismissedVersion?: string | null): void {
  localStorage.setItem(KEY_LAST_CHECK, String(now));
  if (dismissedVersion) localStorage.setItem(KEY_DISMISSED, dismissedVersion);
}

export function markVersionSeen(version: string): void {
  localStorage.setItem(KEY_LAST_SEEN, version);
}
