import { describe, expect, it } from "vitest";

import { sanitizeCrashPayload, sanitizeCrashText } from "./crash.js";

describe("sanitizeCrashText", () => {
  it("redacts email, home paths, and tokens", () => {
    const got = sanitizeCrashText(
      String.raw`user@example.com C:\Users\ada\secret token=abc /home/ada/.env`,
    );
    expect(got).not.toContain("user@example.com");
    expect(got).not.toContain(String.raw`Users\ada`);
    expect(got).toContain("<redacted-email>");
    expect(got).toContain("<redacted-home>");
    expect(got).toContain("<redacted-secret>");
  });

  it("sanitizes crash JSON payload fields", () => {
    const got = sanitizeCrashPayload({
      message: "boom user@example.com",
      stack: String.raw`at C:\Users\ada\x.ts`,
    });
    expect(got.message).toContain("<redacted-email>");
    expect(got.stack).toContain("<redacted-home>");
    expect(got).not.toHaveProperty("email");
  });
});
