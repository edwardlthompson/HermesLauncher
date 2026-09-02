# Feature: oem-onboarding

> Sprint 6 vertical slice. OEM detect + permission-repair banner. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: a banner explains missing notification access or battery restriction and names the OEM family
- Offline/error behavior: unknown manufacturer maps to `OTHER`; no network; detector is string-only
- Accessibility: banner action has a content description
- i18n: keys under `oem_*` in `res/values/strings.xml`

## Smoke scenario

1. Given manufacturer `OnePlus` and a snapshot with listener off
2. When `OemDetector.detect` and `RepairPolicy.needsBanner` run
3. Then the family is `ONEPLUS` and the banner should show

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/oem/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/onboarding/` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/oem/` |
| Wiring | Banner is available for `LauncherHome`; Settings intent is later |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `OemFamily` | `ONEPLUS`, `SAMSUNG`, `XIAOMI`, `PIXEL`, `LINEAGE`, `OTHER` |
| `OemDetector.detect(manufacturer, display)` | Lineage display wins over manufacturer |
| `PermissionSnapshot` | `notificationListenerEnabled`, `batteryUnrestricted` |
| `RepairPolicy.needsBanner(snapshot)` | true if either flag is false |
| `RepairPolicy.primaryStep(oem)` | stable step id for copy lookup |

## Tests

Automated: yes

- `OemDetectorTest`, `RepairPolicyTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — detector uses fixtures, not a live OEM.

## Definition of Done

Detector covers OnePlus + Lineage. Banner uses `oem_*` strings. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Do not silently collect notifications after revoke (ADR-0002 / ADR-0004)
- Autostart extras stay OEM-specific follow-up
