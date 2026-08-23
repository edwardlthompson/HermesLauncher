import { beforeEach, describe, expect, it } from "vitest";
import { onSaveCrashesChanged, persistPendingCrash, readPendingCrash } from "./pendingCrash";

describe("pendingCrash", () => {
  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  it("sanitizes before persist and queues one record", () => {
    persistPendingCrash({
      message: "boom ghp_abcdefghijklmnopqrstuvwxyz012345",
      stack: "at C:\\Users\\Ada\\x.ts",
    });
    const read = readPendingCrash();
    expect(read?.message).not.toContain("ghp_");
    expect(read?.stack).not.toContain("Ada");
  });

  it("does not write localStorage unless saveLocal", () => {
    persistPendingCrash({ message: "e", stack: "s" }, false);
    expect(localStorage.getItem("gp.crash.pending.local")).toBeNull();
  });

  it("clears when save-crashes turns off", () => {
    persistPendingCrash({ message: "e", stack: "s" }, true);
    onSaveCrashesChanged(false);
    expect(readPendingCrash()).toBeNull();
  });
});
