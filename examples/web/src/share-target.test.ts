import { describe, expect, it } from "vitest";

import { shareTargetDescription } from "./share-target";

describe("shareTargetDescription", () => {
  it("returns empty when share params are missing", () => {
    expect(shareTargetDescription(new URLSearchParams())).toBe("");
  });

  it("joins title, text, and url", () => {
    const params = new URLSearchParams({
      title: "Clip",
      text: "hello",
      url: "https://example.com",
    });
    expect(shareTargetDescription(params)).toBe("Clip\nhello\nhttps://example.com");
  });
});
