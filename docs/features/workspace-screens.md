# Feature: workspace-screens

> Sprint 18. Launcher3 `WorkspaceScreens` + inbox/feeds split + page indicator + settings hub. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: swipe right from inbox shows a feeds/podcast page; swipe left shows desktop widget pages; HOME snaps to the inbox screen ID (not pager index 0); dots/line/none page indicator; Settings is a hub of title + one-line body rows
- 🔲 Offline/error behavior: corrupt v5 codec yields default screens (feeds + inbox + one desktop); missing `homeScreenId` falls back to the first INBOX id; empty feed pane shows existing empty copy
- 🔲 Accessibility: page indicator and hub rows have content descriptions
- 🔲 i18n: `workspace_*`, `settings_section_*` hub bodies in `res/values/strings.xml`

## Smoke scenario

1. Given a v4 widget host with two widget pages
2. When v5 migrate runs
3. Then `screenIds` is `[feedsId, inboxId, desktop1, desktop2, trailingEmpty]`, `homeScreenId` is `inboxId`, and widget bindings keep `appWidgetId`s

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/workspace/` + `widgets/WidgetHostCodec` v5 |
| View | `ui/launcher/` pager remap, `ui/workspace/` indicator, `ui/settings/` hub |
| Tests | `src/test/.../workspace/` + codec migrate |
| Wiring | `LauncherHome` ≤10 new lines via `WorkspacePager` extract |
## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `Workspace` + `PagedView` | `WorkspacePager` |
| `LauncherSettings.WorkspaceScreens` | `WorkspaceModel.screenIds: List<Long>` |
| `Workspace.DEFAULT_PAGE` | `homeScreenId` |
| `EXTRA_EMPTY_SCREEN_ID` | trailing empty desktop via `WidgetGrid.withTrailingEmpty` |
| `PageIndicatorDots` | `PageIndicator` composable |
| `SettingsActivity` | Settings hub `ListItem`s |
| Minus-one Discover overlay | **not used** — `WorkspaceKind.FEEDS` is a real screen |
## Public API (lock in Sequential)

| Symbol | Contract |
|--------|----------|
| `WorkspaceKind` | `INBOX`, `FEEDS`, `DESKTOP` |
| `WorkspaceScreen` | stable `id: Long`, `kind`, `items` |
| `WorkspaceModel` | `screenIds`, `homeScreenId`, `screens` |
| `pagerIndex(id)` | `screenIds.indexOf(id)` or home fallback |
| `WidgetHostCodec` | `v5`; v1–v4 migrate; corrupt → defaults |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty `homeScreenId` | First INBOX in `screenIds`; test migrate |
| Network timeout | N/A |
| Race | single DataStore key; no dual v4 migrate |
| Unhandled exceptions | decode `runCatching` → defaults |
| Persist pager index | persist `currentScreenId` + `homeScreenId` only |
## Tests

- Automated: yes — `WorkspaceModelTest`, `WidgetHostCodec` v4→v5 golden (2 pages + 4×5)

## Fallback validation

- Why tests are not feasible: N/A for IDs/codec. Live swipe is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Screen IDs are the identity. Feeds are not mixed into inbox. Hub shell exists even if section screens are stubs. `LauncherHome.kt` stays ≤300 via extract.

## Notes

- Apache-2.0 algorithms in Compose; L3 host for News/Inbox is `docs/features/workspace-pages.md`
- Nova #6 ships this sprint; #1–5 wait for Sprint 19–20
