import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { MS_DAY } from "./productUpdate";
import { decideLaunchPrompt } from "./runAppUpdates";
import { markUpdateChecked, markVersionSeen } from "./updatePrefs";

describe("decideLaunchPrompt", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("returns donate only after a version change and does not fetch", async () => {
    markVersionSeen("0.1.0");
    const fetchLatest = vi.fn();
    const prompt = await decideLaunchPrompt({
      currentVersion: "0.2.0",
      releaseRepo: "acme/app",
      userAgent: "GoldenPath/0.2.0",
      fetchLatest,
    });
    expect(prompt).toEqual({ kind: "donate" });
    expect(fetchLatest).not.toHaveBeenCalled();
  });

  it("records first-run version without a donate prompt", async () => {
    const prompt = await decideLaunchPrompt({
      currentVersion: "0.2.0",
      now: 0,
      releaseRepo: "OWNER/REPO",
      userAgent: "GoldenPath/0.2.0",
    });
    expect(prompt).toBeNull();
    expect(localStorage.getItem("gp.update.lastSeenVersion")).toBe("0.2.0");
  });

  it("prompts update for a newer matching asset", async () => {
    markVersionSeen("0.1.0");
    markUpdateChecked(0);
    const fetchLatest = vi.fn().mockResolvedValue({
      htmlUrl: "https://github.com/acme/app/releases/latest",
      assets: [{ name: "Golden-Path-0.2.0-x64-setup.exe", url: "https://example.com/setup.exe" }],
    });
    const prompt = await decideLaunchPrompt({
      currentVersion: "0.1.0",
      now: MS_DAY,
      releaseRepo: "acme/app",
      userAgent: "GoldenPath/0.1.0",
      fetchLatest,
    });
    expect(prompt).toEqual({
      kind: "update",
      version: "0.2.0",
      url: "https://example.com/setup.exe",
    });
  });

  it("stays silent when the newer version was dismissed", async () => {
    markVersionSeen("0.1.0");
    markUpdateChecked(0, "0.2.0");
    const fetchLatest = vi.fn().mockResolvedValue({
      htmlUrl: "https://github.com/acme/app/releases/latest",
      assets: [{ name: "Golden-Path-0.2.0-x64-setup.exe", url: "https://example.com/setup.exe" }],
    });
    const prompt = await decideLaunchPrompt({
      currentVersion: "0.1.0",
      now: MS_DAY,
      releaseRepo: "acme/app",
      userAgent: "GoldenPath/0.1.0",
      fetchLatest,
    });
    expect(prompt).toBeNull();
  });

  it("never returns donate and update together", async () => {
    markVersionSeen("0.1.0");
    const fetchLatest = vi.fn().mockResolvedValue({
      htmlUrl: "https://github.com/acme/app/releases/latest",
      assets: [{ name: "Golden-Path-9.9.9-x64-setup.exe", url: "https://example.com/setup.exe" }],
    });
    const prompt = await decideLaunchPrompt({
      currentVersion: "0.2.0",
      now: MS_DAY,
      releaseRepo: "acme/app",
      userAgent: "GoldenPath/0.2.0",
      fetchLatest,
    });
    expect(prompt?.kind).toBe("donate");
    expect(fetchLatest).not.toHaveBeenCalled();
  });
});
