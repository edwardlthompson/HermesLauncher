import { describe, expect, it } from "vitest";

import { createApp } from "./app.js";

describe("createApp", () => {
  it("returns health status", async () => {
    const res = await createApp().request("/health");
    expect(res.status).toBe(200);
    expect(await res.json()).toEqual({ status: "ok" });
  });

  it("greets by name", async () => {
    const res = await createApp().request("/greet/FOSS");
    expect(res.status).toBe(200);
    expect(await res.json()).toEqual({ message: "Hello, FOSS!" });
  });

  it("defaults to world", async () => {
    const res = await createApp().request("/greet");
    expect(await res.json()).toEqual({ message: "Hello, world!" });
  });

  it("returns About payload", async () => {
    const res = await createApp().request("/about");
    expect(res.status).toBe(200);
    const body = await res.json();
    expect(body).toMatchObject({ version: "0.1.0" });
    expect(String(body.summary)).toContain("donate");
  });
});
