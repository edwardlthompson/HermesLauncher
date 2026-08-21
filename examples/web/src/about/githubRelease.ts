import type { NamedAsset } from "./productUpdate";
import { DEFAULT_ASSET_PREFIX } from "./productUpdate";

export interface GithubRelease {
  htmlUrl: string;
  assets: NamedAsset[];
}

export function parseGithubRelease(raw: unknown): GithubRelease | null {
  if (!raw || typeof raw !== "object") return null;
  const obj = raw as { html_url?: unknown; assets?: unknown };
  const htmlUrl = typeof obj.html_url === "string" ? obj.html_url : "";
  if (!Array.isArray(obj.assets)) return { htmlUrl, assets: [] };
  const assets: NamedAsset[] = [];
  for (const item of obj.assets) {
    if (!item || typeof item !== "object") continue;
    const a = item as { name?: unknown; browser_download_url?: unknown };
    if (typeof a.name === "string" && typeof a.browser_download_url === "string") {
      assets.push({ name: a.name, url: a.browser_download_url });
    }
  }
  return { htmlUrl, assets };
}

export function isPlaceholderRepo(repo: string): boolean {
  const trimmed = repo.trim();
  return !trimmed || trimmed.toUpperCase() === "OWNER/REPO";
}

export function releaseApiUrl(repo: string): string | null {
  if (isPlaceholderRepo(repo)) return null;
  return `https://api.github.com/repos/${repo.trim()}/releases/latest`;
}

export function releasePageUrl(repo: string): string {
  return `https://github.com/${repo.trim()}/releases/latest`;
}

export async function fetchLatestGithubRelease(
  currentVersion: string,
  releaseRepo: string,
  fetchImpl: typeof fetch = fetch,
): Promise<GithubRelease | null> {
  const api = releaseApiUrl(releaseRepo);
  if (!api) return null;
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 10_000);
  try {
    const res = await fetchImpl(api, {
      signal: ctrl.signal,
      headers: {
        Accept: "application/vnd.github+json",
        "User-Agent": `${DEFAULT_ASSET_PREFIX}/${currentVersion}`,
      },
    });
    if (!res.ok) return null;
    return parseGithubRelease(await res.json());
  } catch {
    return null;
  } finally {
    clearTimeout(timer);
  }
}
