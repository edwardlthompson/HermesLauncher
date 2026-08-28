import { describe, expect, it } from "vitest";
import { fingerprintCrash } from "./fingerprint";
import { buildReportMarkdown } from "./markdown";
import { sanitizeReportText } from "./sanitize";
import fixture from "./sanitize-fixtures.json";

describe("sanitizeReportText", () => {
  it("treats null as empty", () => {
    expect(sanitizeReportText(null)).toBe("");
  });

  it("redacts secrets, JWT, AWS, and home paths", () => {
    const out = sanitizeReportText(fixture.stack, true);
    for (const leak of fixture.must_not_contain) {
      expect(out).not.toContain(leak);
    }
    for (const keep of fixture.must_contain) {
      expect(out).toContain(keep);
    }
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
