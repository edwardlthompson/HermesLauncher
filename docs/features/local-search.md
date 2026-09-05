# Feature: local-search

> Sprint 24. Nova #33–36. FOSS local only — no web. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: search corpora apps, inbox, optional local contacts, desktop/app shortcuts (microresults); window vs bar chrome; pref to cap app hits to one row
- 🔲 Offline/error behavior: `READ_CONTACTS` deny or timeout → empty contacts, other corpora still work; empty query keeps predicted + unread
- 🔲 Accessibility: result types announced
- 🔲 i18n: `search_*`

## Smoke scenario

1. Given query `mail` and three apps
2. When `HomeSearchRank.query` runs with app-row cap 1
3. Then at most one app hit is returned; inbox/feeds unchanged by that cap

## Container map

| Layer | Path |
|-------|------|
| Logic | `icons/HomeSearchRank.kt` + contacts adapter |
| View | `ui/launcher/HomeSearch.kt` chrome |
| Tests | rank + cap + contacts empty-on-deny |
| Wiring | Settings Search section |
## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `AllAppsStore` | launchable apps |
| `DeepShortcutManager` | microresults from existing shortcuts |
| `SearchManager` web | **not used** |
| `ContactsContract` | optional local |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty needle | predicted + unread (shipped) |
| Network timeout | N/A; contacts IO + empty on `SecurityException` |
| Race | rank is pure |
| Unhandled exceptions | contacts adapter never throws to UI |
## Tests

- Automated: yes — `HomeSearchRank` cap + empty contacts

## Fallback validation

- Why tests are not feasible: N/A for rank. Live contacts grant is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

No Google/web search. Shortcuts appear in the overlay. One-row cap is a pref.

## Notes

- Skip Nova web search
