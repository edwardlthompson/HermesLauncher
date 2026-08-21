import { describe, expect, it } from "vitest";
import {
  isNewerVersion,
  MS_DAY,
  parseAssetVersion,
  selectProductAsset,
  shouldCheckDaily,
  shouldNudgeDonate,
  shouldPromptUpdate,
} from "./productUpdate";

describe("shouldCheckDaily", () => {
  it("waits a full day", () => {
    expect(shouldCheckDaily(null, 0)).toBe(true);
    expect(shouldCheckDaily(0, MS_DAY - 1)).toBe(false);
    expect(shouldCheckDaily(0, MS_DAY)).toBe(true);
  });
});

describe("parseAssetVersion", () => {
  it("reads product filenames, not template tags", () => {
    expect(parseAssetVersion("Golden-Path-1.2.3-x64-setup.exe", "exe", "Golden-Path")).toBe(
      "1.2.3",
    );
    expect(parseAssetVersion("golden-path-1.2.3-foss.apk", "apk", "Golden-Path")).toBe("1.2.3");
    expect(parseAssetVersion("v0.22.1", "exe", "Golden-Path")).toBeNull();
  });
});

describe("selectProductAsset", () => {
  it("selects the matching installer URL", () => {
    const picked = selectProductAsset(
      [
        { name: "sbom.cyclonedx.json", url: "https://example.com/sbom" },
        { name: "Golden-Path-0.18.0-x64-setup.exe", url: "https://example.com/setup.exe" },
      ],
      "exe",
      "Golden-Path",
    );
    expect(picked).toEqual({ version: "0.18.0", url: "https://example.com/setup.exe" });
  });
});

describe("shouldNudgeDonate", () => {
  it("nudges only after a version change", () => {
    expect(shouldNudgeDonate(null, "0.17.3")).toBe(false);
    expect(shouldNudgeDonate("0.17.3", "0.17.3")).toBe(false);
    expect(shouldNudgeDonate("0.17.3", "0.17.4")).toBe(true);
  });
});

describe("shouldPromptUpdate", () => {
  it("skips dismissed or equal versions", () => {
    expect(isNewerVersion("0.17.3", "0.17.4")).toBe(true);
    expect(shouldPromptUpdate("0.17.3", "0.17.4", null)).toBe(true);
    expect(shouldPromptUpdate("0.17.3", "0.17.4", "0.17.4")).toBe(false);
    expect(shouldPromptUpdate("0.17.4", "0.17.4", null)).toBe(false);
    expect(shouldPromptUpdate("0.17.3", null, null)).toBe(false);
  });
});
