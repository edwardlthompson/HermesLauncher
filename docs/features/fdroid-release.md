# Feature: fdroid-release

> Sprint 7 vertical slice. F-Droid metadata and About/update stubs. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: Fastlane/F-Droid en-US copy names Hermes Launcher and the inbox-as-home promise
- Offline/error behavior: About donations and release-repo stubs stay FOSS-only; missing assets do not crash
- Accessibility: N/A for metadata files
- i18n: store copy lives in Fastlane/metadata trees, not Compose literals

## Smoke scenario

1. Given root `fastlane/metadata/android/en-US/` (F-Droid scrape path) plus `examples/android/metadata/en-US/`
2. When a reviewer reads title and descriptions
3. Then they describe Hermes, not the Golden Path stub, and About unit tests still pass

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/about/` |
| View | About screen already shipped |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/about/` |
| Wiring | none — metadata only |

## Public API (locked)

| Item | Contract |
|------|----------|
| ApplicationId | `org.hermeslauncher.app` |
| Fastlane short description | ≤80 characters, Hermes-specific |
| Fastlane/F-Droid title | Hermes Launcher |
| Release repo | `edwardlthompson/HermesLauncher` or unset (tests accept both) |
| Donations | Venmo URL via `DonationsLoader`; no Play billing |

## Tests

Automated: yes

- Existing `DonationsLoaderTest`, `ReleaseTagFetcherTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — About stubs already have unit tests.

## Definition of Done

Metadata is Hermes-branded. About tests remain green. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Reproducible APK / `SOURCE_DATE_EPOCH` stays a pre-release AUTO row
- Phone screenshots are still a later ADB/HUMAN item (`fastlane/metadata/android/en-US/images/phoneScreenshots/README.md`)
- APK version is single-sourced from `schemas/golden-path/app-version.json` (1.0.4 / 104). Git tags `v1.6.x` are template/Release Please tags and do not match `versionName`.
