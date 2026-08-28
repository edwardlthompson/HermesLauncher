import { Hono } from "hono";

import { aboutPayload } from "./about.js";
import { greet } from "./greet.js";

export function createApp() {
  const app = new Hono();

  app.get("/health", (c) => c.json({ status: "ok" }));
  app.get("/about", (c) => c.json(aboutPayload()));

  app.get("/greet/:name?", (c) => {
    const name = c.req.param("name") ?? "";
    return c.json({ message: greet(name) });
  });

  return app;
}
