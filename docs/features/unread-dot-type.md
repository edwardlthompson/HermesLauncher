# Feature: unread-dot-type

> Sprint 53. UnreadDot count uses `labelSmall` instead of 9sp. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: count badges use `MaterialTheme.typography.labelSmall`
- ✅ Offline/error behavior: count 0 still hides the badge
- ✅ Accessibility: type scale follows the theme
- ✅ i18n: N/A

## Smoke scenario

1. Given Inbox has unread items
2. When the FilterBar badge is visible
3. Then the numeral uses labelSmall, not a hardcoded 9sp

## Container map

| Layer | Path |
|-------|------|
| Logic | N/A |
| View | `ui/launcher/UnreadDot` |
| Tests | N/A — Compose type |
| Wiring | none |

## Tests

- Automated: no

## Fallback validation

- Why tests are not feasible: Compose typography is not unit-testable without screenshot
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

UnreadDot no longer hardcodes 9sp.

## Notes

- File stays under the 300-line UI cap.
