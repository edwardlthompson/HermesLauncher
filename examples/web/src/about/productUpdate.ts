export const MS_DAY = 86_400_000;
export const DEFAULT_ASSET_PREFIX = "Golden-Path";

export type ProductKind = "exe" | "apk";

export interface NamedAsset {
  name: string;
  url: string;
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

export function shouldCheckDaily(lastCheckAt: number | null, now: number): boolean {
  if (lastCheckAt == null || !Number.isFinite(lastCheckAt)) return true;
  return now - lastCheckAt >= MS_DAY;
}

export function compareVersions(current: string, latest: string): number {
  const parse = (v: string) => v.split(".").map((n) => Number.parseInt(n, 10) || 0);
  const a = parse(current);
  const b = parse(latest);
  for (let i = 0; i < 3; i += 1) {
    const diff = (a[i] ?? 0) - (b[i] ?? 0);
    if (diff !== 0) return diff;
  }
  return 0;
}

export function isNewerVersion(current: string, latest: string): boolean {
  return compareVersions(current, latest) < 0;
}

export function parseAssetVersion(name: string, kind: ProductKind, prefix: string): string | null {
  const src = name.trim();
  const stem = prefix.trim();
  if (!src || !stem) return null;
  const re =
    kind === "exe"
      ? new RegExp(`${escapeRegExp(stem)}-(\\d+\\.\\d+\\.\\d+)`, "i")
      : new RegExp(`${escapeRegExp(stem.toLowerCase())}-(\\d+\\.\\d+\\.\\d+)-foss\\.apk`, "i");
  return re.exec(src)?.[1] ?? null;
}

export function selectProductAsset(
  assets: NamedAsset[],
  kind: ProductKind,
  prefix: string,
): { version: string; url: string } | null {
  for (const asset of assets) {
    const version = parseAssetVersion(asset.name, kind, prefix);
    if (version && asset.url) return { version, url: asset.url };
  }
  return null;
}

/** First launch records the version; later version changes get one donate note. */
export function shouldNudgeDonate(lastSeenVersion: string | null, currentVersion: string): boolean {
  if (!currentVersion.trim()) return false;
  if (!lastSeenVersion?.trim()) return false;
  return lastSeenVersion.trim() !== currentVersion.trim();
}

export function shouldPromptUpdate(
  currentVersion: string,
  latestVersion: string | null,
  dismissedVersion: string | null,
): boolean {
  if (!latestVersion) return false;
  if (!isNewerVersion(currentVersion, latestVersion)) return false;
  if (dismissedVersion && dismissedVersion === latestVersion) return false;
  return true;
}
