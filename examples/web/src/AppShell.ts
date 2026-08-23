import { APP_VERSION } from "./about/aboutSession";
import { createLaunchPromptDialog } from "./about/launchPrompt";
import type { LaunchPrompt } from "./about/runAppUpdates";
import type { DonationConfig } from "./about/types";
import { createAboutPanel } from "./components/AboutPanel";
import { createFeedbackPanel, type FeedbackKind } from "./components/FeedbackPanel";
import { createSettingsPanel } from "./components/SettingsPanel";
import { createThemeToggle } from "./components/ThemeToggle";
import { isOnline } from "./greet";
import { t } from "./i18n";
import { bindPanelDialog } from "./panelDialog";

let dialogCleanup: (() => void) | undefined;

export type AppShellState = {
  showAbout: boolean;
  showSettings: boolean;
  showFeedback: FeedbackKind | null;
  updateStatus: string;
  donations: DonationConfig;
  launchPrompt: LaunchPrompt | null;
  releaseRepo?: string;
};

export type AppShellCallbacks = {
  onState: (next: Partial<AppShellState>) => void;
  onApplyUpdate?: () => void;
  onDonate?: () => void;
  onLaunchPrompt?: (accepted: boolean) => void;
  canApplyUpdate?: boolean;
};

export function createAppShell(
  root: HTMLElement,
  state: AppShellState,
  callbacks: AppShellCallbacks,
): void {
  const online = isOnline();
  const statusKey = online ? "app.status.online" : "app.status.offline";
  const donateEnabled = state.donations.enabled && state.donations.links.length > 0;

  root.innerHTML = `
    <main>
      <div class="gp-header">
        <h1 class="gp-title">${t("app.title")}</h1>
        <div class="gp-header-actions">
          ${
            donateEnabled
              ? `<button type="button" class="gp-donate-btn" data-donate-open>${t("about.donate")}</button>`
              : ""
          }
          <button type="button" class="gp-settings-btn" data-settings-open aria-label="${t("settings.open")}">⚙</button>
          <button type="button" class="gp-about-btn" data-about-open aria-label="${t("about.open")}">i</button>
        </div>
      </div>
      <p class="gp-headline">${t("app.greeting")}</p>
      <p class="gp-body" data-testid="status">${t(statusKey)}</p>
      <div data-panel-mount></div>
    </main>
  `;

  const actions = root.querySelector<HTMLDivElement>(".gp-header-actions");
  if (actions) {
    actions.insertBefore(createThemeToggle(), actions.firstChild);
  }

  root.querySelector("[data-donate-open]")?.addEventListener("click", () => {
    callbacks.onDonate?.();
  });

  root.querySelector("[data-about-open]")?.addEventListener("click", () => {
    callbacks.onState({ showAbout: !state.showAbout, showSettings: false, showFeedback: null });
  });

  root.querySelector("[data-settings-open]")?.addEventListener("click", () => {
    callbacks.onState({ showSettings: !state.showSettings, showAbout: false, showFeedback: null });
  });

  const mount = root.querySelector("[data-panel-mount]");
  if (!mount) return;

  dialogCleanup?.();
  dialogCleanup = undefined;
  mount.innerHTML = "";

  if (state.launchPrompt) {
    const promptDialog = createLaunchPromptDialog(state.launchPrompt, (accepted) => {
      callbacks.onLaunchPrompt?.(accepted);
    });
    mount.appendChild(promptDialog);
    return;
  }

  if (state.showFeedback) {
    const panel = createFeedbackPanel(state.showFeedback, {
      onClose: () => callbacks.onState({ showFeedback: null }),
      releaseRepo: state.releaseRepo ?? "",
    });
    mount.appendChild(panel);
    dialogCleanup = bindPanelDialog(panel, () => callbacks.onState({ showFeedback: null }));
    return;
  }

  if (state.showSettings) {
    const panel = createSettingsPanel({
      onClose: () => callbacks.onState({ showSettings: false }),
    });
    mount.appendChild(panel);
    dialogCleanup = bindPanelDialog(panel, () => callbacks.onState({ showSettings: false }));
    return;
  }

  if (!state.showAbout) return;

  mount.appendChild(
    createAboutPanel(
      {
        version: APP_VERSION,
        updateStatus: state.updateStatus,
        donations: state.donations,
        canApplyUpdate: callbacks.canApplyUpdate,
      },
      () => callbacks.onState({ showAbout: false }),
      callbacks.onApplyUpdate,
      () => callbacks.onState({ showAbout: false, showFeedback: "bug" }),
      () => callbacks.onState({ showAbout: false, showFeedback: "feature" }),
    ),
  );
  const aboutPanel = mount.lastElementChild as HTMLElement;
  dialogCleanup = bindPanelDialog(aboutPanel, () => callbacks.onState({ showAbout: false }));
}
