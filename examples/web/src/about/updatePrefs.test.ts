import { beforeEach, describe, expect, it } from "vitest";
import { loadUpdatePrefs, markUpdateChecked, markVersionSeen } from "./updatePrefs";

describe("updatePrefs", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("stores last-check and dismissed version locally", () => {
    markUpdateChecked(100, "1.2.3");
    markVersionSeen("1.2.0");
    expect(loadUpdatePrefs()).toEqual({
      lastCheckAt: 100,
      lastSeenVersion: "1.2.0",
      dismissedVersion: "1.2.3",
    });
  });

  it("ignores non-numeric last-check values", () => {
    localStorage.setItem("gp.update.lastCheckAt", "nope");
    expect(loadUpdatePrefs().lastCheckAt).toBeNull();
  });
});
