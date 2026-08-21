import AxeBuilder from "@axe-core/playwright";
import { expect, test } from "@playwright/test";

test("renders golden path heading", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByText("Hello, FOSS!")).toBeVisible();
  await expect(page.getByTestId("status")).toContainText("Golden Path PWA");
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
