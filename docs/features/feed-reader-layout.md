# Feature: reader chrome, dates, Nova import

> Sprint 35. Reader nav sits on the feed tray; YY/MM/DD dates; Inbox settings; Nova `.novabackup` import.

## Acceptance criteria

- ✅ User-visible behavior: prev/next bar is flush to the bottom of the News tray with contrast dividers; published date `YY/MM/DD` under titles on cards and in the reader; Inbox FilterBar Settings opens Hermes Settings; first-run Inbox offers Nova backup import (auto-scan Downloads for `.novabackup`)
- ✅ Offline/error behavior: missing pubDate stays blank; Nova picker toasts on invalid zip; widgets are not imported
- ✅ Accessibility: Settings uses `settings_open`; Nova card uses `home_setup_nova` / `backup_nova`
- ✅ i18n: `home_setup_nova*`, `backup_nova*`

## Smoke scenario

1. Given News is open with Android Authority cards
2. When the user opens an article
3. Then the arrow bar sits on the tray bottom, dates show as `YY/MM/DD`, and Inbox Settings opens the hub

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/ArticleStamp.kt`, `workspace/NovaBackup.kt`, `workspace/NovaLayout.kt`, `l3/L3NovaImport.kt` |
| View | `ui/player/FeedReader.kt`, `ui/player/FeedCard.kt`, `ui/inbox/FilterBar.kt`, `ui/onboarding/NovaSetupCard.kt` |
| Tests | `ArticleStampTest.kt`, `NovaLayoutTest.kt` |
| Wiring | `HermesInboxPage`, `HermesApplication`, `NovaImportActivity` |
## Tests

- Automated: yes — `ArticleStampTest`, `NovaLayoutTest`
- Coverage: stamp format + Nova intent/desktop/dock mapping

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/op12-feed-inbox-smoke.py`

## Definition of Done

Reader chrome + dates + Inbox settings + Nova import path on OP12.
