import { describe, expect, it, vi } from "vitest";
import { createFeedbackPanel } from "./FeedbackPanel";

vi.mock("../i18n", () => ({
  t: (key: string) => key,
}));

describe("createFeedbackPanel", () => {
  it("puts reporter text in textContent, not innerHTML", () => {
    const panel = createFeedbackPanel("bug", { onClose: () => {}, releaseRepo: "acme/app" });
    const area = panel.querySelector<HTMLTextAreaElement>("[data-testid=feedback-description]");
    const preview = panel.querySelector<HTMLPreElement>("[data-testid=feedback-preview]");
    expect(area && preview).toBeTruthy();
    area!.value = "<script>alert(1)</script> hello";
    area!.dispatchEvent(new Event("input"));
    expect(preview!.innerHTML.includes("<script>")).toBe(false);
    expect(preview!.textContent || "").toContain("hello");
  });
});
