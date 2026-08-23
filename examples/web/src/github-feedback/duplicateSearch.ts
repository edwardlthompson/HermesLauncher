import { createCooldown } from "./cooldown";
import { isPlaceholderRepo } from "./issueFormUrl";

export interface DuplicateIssue {
  title: string;
  url: string;
  number: number;
}

const shared = createCooldown();

export function searchDuplicateIssues(
  repo: string,
  fingerprint: string,
  fetchImpl: typeof fetch = fetch,
  userAgent = "GoldenPath/0",
  cooldown = shared,
): Promise<DuplicateIssue[]> {
  const cached = cooldown.peekSearch<DuplicateIssue[]>();
  if (cached) return Promise.resolve(cached);
  if (isPlaceholderRepo(repo) || !fingerprint.trim()) {
    return Promise.resolve([]);
  }
  const api = new URL("https://api.github.com/search/issues");
  api.searchParams.set("q", `repo:${repo.trim()} "[crash] ${fingerprint.trim()}" in:title`);
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 10_000);
  return fetchImpl(api.toString(), {
    signal: ctrl.signal,
    headers: {
      Accept: "application/vnd.github+json",
      "User-Agent": userAgent,
    },
  })
    .then(async (res) => {
      if (!res.ok) return [];
      const data = (await res.json()) as { items?: unknown };
      return parseSearchItems(data.items);
    })
    .catch(() => [])
    .finally(() => clearTimeout(timer))
    .then((items) => cooldown.cachedSearch(items));
}

export function parseSearchItems(items: unknown): DuplicateIssue[] {
  if (!Array.isArray(items)) return [];
  const out: DuplicateIssue[] = [];
  for (const item of items) {
    if (!item || typeof item !== "object") continue;
    const row = item as { title?: unknown; html_url?: unknown; number?: unknown };
    if (typeof row.title !== "string" || typeof row.html_url !== "string") continue;
    if (typeof row.number !== "number") continue;
    out.push({ title: row.title, url: row.html_url, number: row.number });
  }
  return out;
}
