import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "node",
    fileParallelism: true,
    maxWorkers: process.env.VITEST_MAX_WORKERS || "50%",
    coverage: {
      provider: "v8",
      reporter: ["text", "json-summary"],
      reportsDirectory: "coverage",
    },
  },
});
