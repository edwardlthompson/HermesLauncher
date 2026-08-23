import { sanitizeReportText } from "./sanitize";

const KINDS = new Set(["crash", "bug", "feature"]);

export function buildReportMarkdown(input: {
  kind: string;
  description?: string | null;
  stack?: string | null;
  exceptionType?: string | null;
  fingerprint?: string | null;
  appVersion?: string | null;
  osFamily?: string | null;
}): string {
  const reportKind = KINDS.has(input.kind) ? input.kind : "bug";
  const desc = sanitizeReportText(input.description);
  const stackS = sanitizeReportText(input.stack, true);
  const parts = ["## What happened", desc || "(no description)", "", "## Kind", reportKind];
  if (input.fingerprint) {
    parts.push("", "## Fingerprint", `\`${sanitizeReportText(input.fingerprint)}\``);
  }
  if (input.exceptionType) {
    parts.push("", "## Exception", sanitizeReportText(input.exceptionType));
  }
  if (input.appVersion) {
    parts.push("", "## App version", sanitizeReportText(input.appVersion));
  }
  if (input.osFamily) {
    parts.push("", "## OS family", sanitizeReportText(input.osFamily));
  }
  if (stackS) {
    parts.push("", "## Stack", "```", stackS, "```");
  }
  return `${parts.join("\n").trim()}\n`;
}
