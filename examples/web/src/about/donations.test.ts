import { describe, expect, it, vi } from "vitest";
import {
  DEFAULT_VENMO_URL,
  loadDonations,
  normalizeDonations,
  primaryDonateUrl,
} from "./donations";

describe("normalizeDonations", () => {
  it("returns disabled config for invalid input", () => {
    expect(normalizeDonations(null)).toEqual({ enabled: false, message: "", links: [] });
  });

  it("enables when links are valid", () => {
    const result = normalizeDonations({
      enabled: true,
      message: "Support us",
      links: [{ label: "GitHub", url: "https://example.com" }],
    });
    expect(result.enabled).toBe(true);
    expect(result.links).toHaveLength(1);
  });

  it("disables when links array empty", () => {
    const result = normalizeDonations({ enabled: true, message: "x", links: [] });
    expect(result.enabled).toBe(false);
  });

  it("filters invalid links", () => {
    const result = normalizeDonations({
      enabled: true,
      message: "",
      links: [{ label: "OK", url: "https://a.test" }, { label: 1, url: "bad" } as never],
    });
    expect(result.links).toHaveLength(1);
  });

  it("loadDonations keeps quiet Venmo when fetch fails", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
    const result = await loadDonations();
    expect(result.enabled).toBe(true);
    expect(result.links[0]?.url).toBe(DEFAULT_VENMO_URL);
    vi.unstubAllGlobals();
  });

  it("loadDonations parses successful response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({
          enabled: true,
          message: "hi",
          links: [{ label: "A", url: "https://a" }],
        }),
      }),
    );
    const result = await loadDonations("/donations.json");
    expect(result.enabled).toBe(true);
    vi.unstubAllGlobals();
  });

  it("primaryDonateUrl uses first link or Venmo default", () => {
    expect(primaryDonateUrl({ enabled: true, message: "", links: [] })).toBe(DEFAULT_VENMO_URL);
    expect(
      primaryDonateUrl({
        enabled: true,
        message: "",
        links: [
          { label: "Donate via Venmo", url: "https://venmo.com/code?user_id=1857304970395648420" },
        ],
      }),
    ).toBe("https://venmo.com/code?user_id=1857304970395648420");
  });

  it("loadDonations keeps quiet Venmo when response is not ok", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false }));
    const result = await loadDonations();
    expect(result.enabled).toBe(true);
    expect(result.links[0]?.url).toBe(DEFAULT_VENMO_URL);
    vi.unstubAllGlobals();
  });
});
