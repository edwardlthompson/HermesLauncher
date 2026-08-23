export const MAX_BODY_BYTES = 8192;
export const MAX_STACK_LINES = 200;

const PEM = /-----BEGIN [A-Z ]*PRIVATE KEY-----[\s\S]*?-----END [A-Z ]*PRIVATE KEY-----/g;
const GITHUB = /\b(?:ghp|gho|github_pat)_[A-Za-z0-9_]+/g;
const BEARER = /Bearer\s+\S+/gi;
const JWT = /\beyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/g;
const AWS = /\bAKIA[0-9A-Z]{16}\b/g;
const API = /(?:api[_-]?key|token)\s*[:=]\s*\S+/gi;
const EMAIL = /[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/g;
const WIN_HOME = /C:\\Users\\[^\\]+\\/gi;
const UNIX_HOME = /\/(?:home|Users)\/[^/\s]+\//g;
const UNC = /\\\\[^\\\s]+\\[^\\\s]+\\/g;
const IPV4 = /\b(?:\d{1,3}\.){3}\d{1,3}\b/g;
const IPV6 = /\b(?:[0-9a-f]{1,4}:){2,7}[0-9a-f]{1,4}\b/gi;
const URL_Q = /([?&])(token|key|code|access_token)=[^&\s]+/gi;

export function sanitizeReportText(text: string | null | undefined, stack = false): string {
  if (text == null) return "";
  let out = String(text);
  out = out.replace(PEM, "<redacted-secret>");
  out = out.replace(GITHUB, "<redacted-secret>");
  out = out.replace(BEARER, "<redacted-secret>");
  out = out.replace(JWT, "<redacted-secret>");
  out = out.replace(AWS, "<redacted-secret>");
  out = out.replace(API, "<redacted-secret>");
  out = out.replace(EMAIL, "<redacted-email>");
  out = out.replace(WIN_HOME, "<redacted-home>");
  out = out.replace(UNIX_HOME, "<redacted-home>/");
  out = out.replace(UNC, "<redacted-unc>");
  out = out.replace(IPV4, "<redacted-ip>");
  out = out.replace(IPV6, "<redacted-ip>");
  out = out.replace(URL_Q, "$1$2=<redacted-secret>");
  if (stack) {
    out = out.split("\n").slice(0, MAX_STACK_LINES).join("\n");
  }
  return capWholeLines(out);
}

function capWholeLines(text: string): string {
  const encoded = new TextEncoder().encode(text);
  if (encoded.length <= MAX_BODY_BYTES) return text;
  const kept: string[] = [];
  let size = 0;
  for (const line of text.split("\n")) {
    const add = new TextEncoder().encode(line).length + 1;
    if (size + add > MAX_BODY_BYTES) break;
    kept.push(line);
    size += add;
  }
  return kept.join("\n");
}
