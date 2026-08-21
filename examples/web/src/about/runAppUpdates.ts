import { fetchLatestGithubRelease, type GithubRelease, releasePageUrl } from "./githubRelease";
import {
  DEFAULT_ASSET_PREFIX,
  type ProductKind,
  selectProductAsset,
  shouldCheckDaily,
  shouldNudgeDonate,
  shouldPromptUpdate,
} from "./productUpdate";
import { loadUpdatePrefs, markUpdateChecked, markVersionSeen } from "./updatePrefs";

export type LaunchPrompt = { kind: "donate" } | { kind: "update"; version: string; url: string };

export type DecideLaunchArgs = {
  currentVersion: string;
  now?: number;
  releaseRepo: string;
  userAgent: string;
  fetchLatest?: (currentVersion: string, releaseRepo: string) => Promise<GithubRelease | null>;
  interval?: string;
  prefix?: string;
  kind?: ProductKind;
};

export async function decideLaunchPrompt(args: DecideLaunchArgs): Promise<LaunchPrompt | null> {
  const now = args.now ?? Date.now();
  const prefs = loadUpdatePrefs();
  if (shouldNudgeDonate(prefs.lastSeenVersion, args.currentVersion)) {
    return { kind: "donate" };
  }
  markVersionSeen(args.currentVersion);
  if ((args.interval ?? "daily") === "off") return null;
  if (!shouldCheckDaily(prefs.lastCheckAt, now)) return null;
  const fetchLatest = args.fetchLatest ?? fetchLatestGithubRelease;
  const release = await fetchLatest(args.currentVersion, args.releaseRepo);
  markUpdateChecked(now);
  if (!release) return null;
  const prefix = args.prefix?.trim() || DEFAULT_ASSET_PREFIX;
  const kind = args.kind ?? "exe";
  const asset = selectProductAsset(release.assets, kind, prefix);
  const latest = asset?.version ?? null;
  if (!shouldPromptUpdate(args.currentVersion, latest, prefs.dismissedVersion) || !latest) {
    return null;
  }
  return {
    kind: "update",
    version: latest,
    url: asset?.url || release.htmlUrl || releasePageUrl(args.releaseRepo),
  };
}
