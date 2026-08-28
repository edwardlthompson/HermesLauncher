import { describe, expect, it } from "vitest";

import { applySettingsBundle, parseSettings, snapshotSettings } from "./export";

describe("settings export", () => {
  it("roundtrips theme and save-crashes", () => {
    applySettingsBundle({ version: 1, theme: "light", saveCrashes: true });
    const raw = JSON.stringify(snapshotSettings());
    const parsed = parseSettings(raw);
    expect(parsed).toEqual({ version: 1, theme: "light", saveCrashes: true });
  });

  it("rejects invalid payload", () => {
    expect(parseSettings("{")).toBeNull();
    expect(parseSettings('{"version":2,"theme":"dark"}')).toBeNull();
  });
});
