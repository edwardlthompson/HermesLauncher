import { describe, expect, it, vi } from "vitest";
import { fetchLatestGithubRelease, isPlaceholderRepo, parseGithubRelease } from "./githubRelease";

describe("parseGithubRelease", () => {
  it("ignores empty or malformed payloads", () => {
    expect(parseGithubRelease(null)).toBeNull();
    expect(parseGithubRelease({ html_url: "https://example.com/r", assets: [] })?.assets).toEqual(
      [],
    );
  });

  it("keeps named download URLs", () => {
    const parsed = parseGithubRelease({
      html_url: "https://example.com/r",
      assets: [
        { name: "Golden-Path-0.18.0-x64-setup.exe", browser_download_url: "https://example.com/e" },
      ],
    });
    expect(parsed?.htmlUrl).toBe("https://example.com/r");
    expect(parsed?.assets[0]?.url).toBe("https://example.com/e");
  });
});

describe("isPlaceholderRepo", () => {
  it("treats OWNER/REPO as a no-op", () => {
    expect(isPlaceholderRepo("OWNER/REPO")).toBe(true);
    expect(isPlaceholderRepo("acme/app")).toBe(false);
  });
});

describe("fetchLatestGithubRelease", () => {
  it("returns null on timeout or error", async () => {
    const fetchImpl = vi.fn().mockRejectedValue(new Error("timeout"));
    await expect(fetchLatestGithubRelease("1.0.0", "acme/app", fetchImpl)).resolves.toBeNull();
  });

  it("returns null when response is not ok", async () => {
    const fetchImpl = vi.fn().mockResolvedValue({ ok: false });
    await expect(fetchLatestGithubRelease("1.0.0", "acme/app", fetchImpl)).resolves.toBeNull();
  });
});
