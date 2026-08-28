const MAX_STACK_LINES = 200;

const EMAIL = /[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}/g;
const WIN_HOME = /C:\\Users\\[^\\]+\\/gi;
const UNIX_HOME = /\/(?:home|Users)\/[^/\s]+\//g;
const TOKEN = /(?:api[_-]?key|token)\s*[:=]\s*\S+/gi;

export function sanitizeCrashText(text: string): string {
  let out = text.replace(EMAIL, "<redacted-email>");
  out = out.replace(WIN_HOME, "<redacted-home>");
  out = out.replace(UNIX_HOME, "<redacted-home>/");
  out = out.replace(TOKEN, "<redacted-secret>");
  return out.split("\n").slice(0, MAX_STACK_LINES).join("\n");
}

export function sanitizeCrashPayload(raw: { message: string; stack: string }): {
  message: string;
  stack: string;
} {
  return {
    message: sanitizeCrashText(raw.message),
    stack: sanitizeCrashText(raw.stack),
  };
}
