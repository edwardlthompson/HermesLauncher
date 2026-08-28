import { type AppShellState, createAppShell } from "./AppShell";
import {
  APP_VERSION,
  assetPrefixOf,
  handleRestartGuard,
  loadAppUpdateConfig,
} from "./about/aboutSession";
import { applyPwaUpdate } from "./about/applyUpdate";
import { loadDonations, primaryDonateUrl } from "./about/donations";
import { decideLaunchPrompt, type LaunchPrompt } from "./about/runAppUpdates";
import { markUpdateChecked, markVersionSeen } from "./about/updatePrefs";
import { assetUrl } from "./assetUrl";
import { installCrashHandler } from "./crash-capture/installHandler";
import { readPendingCrash } from "./crash-capture/pendingCrash";
import { getSaveCrashes } from "./feedback/saveCrashes";
import { t } from "./i18n";
import { shareTargetDescription } from "./share-target";
import { initTheme, subscribeThemeChange } from "./theme";

export function bootstrapApp(appRoot: HTMLDivElement): void {
  let state: AppShellState = {
    showAbout: false,
    showSettings: false,
    showFeedback: null,
    updateStatus: t("about.update.current"),
    donations: { enabled: false, message: "", links: [] },
    launchPrompt: null,
  };

  async function handleApplyUpdate(): Promise<void> {
    if (!("serviceWorker" in navigator)) return;
    const registration = await navigator.serviceWorker.getRegistration();
    if (!registration) return;
    const applied = await applyPwaUpdate(registration);
    if (applied) {
      state = { ...state, updateStatus: t("about.update.restarting") };
      render();
    }
  }

  function openDonate(): void {
    window.open(primaryDonateUrl(state.donations), "_blank", "noopener,noreferrer");
  }

  function handleLaunchPrompt(accepted: boolean): void {
    const prompt = state.launchPrompt;
    state = { ...state, launchPrompt: null };
    render();
    if (!prompt) return;
    if (prompt.kind === "donate") {
      markVersionSeen(APP_VERSION);
      if (accepted) openDonate();
      return;
    }
    markUpdateChecked(Date.now(), prompt.version);
    if (accepted) window.open(prompt.url, "_blank", "noopener,noreferrer");
  }

  function render(): void {
    createAppShell(appRoot, state, {
      onState: (patch) => {
        state = { ...state, ...patch };
        render();
      },
      onApplyUpdate: () => {
        void handleApplyUpdate();
      },
      onDonate: openDonate,
      onLaunchPrompt: handleLaunchPrompt,
      canApplyUpdate: false,
    });
  }

  initTheme();
  subscribeThemeChange(() => render());
  installCrashHandler(() => getSaveCrashes());
  if (readPendingCrash()) {
    state = { ...state, showFeedback: "bug" };
  }
  const shared = shareTargetDescription(new URLSearchParams(window.location.search));
  if (shared) {
    state = { ...state, showFeedback: "feature", feedbackPrefill: shared };
  }
  render();
  void loadDonations().then((d) => {
    state = { ...state, donations: d };
    render();
  });

  if (!handleRestartGuard()) {
    void (async () => {
      const config = await loadAppUpdateConfig();
      const prompt: LaunchPrompt | null = await decideLaunchPrompt({
        currentVersion: APP_VERSION,
        kind: "exe",
        prefix: assetPrefixOf(config),
        releaseRepo: config?.release_repo ?? "",
        userAgent: `GoldenPath/${APP_VERSION}`,
      });
      if (prompt) {
        state = { ...state, launchPrompt: prompt, releaseRepo: config?.release_repo ?? "" };
        render();
      } else {
        state = { ...state, releaseRepo: config?.release_repo ?? "" };
      }
    })();
  }

  window.addEventListener("online", render);
  window.addEventListener("offline", render);

  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register(assetUrl("sw.js")).catch(() => {});
    });
  }
}
