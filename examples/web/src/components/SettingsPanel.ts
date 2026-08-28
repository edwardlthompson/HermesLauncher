import { onSaveCrashesChanged } from "../crash-capture/pendingCrash";
import { getSaveCrashes, setSaveCrashes } from "../feedback/saveCrashes";
import { t } from "../i18n";
import { applySettingsBundle, parseSettings, snapshotSettings } from "../settings/export";
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
    <label class="gp-settings-field">
      <input type="checkbox" data-save-crashes />
      <span>${t("settings.feedback.save_crashes")}</span>
    </label>
    <div class="gp-settings-field">
      <button type="button" data-settings-export>${t("settings.export")}</button>
      <button type="button" data-settings-import>${t("settings.import")}</button>
      <input type="file" accept="application/json" hidden data-settings-import-file />
    </div>
  `;

  const themeSelect = panel.querySelector<HTMLSelectElement>("[data-settings-theme]");
  if (themeSelect) {
    themeSelect.value = themeMode;
    themeSelect.addEventListener("change", () => {
      applySettingsThemeMode(themeSelect.value as ThemeMode);
    });
  }

  const save = panel.querySelector<HTMLInputElement>("[data-save-crashes]");
  if (save) {
    save.checked = getSaveCrashes();
    save.addEventListener("change", () => {
      setSaveCrashes(save.checked);
      onSaveCrashesChanged(save.checked);
    });
  }

  panel.querySelector(".gp-settings-close")?.addEventListener("click", callbacks.onClose);

  const fileInput = panel.querySelector<HTMLInputElement>("[data-settings-import-file]");
  panel.querySelector("[data-settings-export]")?.addEventListener("click", () => {
    const blob = new Blob([JSON.stringify(snapshotSettings(), null, 2)], {
      type: "application/json",
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "golden-path-settings.json";
    link.click();
    URL.revokeObjectURL(url);
  });
  panel.querySelector("[data-settings-import]")?.addEventListener("click", () => {
    fileInput?.click();
  });
  fileInput?.addEventListener("change", () => {
    const file = fileInput.files?.[0];
    if (!file) return;
    void file.text().then((raw) => {
      const bundle = parseSettings(raw);
      if (bundle) applySettingsBundle(bundle);
    });
  });
  return panel;
}
