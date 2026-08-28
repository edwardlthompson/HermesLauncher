import { describe, expect, it } from "vitest";

import { APP_VERSION, aboutPayload, aboutSummary } from "./about.js";

describe("about", () => {
  it("includes version and donate", () => {
    const text = aboutSummary();
    expect(text).toContain(APP_VERSION);
    expect(text).toContain("donate");
  });

  it("payload matches About fields", () => {
    const payload = aboutPayload();
    expect(payload.version).toBe(APP_VERSION);
    expect(payload.donate).toContain("http");
    expect(payload.summary).toBe(aboutSummary());
  });
});
