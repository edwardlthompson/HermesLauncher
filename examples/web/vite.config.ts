import { readFileSync, writeFileSync, existsSync } from "node:fs";
import path from "node:path";
import { defineConfig, type Plugin } from "vite";

const pkg = JSON.parse(
  readFileSync(new URL("./package.json", import.meta.url), "utf-8"),
) as { version: string };

function stampServiceWorkerCache(version: string): Plugin {
  return {
    name: "stamp-sw-cache",
    closeBundle() {
      const distSw = path.resolve("dist/sw.js");
      if (!existsSync(distSw)) return;
      const content = readFileSync(distSw, "utf-8");
      writeFileSync(distSw, content.replaceAll("__APP_VERSION__", version));
    },
  };
}

const CSP =
  "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; connect-src 'self' https://api.github.com; font-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'";

const SECURITY_HEADERS: Record<string, string> = {
  "Content-Security-Policy": `${CSP}; frame-ancestors 'none'`,
  "Referrer-Policy": "no-referrer",
  "Permissions-Policy":
    "camera=(), microphone=(), geolocation=(), payment=(), usb=(), interest-cohort=()",
};

function injectCspMeta(): Plugin {
  return {
    name: "inject-csp-meta",
    transformIndexHtml(html, ctx) {
      if (ctx.server) return html;
      const tag = `    <meta http-equiv="Content-Security-Policy" content="${CSP}" />\n`;
      return html.replace('    <meta name="referrer"', `${tag}    <meta name="referrer"`);
    },
  };
}

export default defineConfig({
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version),
  },
  plugins: [stampServiceWorkerCache(pkg.version), injectCspMeta()],
  base: process.env.VITE_BASE_PATH || "/",
  preview: { headers: SECURITY_HEADERS },
  build: {
    rollupOptions: {
      input: {
        main: "index.html",
      },
    },
  },
});
