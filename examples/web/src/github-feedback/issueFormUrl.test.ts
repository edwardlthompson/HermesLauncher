import { describe, expect, it, vi } from "vitest";
import { createCooldown } from "./cooldown";
import { parseSearchItems, searchDuplicateIssues } from "./duplicateSearch";
import { buildIssueFormUrl, crashTitle, isPlaceholderRepo, MAX_QUERY_CHARS } from "./issueFormUrl";

describe("crashTitle", () => {
  it("formats fingerprint and type", () => {
    expect(crashTitle("A1B2C3D4E5F6ffff", "TypeError: x")).toBe("[crash] a1b2c3d4e5f6 TypeError");
  });
});

describe("isPlaceholderRepo", () => {
  it("treats empty and OWNER/REPO as placeholders", () => {
    expect(isPlaceholderRepo("")).toBe(true);
    expect(isPlaceholderRepo("OWNER/REPO")).toBe(true);
    expect(isPlaceholderRepo("acme/app")).toBe(false);
  });
});

describe("buildIssueFormUrl", () => {
  it("returns empty URL for OWNER/REPO", () => {
    expect(buildIssueFormUrl("OWNER/REPO", "crash_report.yml", { description: "x" }).url).toBe("");
  });

  it("prefills small fields", () => {
    const built = buildIssueFormUrl("acme/app", "crash_report.yml", {
      title: "[crash] abc TypeError",
      description: "boom",
    });
    expect(built.bodyTooLarge).toBe(false);
    expect(built.url).toContain("github.com/acme/app/issues/new");
    expect(built.url).toContain("template=crash_report.yml");
  });

  it("drops a large stack from the query string", () => {
    const stack = "x".repeat(MAX_QUERY_CHARS + 500);
    const built = buildIssueFormUrl("acme/app", "crash_report.yml", { stack, title: "[crash] ab" });
    expect(built.bodyTooLarge).toBe(true);
    expect(built.url.length).toBeLessThan(MAX_QUERY_CHARS);
    expect(built.url).not.toContain(stack.slice(0, 40));
    expect(built.clipboardMarkdown).toBe(stack);
  });
});

describe("searchDuplicateIssues", () => {
  it("does not fetch OWNER/REPO", async () => {
    const fetchImpl = vi.fn();
    await expect(searchDuplicateIssues("OWNER/REPO", "abc", fetchImpl)).resolves.toEqual([]);
    expect(fetchImpl).not.toHaveBeenCalled();
  });

  it("reuses cache inside 60s", async () => {
    let now = 1_000;
    const cd = createCooldown(() => now);
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ items: [{ title: "t", html_url: "https://x", number: 1 }] }),
    });
    const first = await searchDuplicateIssues("acme/app", "abc", fetchImpl, "UA", cd);
    now += 1_000;
    const second = await searchDuplicateIssues("acme/app", "abc", fetchImpl, "UA", cd);
    expect(first).toEqual(second);
    expect(fetchImpl).toHaveBeenCalledTimes(1);
  });

  it("parses search items", () => {
    expect(parseSearchItems([{ title: "t", html_url: "https://x", number: 2 }])[0]?.number).toBe(2);
    expect(parseSearchItems(null)).toEqual([]);
  });
});
