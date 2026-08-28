# Feature: privacy-report

> Shared sanitizer, fingerprint, and markdown builder. No UI and no network.

## Acceptance criteria

- ✅ User-visible behavior: N/A — pure logic only
- ✅ Offline/error behavior: `null`/empty input becomes `""`; size cap drops excess lines (8 KiB / 200 stack lines)
- ✅ Accessibility: N/A
- ✅ i18n: N/A

## Smoke scenario

1. Given a stack containing `C:\Users\Ada\secret.env`, a `ghp_` token, a JWT, and `AKIA`
2. When `sanitizeReportText` and `buildReportMarkdown` run
3. Then none of those secrets remain and the fingerprint is stable if only the username in a path changes

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/web/src/privacy-report/`, `examples/android/.../privacyreport/`, `scripts/lib/privacy_report_*.py` |
| Tests | co-located Vitest/JUnit + `tests/privacy_report/` oracle |
| Wiring | none |
## Tests

- Automated: yes — co-located Vitest/JUnit plus `tests/privacy_report/`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack <active>`

## Definition of Done

See `docs/FEATURE_MODULES.md`. Fallback: `python3 -m unittest tests.privacy_report.test_sanitize`.

## Notes

- Run sanitize before persist and again before Copy / Open GitHub / `gh issue create`
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
