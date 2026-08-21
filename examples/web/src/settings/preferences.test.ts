import { beforeEach, describe, expect, it } from "vitest";
import { initTheme } from "../theme";
import { applySettingsThemeMode, getSettingsThemeMode } from "./preferences";

describe("settings preferences", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("persists theme across initTheme reload", () => {
    applySettingsThemeMode("dark");
    initTheme();
    expect(getSettingsThemeMode()).toBe("dark");
  });
});
