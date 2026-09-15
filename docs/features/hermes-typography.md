# Feature: hermes-typography

> Sprint 53. Expand HermesTypography from design tokens. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Type.kt includes titleMedium, titleSmall, headlineSmall, bodySmall, labelMedium, and labelSmall
- ✅ Offline/error behavior: N/A — generated file
- ✅ Accessibility: UnreadDot labelSmall now comes from tokens
- ✅ i18n: N/A

## Smoke scenario

1. Given `scripts/sync-design-tokens.py` has been run
2. When Type.kt is compiled
3. Then Material 3 roles used by Inbox chrome exist

## Container map

| Layer | Path |
|-------|------|
| Logic | `design-tokens/design-tokens.json` |
| View | generated `ui/theme/Type.kt` |
| Tests | compile `Type.kt` |
| Wiring | `scripts/sync-design-tokens.py` |

## Tests

- Automated: no

## Fallback validation

- Why tests are not feasible: generated Typography is compile-checked
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

HermesTypography covers the M3 roles Inbox already uses.

## Notes

- Do not hand-edit Type.kt.
