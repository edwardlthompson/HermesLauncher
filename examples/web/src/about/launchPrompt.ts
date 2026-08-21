import { t } from "../i18n";
import { bindPanelDialog } from "../panelDialog";
import type { LaunchPrompt } from "./runAppUpdates";

export function createLaunchPromptDialog(
  prompt: LaunchPrompt,
  onChoose: (accepted: boolean) => void,
): HTMLElement {
  const dialog = document.createElement("section");
  dialog.className = "gp-launch-dialog";
  dialog.dataset.testid = prompt.kind === "donate" ? "donate-nudge" : "update-prompt";

  const title = document.createElement("h2");
  const body = document.createElement("p");
  const actions = document.createElement("div");
  actions.className = "gp-launch-actions";

  const accept = document.createElement("button");
  accept.type = "button";
  accept.dataset.testid = "launch-accept";
  const decline = document.createElement("button");
  decline.type = "button";
  decline.dataset.testid = "launch-decline";

  if (prompt.kind === "donate") {
    title.textContent = t("about.donate.nudge.title");
    body.textContent = t("about.donate.nudge.body");
    accept.textContent = t("about.donate");
    decline.textContent = t("about.not_now");
  } else {
    title.textContent = t("about.update.available");
    body.textContent = t("about.update.message").replace("{version}", prompt.version);
    accept.textContent = t("about.update.install");
    decline.textContent = t("about.update.later");
  }

  accept.addEventListener("click", () => onChoose(true));
  decline.addEventListener("click", () => onChoose(false));
  actions.append(accept, decline);
  dialog.append(title, body, actions);
  bindPanelDialog(dialog, () => onChoose(false));
  return dialog;
}
