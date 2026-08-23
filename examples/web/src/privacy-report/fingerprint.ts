import { sanitizeReportText } from "./sanitize";

export async function fingerprintCrash(
  stack: string | null | undefined,
  exceptionType?: string | null,
): Promise<string> {
  const cleaned = sanitizeReportText(stack, true);
  const frames = cleaned
    .split("\n")
    .map((ln) => ln.trim())
    .filter(Boolean)
    .slice(0, 12);
  const kind = (exceptionType ?? guessType(cleaned) ?? "Error").trim();
  const payload = `${kind}\n${frames.join("\n")}`;
  const digest = await sha256Hex(payload);
  return digest.slice(0, 12);
}

function guessType(stack: string): string {
  const first = stack.split("\n")[0]?.trim() ?? "";
  const match = first.match(/^([A-Za-z][A-Za-z0-9_.$]+)/);
  return match?.[1] ?? "Error";
}

async function sha256Hex(text: string): Promise<string> {
  const buf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text));
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, "0")).join("");
}
