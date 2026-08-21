import { t } from "../i18n";
import { applySettingsThemeMode, getSettingsThemeMode } from "../settings/preferences";
import type { ThemeMode } from "../theme";

export type SettingsPanelCallbacks = {
  onClose: () => void;
};

export function createSettingsPanel(callbacks: SettingsPanelCallbacks): HTMLElement {
  const panel = document.createElement("section");
  panel.className = "gp-settings-panel";
  panel.setAttribute("aria-label", t("settings.title"));
  panel.dataset.testid = "settings-panel";

  const themeMode = getSettingsThemeMode();

  panel.innerHTML = `
    <header class="gp-settings-header">
      <h2>${t("settings.title")}</h2>
      <button type="button" class="gp-settings-close" aria-label="${t("settings.close")}">×</button>
    </header>
    <label class="gp-settings-field">
      <span>${t("settings.theme.label")}</span>
      <select data-settings-theme>
        <option value="system">${t("settings.theme.mode.system")}</option>
        <option value="light">${t("settings.theme.mode.light")}</option>
        <option value="dark">${t("settings.theme.mode.dark")}</option>
      </select>
    </label>
  `;

  const themeSelect = panel.querySelector<HTMLSelectElement>("[data-settings-theme]");
  if (themeSelect) {
    themeSelect.value = themeMode;
    themeSelect.addEventListener("change", () => {
      applySettingsThemeMode(themeSelect.value as ThemeMode);
    });
  }

  panel.querySelector(".gp-settings-close")?.addEventListener("click", callbacks.onClose);
  return panel;
}
