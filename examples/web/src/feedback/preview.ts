import { buildReportMarkdown } from "../privacy-report/markdown";
import { sanitizeReportText } from "../privacy-report/sanitize";

/** Escaped preview string — callers must use textContent, never innerHTML. */
export function feedbackPreviewText(input: {
  kind: string;
  description?: string | null;
  stack?: string | null;
  fingerprint?: string | null;
  appVersion?: string | null;
  osFamily?: string | null;
}): string {
  return buildReportMarkdown({
    kind: input.kind,
    description: sanitizeReportText(input.description),
    stack: input.stack,
    fingerprint: input.fingerprint,
    appVersion: input.appVersion,
    osFamily: input.osFamily,
  });
}

export function canSubmitFeedback(description: string, stack?: string | null): boolean {
  return Boolean(sanitizeReportText(description).trim() || sanitizeReportText(stack, true).trim());
}
