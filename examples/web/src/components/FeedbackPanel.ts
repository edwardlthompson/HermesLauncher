import { clearPendingCrash, readPendingCrash } from "../crash-capture/pendingCrash";
import { canSubmitFeedback, feedbackPreviewText } from "../feedback/preview";
import { buildIssueFormUrl } from "../github-feedback/issueFormUrl";
import { t } from "../i18n";
import { sanitizeReportText } from "../privacy-report/sanitize";

export type FeedbackKind = "bug" | "feature";

export type FeedbackPanelCallbacks = {
  onClose: () => void;
  releaseRepo: string;
  description?: string;
  stack?: string;
  fingerprint?: string;
  osFamily?: string;
};

export function createFeedbackPanel(
  kind: FeedbackKind,
  callbacks: FeedbackPanelCallbacks,
): HTMLElement {
  const pending = readPendingCrash();
  const stack = callbacks.stack ?? pending?.stack;
  const panel = document.createElement("section");
  panel.className = "gp-feedback-panel";
  panel.dataset.testid = "feedback-panel";
  panel.setAttribute(
    "aria-label",
    t(kind === "bug" ? "feedback.bug.title" : "feedback.feature.title"),
  );

  const title = document.createElement("h2");
  title.textContent = t(kind === "bug" ? "feedback.bug.title" : "feedback.feature.title");

  const hint = document.createElement("p");
  hint.textContent = t("feedback.clipboard.hint");

  const area = document.createElement("textarea");
  area.dataset.testid = "feedback-description";
  area.setAttribute("aria-label", t("feedback.description"));
  if (callbacks.description) {
    area.value = callbacks.description;
  }

  const preview = document.createElement("pre");
  preview.dataset.testid = "feedback-preview";
  preview.setAttribute("aria-live", "polite");

  const openBtn = document.createElement("button");
  openBtn.type = "button";
  openBtn.dataset.testid = "feedback-open";
  openBtn.textContent = t("feedback.open");
  openBtn.disabled = true;

  const copyBtn = document.createElement("button");
  copyBtn.type = "button";
  copyBtn.textContent = t("feedback.copy");

  const discardBtn = document.createElement("button");
  discardBtn.type = "button";
  discardBtn.textContent = t("feedback.discard");

  const offline = document.createElement("p");
  offline.hidden = navigator.onLine;
  offline.textContent = t("feedback.offline");

  function markdown(): string {
    return feedbackPreviewText({
      kind,
      description: area.value,
      stack,
      fingerprint: callbacks.fingerprint,
      appVersion: __APP_VERSION__,
      osFamily: callbacks.osFamily,
    });
  }

  function refresh(): void {
    preview.textContent = markdown();
    const online = navigator.onLine;
    offline.hidden = online;
    openBtn.disabled = !canSubmitFeedback(area.value, stack) || !online;
  }

  area.addEventListener("input", refresh);
  copyBtn.addEventListener("click", () => {
    void navigator.clipboard?.writeText(markdown());
  });
  openBtn.addEventListener("click", () => {
    const md = markdown();
    const template = kind === "bug" ? "bug_report.yml" : "product_idea.yml";
    const fields =
      kind === "bug"
        ? { description: sanitizeReportText(area.value), reproduction: md, title: "[bug]: " }
        : { problem: sanitizeReportText(area.value), solution: md, title: "[feat]: " };
    const built = buildIssueFormUrl(callbacks.releaseRepo, template, fields);
    if (!built.url) return;
    if (built.bodyTooLarge && built.clipboardMarkdown) {
      void navigator.clipboard?.writeText(built.clipboardMarkdown);
    }
    window.open(built.url, "_blank", "noopener,noreferrer");
  });
  discardBtn.addEventListener("click", () => {
    void navigator.clipboard?.writeText("");
    clearPendingCrash();
    callbacks.onClose();
  });

  refresh();
  panel.append(title, hint, area, preview, offline, copyBtn, openBtn, discardBtn);
  return panel;
}
