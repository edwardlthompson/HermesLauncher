import { describe, expect, it } from "vitest";
import { fingerprintCrash } from "./fingerprint";
import { buildReportMarkdown } from "./markdown";
import { sanitizeReportText } from "./sanitize";

const JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.signaturepart";
const STACK = [
  "TypeError: boom",
  "    at C:\\Users\\Ada\\secret.env:1",
  "token=ghp_abcdefghijklmnopqrstuvwxyz012345",
  JWT,
  "AKIAIOSFODNN7EXAMPLE",
].join("\n");

describe("sanitizeReportText", () => {
  it("treats null as empty", () => {
    expect(sanitizeReportText(null)).toBe("");
  });

  it("redacts secrets, JWT, AWS, and home paths", () => {
    const out = sanitizeReportText(STACK, true);
    expect(out).not.toContain("Ada");
    expect(out).not.toContain("ghp_");
    expect(out).not.toContain("eyJ");
    expect(out).not.toContain("AKIA");
    expect(out).toContain("<redacted-secret>");
    expect(out).toContain("<redacted-home>");
  });
});

describe("fingerprintCrash", () => {
  it("is stable when only the username changes", async () => {
    const a = await fingerprintCrash("Error\n    at C:\\Users\\Ada\\app\\main.ts:1");
    const b = await fingerprintCrash("Error\n    at C:\\Users\\Bob\\app\\main.ts:1");
    expect(a).toBe(b);
    expect(a).toHaveLength(12);
  });
});

describe("buildReportMarkdown", () => {
  it("strips tokens from description", () => {
    const md = buildReportMarkdown({
      kind: "crash",
      description: "user ghp_abcdefghijklmnopqrstuvwxyz012345 leaked",
    });
    expect(md).not.toContain("ghp_");
    expect(md).toContain("crash");
  });
});
