import { beforeEach, describe, expect, it, vi } from "vitest";
import type { AppShellCallbacks } from "./AppShell";
import { handleRestartGuard } from "./about/aboutSession";
import { bootstrapApp } from "./appBootstrap";
import en from "./locales/en.json";

const messages = en as Record<string, string>;

vi.mock("./AppShell", () => ({
  createAppShell: vi.fn(),
}));

vi.mock("./about/aboutSession", async () => {
  const actual =
    await vi.importActual<typeof import("./about/aboutSession")>("./about/aboutSession");
  return {
    ...actual,
    handleRestartGuard: vi.fn(() => false),
    loadAppUpdateConfig: vi.fn(() => Promise.resolve(null)),
  };
});

vi.mock("./about/runAppUpdates", () => ({
  decideLaunchPrompt: vi.fn(() => Promise.resolve(null)),
}));

vi.mock("./about/donations", () => ({
  loadDonations: vi.fn(() => Promise.resolve({ enabled: true, message: "thanks", links: [] })),
  primaryDonateUrl: vi.fn(() => "https://venmo.com/code?user_id=1857304970395648420"),
}));

vi.mock("./theme", () => ({
  initTheme: vi.fn(),
  subscribeThemeChange: vi.fn(),
}));

vi.mock("./i18n", () => ({
  t: vi.fn((key: string) => messages[key] ?? key),
}));

vi.mock("./about/applyUpdate", () => ({
  applyPwaUpdate: vi.fn(() => Promise.resolve(true)),
}));

import { createAppShell } from "./AppShell";
import { applyPwaUpdate } from "./about/applyUpdate";
import { decideLaunchPrompt } from "./about/runAppUpdates";

const mockedCreateAppShell = vi.mocked(createAppShell);
const mockedDecide = vi.mocked(decideLaunchPrompt);
const mockedApplyPwaUpdate = vi.mocked(applyPwaUpdate);

describe("bootstrapApp", () => {
  let handlers: AppShellCallbacks | undefined;

  function requireHandlers(): AppShellCallbacks {
    if (!handlers) {
      throw new Error("App shell handlers were not captured");
    }
    return handlers;
  }

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    handlers = undefined;
    mockedCreateAppShell.mockImplementation((_root, _state, h) => {
      handlers = h;
    });
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: { register: vi.fn(() => Promise.resolve()) },
    });
    vi.stubGlobal("open", vi.fn());
  });

  it("renders app shell on bootstrap", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => {
      expect(mockedCreateAppShell).toHaveBeenCalledWith(
        root,
        expect.objectContaining({
          updateStatus: messages["about.update.current"],
          launchPrompt: null,
        }),
        expect.any(Object),
      );
    });
  });

  it("renders immediately before donations load completes", async () => {
    const donationsMod = await import("./about/donations");
    vi.mocked(donationsMod.loadDonations).mockImplementation(() => new Promise(() => {}));
    const root = document.createElement("div");
    mockedCreateAppShell.mockClear();
    bootstrapApp(root);
    expect(mockedCreateAppShell).toHaveBeenCalled();
  });

  it("re-renders when shell state changes", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    requireHandlers().onState({ showAbout: true });
    expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore);
  });

  it("registers service worker on load", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    window.dispatchEvent(new Event("load"));
    await vi.waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalledWith("/sw.js");
    });
  });

  it("skips launch prompt when restart guard is active", async () => {
    vi.mocked(handleRestartGuard).mockReturnValueOnce(true);
    mockedDecide.mockClear();
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    expect(mockedDecide).not.toHaveBeenCalled();
  });

  it("shows a donate launch prompt when decide returns donate", async () => {
    mockedDecide.mockResolvedValueOnce({ kind: "donate" });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, state]) => state.launchPrompt?.kind === "donate"),
      ).toBe(true),
    );
  });

  it("opens the installer URL when an update prompt is accepted", async () => {
    mockedDecide.mockResolvedValueOnce({
      kind: "update",
      version: "0.2.0",
      url: "https://example.com/setup.exe",
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, state]) => state.launchPrompt?.kind === "update"),
      ).toBe(true),
    );
    requireHandlers().onLaunchPrompt?.(true);
    expect(window.open).toHaveBeenCalledWith(
      "https://example.com/setup.exe",
      "_blank",
      "noopener,noreferrer",
    );
  });

  it("declines donate and update prompts without opening a URL", async () => {
    mockedDecide.mockResolvedValueOnce({ kind: "donate" });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, state]) => state.launchPrompt?.kind === "donate"),
      ).toBe(true),
    );
    requireHandlers().onLaunchPrompt?.(false);
    expect(window.open).not.toHaveBeenCalled();
  });

  it("silences an update version when Later is chosen", async () => {
    mockedDecide.mockResolvedValueOnce({
      kind: "update",
      version: "0.3.0",
      url: "https://example.com/later.exe",
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, state]) => state.launchPrompt?.kind === "update"),
      ).toBe(true),
    );
    requireHandlers().onLaunchPrompt?.(false);
    expect(window.open).not.toHaveBeenCalled();
    expect(localStorage.getItem("gp.update.dismissedVersion")).toBe("0.3.0");
  });

  it("opens Venmo from the quiet donate action", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onDonate?.();
    expect(window.open).toHaveBeenCalled();
  });

  it("no-ops launch prompt when none is pending", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onLaunchPrompt?.(true);
    expect(window.open).not.toHaveBeenCalled();
  });

  it("opens Venmo when donate prompt is accepted", async () => {
    mockedDecide.mockResolvedValueOnce({ kind: "donate" });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, state]) => state.launchPrompt?.kind === "donate"),
      ).toBe(true),
    );
    requireHandlers().onLaunchPrompt?.(true);
    expect(window.open).toHaveBeenCalled();
  });

  it("applies PWA update through service worker registration", async () => {
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).toHaveBeenCalledWith(registration));
  });

  it("shows restarting status after apply succeeds", async () => {
    mockedApplyPwaUpdate.mockResolvedValueOnce(true);
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(
          ([, state]) => state.updateStatus === messages["about.update.restarting"],
        ),
      ).toBe(true),
    );
  });

  it("leaves status unchanged when applyPwaUpdate returns false", async () => {
    mockedApplyPwaUpdate.mockResolvedValueOnce(false);
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).toHaveBeenCalled());
    expect(
      mockedCreateAppShell.mock.calls.some(
        ([, state]) => state.updateStatus === messages["about.update.restarting"],
      ),
    ).toBe(false);
  });

  it("no-ops apply when service worker registration is missing", async () => {
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(undefined)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).not.toHaveBeenCalled());
  });
});
