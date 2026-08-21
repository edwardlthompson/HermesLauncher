import { assetUrl } from "../assetUrl";
import { clearRestartGuard, getRestartGuardKey, isRestartPending } from "./applyUpdate";
import { DEFAULT_ASSET_PREFIX } from "./productUpdate";
import type { AppUpdateConfig } from "./types";

export const APP_VERSION = __APP_VERSION__;

export async function loadAppUpdateConfig(): Promise<AppUpdateConfig | null> {
  try {
    const res = await fetch(assetUrl("app-update.json"));
    if (!res.ok) return null;
    return (await res.json()) as AppUpdateConfig;
  } catch {
    return null;
  }
}

export function assetPrefixOf(config: AppUpdateConfig | null): string {
  return config?.product_asset_prefix?.trim() || DEFAULT_ASSET_PREFIX;
}

export function handleRestartGuard(): boolean {
  const guardKey = getRestartGuardKey();
  if (isRestartPending(guardKey)) {
    clearRestartGuard(guardKey);
    return true;
  }
  return false;
}
