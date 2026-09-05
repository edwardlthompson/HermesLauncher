# Third-Party Licenses

> Generated and maintained per release. See pre-release gate in `docs/INITIALIZATION_PROMPT.md` Section 7a.

## Project License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).

## Dependencies

Run license audits for active stacks:

```bash
# Web (npm)
cd examples/web && npx license-checker --production --summary

# Python (pip)
cd examples/python && uv run pip-licenses --format=markdown

# Rust / Go (optional stacks — MIT stubs; expand when deps are added)
grep 'license' examples/rust/Cargo.toml
head -1 examples/go/go.mod

```

`[AUTO]` CI runs `scripts/check-license-compliance.sh` on each push.

## Attribution

When bundling dependencies in releases (APK, desktop binary, etc.), include
this file or a generated `NOTICE` file in the distribution artifact.

Home-screen interaction is **AOSP Launcher3** tag `android-14.0.0_r28` (Apache-2.0), vendored under `examples/android/third_party/`. See that tree’s `NOTICE` and `README.md`. Hermes custom UI is the notification inbox and news feed only.

## Incompatible Licenses

`[HUMAN]` must approve any dependency with copyleft licenses (GPL, AGPL) that
may affect distribution. Document exceptions in `DECISION_LOG.md`.
