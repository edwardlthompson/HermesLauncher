import { getSaveCrashes, setSaveCrashes } from "../feedback/saveCrashes";
import { getThemeMode, setThemeMode, type ThemeMode } from "../theme";

export type SettingsBundle = {
  version: 1;
  theme: ThemeMode;
  saveCrashes: boolean;
};

const MODES = new Set<ThemeMode>(["system", "light", "dark"]);

export function snapshotSettings(): SettingsBundle {
  return { version: 1, theme: getThemeMode(), saveCrashes: getSaveCrashes() };
}

export function parseSettings(raw: string): SettingsBundle | null {
  try {
    const data = JSON.parse(raw) as Partial<SettingsBundle>;
    if (data.version !== 1 || !MODES.has(data.theme as ThemeMode)) {
      return null;
    }
    return {
      version: 1,
      theme: data.theme as ThemeMode,
      saveCrashes: Boolean(data.saveCrashes),
    };
  } catch {
    return null;
  }
}

export function applySettingsBundle(bundle: SettingsBundle): void {
  setThemeMode(bundle.theme);
  setSaveCrashes(bundle.saveCrashes);
}
