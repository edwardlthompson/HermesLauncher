import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { assetPrefixOf, handleRestartGuard, loadAppUpdateConfig } from "./aboutSession";

describe("assetPrefixOf", () => {
  it("defaults to Golden-Path", () => {
    expect(assetPrefixOf(null)).toBe("Golden-Path");
    expect(assetPrefixOf({ release_repo: "a/b", check_interval: "daily" })).toBe("Golden-Path");
    expect(
      assetPrefixOf({
        release_repo: "a/b",
        check_interval: "daily",
        product_asset_prefix: "App-Name",
      }),
    ).toBe("App-Name");
  });
});

describe("loadAppUpdateConfig", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("parses a successful response", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        json: async () => ({ release_repo: "acme/app", product_asset_prefix: "App-Name" }),
      }),
    );
    const config = await loadAppUpdateConfig();
    expect(config?.release_repo).toBe("acme/app");
    expect(assetPrefixOf(config)).toBe("App-Name");
  });

  it("returns null when the response is not ok", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false }));
    await expect(loadAppUpdateConfig()).resolves.toBeNull();
  });

  it("returns null when fetch throws", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
    await expect(loadAppUpdateConfig()).resolves.toBeNull();
  });
});

describe("handleRestartGuard", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("clears pending restart guard and reports handled", () => {
    localStorage.setItem("gp-update-restart-pending", "true");
    expect(handleRestartGuard()).toBe(true);
    expect(localStorage.getItem("gp-update-restart-pending")).toBeNull();
  });

  it("returns false when no restart is pending", () => {
    expect(handleRestartGuard()).toBe(false);
  });
});
