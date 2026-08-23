import { describe, expect, it } from "vitest";
import { canSubmitFeedback, feedbackPreviewText } from "./preview";

describe("feedbackPreviewText", () => {
  it("never keeps a token in the preview string", () => {
    const text = feedbackPreviewText({
      kind: "bug",
      description: "<img src=x onerror=alert(1)> ghp_abcdefghijklmnopqrstuvwxyz012345",
    });
    expect(text).not.toContain("ghp_");
    expect(text).toContain("<redacted-secret>");
  });

  it("requires description or stack", () => {
    expect(canSubmitFeedback("")).toBe(false);
    expect(canSubmitFeedback("  ")).toBe(false);
    expect(canSubmitFeedback("steps")).toBe(true);
  });
});
