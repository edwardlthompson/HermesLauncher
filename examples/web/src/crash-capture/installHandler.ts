import { type PendingCrash, persistPendingCrash } from "./pendingCrash";

let installed = false;

export function installCrashHandler(optIn: () => boolean): void {
  if (installed) return;
  installed = true;
  const store = (record: PendingCrash) => {
    if (!optIn()) return;
    try {
      persistPendingCrash(record);
    } catch {
      /* never re-enter */
    }
  };
  window.addEventListener("error", (event) => {
    store({
      message: String(event.message || "Error"),
      stack: String(event.error?.stack || ""),
    });
  });
  window.addEventListener("unhandledrejection", (event) => {
    const reason = event.reason;
    store({
      message: String(reason?.message || reason || "UnhandledRejection"),
      stack: String(reason?.stack || ""),
    });
  });
}
