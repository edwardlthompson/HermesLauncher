import { describe, expect, it } from "vitest";
import { DONATE_LABEL, VENMO_DONATE_URL, withQuietDonate } from "./donate";

describe("withQuietDonate", () => {
  it("always adds the Venmo link", () => {
    const result = withQuietDonate({ enabled: false, message: "", links: [] });
    expect(result.enabled).toBe(true);
    expect(result.links).toEqual([{ label: DONATE_LABEL, url: VENMO_DONATE_URL }]);
  });

  it("does not duplicate an existing Venmo URL", () => {
    const result = withQuietDonate({
      enabled: true,
      message: "Thanks",
      links: [{ label: "Venmo", url: VENMO_DONATE_URL }],
    });
    expect(result.links).toHaveLength(1);
    expect(result.links[0]?.label).toBe(DONATE_LABEL);
  });
});
