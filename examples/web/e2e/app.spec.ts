import AxeBuilder from "@axe-core/playwright";
import { expect, type Page, test } from "@playwright/test";

test("preview sends CSP and referrer policy", async ({ page }) => {
  const response = await page.goto("/");
  expect(response).toBeTruthy();
  const headers = response?.headers() ?? {};
  expect(headers["content-security-policy"] ?? "").toContain("default-src 'self'");
  expect(headers["referrer-policy"]).toBe("no-referrer");
  expect(headers["permissions-policy"] ?? "").toContain("camera=()");
});

test("renders golden path heading", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();
  await expect(page.getByTestId("status")).toContainText("Golden Path PWA");
});

test("rtl dir places the title after header actions", async ({ page }) => {
  await page.goto("/");
  await page.evaluate(() => {
    document.documentElement.setAttribute("dir", "rtl");
  });
  await expect(page.locator("html")).toHaveAttribute("dir", "rtl");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  const title = await page.locator(".gp-title").boundingBox();
  const actions = await page.locator(".gp-header-actions").boundingBox();
  expect(title).toBeTruthy();
  expect(actions).toBeTruthy();
  expect(title!.x).toBeGreaterThan(actions!.x);
});

test("reduced motion shortens theme-toggle transitions", async ({ page }) => {
  await page.emulateMedia({ reducedMotion: "reduce" });
  await page.goto("/");
  const duration = await page.locator(".gp-theme-toggle").evaluate((el) => {
    return Number.parseFloat(getComputedStyle(el).transitionDuration);
  });
  expect(duration).toBeLessThan(0.02);
});

async function tabUntil(page: Page, name: string): Promise<void> {
  const target = page.getByRole("button", { name });
  for (let i = 0; i < 12; i++) {
    if (await target.evaluate((el) => el === document.activeElement)) {
      return;
    }
    await page.keyboard.press("Tab");
  }
  await expect(target).toBeFocused();
}

test("keyboard-only opens Settings, About, and Feedback", async ({ page }) => {
  await page.goto("/");
  await page.locator("body").click({ position: { x: 0, y: 0 } });
  await tabUntil(page, "Settings");
  await page.keyboard.press("Enter");
  await expect(page.getByTestId("settings-panel")).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(page.getByTestId("settings-panel")).toHaveCount(0);

  await tabUntil(page, "About");
  await page.keyboard.press("Enter");
  await expect(page.getByTestId("about-panel")).toBeVisible();

  const bug = page.getByRole("button", { name: "Report a bug" });
  for (let i = 0; i < 12; i++) {
    if (await bug.evaluate((el) => el === document.activeElement)) {
      break;
    }
    await page.keyboard.press("Tab");
  }
  await expect(bug).toBeFocused();
  await page.keyboard.press("Enter");
  await expect(page.getByTestId("feedback-panel")).toBeVisible();
  await page.keyboard.press("Escape");
  await expect(page.getByTestId("feedback-panel")).toHaveCount(0);
});

test("passes accessibility audit", async ({ page }) => {
  await page.goto("/");
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("passes accessibility audit with settings panel open", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  await expect(page.getByTestId("settings-panel")).toBeVisible();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("passes accessibility audit with about panel open", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "About" }).click();
  await expect(page.getByTestId("about-panel")).toBeVisible();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("homepage visual snapshot", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("main")).toBeVisible();
  await expect(page).toHaveScreenshot("homepage.png", { maxDiffPixelRatio: 0.02 });
});

test("opens settings panel and toggles theme", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  await expect(page.getByRole("heading", { name: "Settings" })).toBeVisible();
  await page.locator("[data-settings-theme]").selectOption("dark");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("persists dark theme after reload", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "Settings" }).click();
  await page.locator("[data-settings-theme]").selectOption("dark");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await page.reload();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("opens about panel with donate link", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("button", { name: "About" }).click();
  await expect(page.getByRole("heading", { name: "About" })).toBeVisible();
  await expect(page.getByTestId("about-status")).toBeVisible();
  await expect(page.getByRole("link", { name: "Donate via Venmo" })).toHaveAttribute(
    "href",
    "https://venmo.com/code?user_id=1857304970395648420",
  );
});

test("shows quiet donate action in the header", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("button", { name: "Donate via Venmo" })).toBeVisible();
});

test.describe("donate nudge", () => {
  test("shows once after a version change", async ({ page }) => {
    await page.addInitScript(() => {
      if (!localStorage.getItem("gp.update.lastSeenVersion")) {
        localStorage.setItem("gp.update.lastSeenVersion", "0.0.1");
      }
    });
    await page.goto("/");
    await expect(page.getByTestId("donate-nudge")).toBeVisible();
    await expect(page.getByRole("heading", { name: "Development is still going" })).toBeVisible();
    await page.getByTestId("launch-decline").click();
    await expect(page.getByTestId("donate-nudge")).toHaveCount(0);
    await page.reload();
    await expect(page.getByTestId("donate-nudge")).toHaveCount(0);
  });
});

test.describe("PWA apply update", () => {
  test.use({ serviceWorkers: "block" });

  test("clears restart guard on load", async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem("gp-update-restart-pending", "true");
    });
    await page.goto("/");
    const pending = await page.evaluate(() => localStorage.getItem("gp-update-restart-pending"));
    expect(pending).toBeNull();
  });
});

test("serves cached shell offline via service worker", async ({ page, context }) => {
  await page.goto("/");
  await page.waitForLoadState("networkidle");
  await page.waitForFunction(() => navigator.serviceWorker?.controller != null, null, {
    timeout: 15_000,
  });
  await page.reload();
  await page.waitForLoadState("networkidle");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();

  await context.setOffline(true);
  await page.reload();
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();
  await expect(page.getByTestId("status")).toBeVisible();
});
