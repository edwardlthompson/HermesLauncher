const MAX_STACK_LINES = 200;

const EMAIL = /[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}/g;
const WIN_HOME = /C:\\Users\\[^\\]+\\/gi;
const UNIX_HOME = /\/(?:home|Users)\/[^/\s]+\//g;
const TOKEN = /(?:api[_-]?key|token)\s*[:=]\s*\S+/gi;
const INJECT = /(?:ignore\s+(?:all\s+)?previous\s+instructions|you\s+are\s+now|<<SYS>>|\[INST\])/gi;

export function sanitizeCrashText(text: string): string {
  let out = text.replace(EMAIL, "<redacted-email>");
  out = out.replace(WIN_HOME, "<redacted-home>");
  out = out.replace(UNIX_HOME, "<redacted-home>/");
  out = out.replace(TOKEN, "<redacted-secret>");
  out = out.replace(INJECT, "<redacted-injection>");
  return out.split("\n").slice(0, MAX_STACK_LINES).join("\n");
}

export function sanitizeCrashPayload(raw: Record<string, unknown>): {
  message: string;
  stack: string;
} {
  return {
    message: sanitizeCrashText(String(raw.message ?? "")),
    stack: sanitizeCrashText(String(raw.stack ?? "")),
  };
}
