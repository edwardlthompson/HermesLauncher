# Feature: crash-stability

> Sprint 50. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ Feed refresh no longer OOMs while encoding the article catalog into DataStore
- ✅ Desktop widgets survive process death / force-stop (grid pin + no hard-delete of out-of-bounds widgets)
- ✅ Inbox scroll does not snap mid-fling; card images decode off the UI thread with subsampling
- ✅ Offline reader: body from `FeedFull` cache or open-in-browser when RSS html is not persisted
- ✅ Accessibility: inbox cards still expose dismiss/open actions when image decode fails
- ✅ i18n: no new user-facing strings required for this stability slice

## Smoke scenario

1. _Given_ Hermes is HOME on OP13 with many feed subscriptions
2. _When_ News page opens / refresh runs and the process is force-stopped
3. _Then_ no `ArticleCodec.encode` OOM in dropbox, and desktop widget count is unchanged after relaunch

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/ArticleCodec`, `FeedRefreshGate`, `FeedPersistPolicy`, `l3/DesktopReflow`, `ui/inbox/InboxImageDecode` |
| View | `InboxCard`, `InboxStick`, `InboxFeed`/`InboxGroup`, `HermesWorkspacePages` |
| Tests | co-located `*Test.kt` under `app/src/test` |
| Wiring | `HermesApplication.onCreate` grid pin ≤10 lines |

## Tests

- Automated: yes — `ArticleCodecTest`, `FeedRefreshGateTest`, `FeedPersistPolicyTest`, `DesktopReflowTest`, `InboxStickPolicyTest`, `InboxImageDecodeTest`
- Coverage: pure logic plus refresh/persist and stick/decode paths

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`
- Device: `adb install -r` of CI/GitHub-signed APK; `am force-stop` widget survival (never uninstall)

## Definition of Done

See `docs/FEATURE_MODULES.md` and BUILD_PLAN Sprint 50 rows.

## Notes

- Compose `MainActivity` widget host uses host id `2048`; Launcher3 HOME stays on `1024`
- Article DataStore omits `html`; full bodies live in `FeedFull` files
