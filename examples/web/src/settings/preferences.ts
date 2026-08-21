import { getThemeMode, setThemeMode, type ThemeMode } from "../theme";

export function getSettingsThemeMode(): ThemeMode {
  return getThemeMode();
}

export function applySettingsThemeMode(mode: ThemeMode): void {
  setThemeMode(mode);
}
