# Feature: backup-opml

> Sprint 27. Nova #45–48 + RSS/Atom/podcast OPML export. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: SAF export/import JSON Hermes backup (screens, Hotseat, prefs, hidden apps, blacklist, feed URLs); OPML export of all feed URLs next to existing import; reset home layout confirm (does **not** wipe vault); Labs toggles; default-home reminder if not default
- 🔲 Offline/error behavior: invalid JSON → `BackupResult.Invalid` toast; missing widget provider skipped with count toast; empty feeds → valid empty OPML `<body/>`
- 🔲 Accessibility: export/import/reset labeled
- 🔲 i18n: existing `settings_export` / `settings_import` plus `backup_*`, `feed_export_opml`

## Smoke scenario

1. Given a backup whose widgets have foreign `appWidgetId`s
2. When import runs
3. Then new ids are allocated from `HermesAppWidgetHost` using `providerFlattened`; foreign ids are never written

## Container map

| Layer | Path |
|-------|------|
| Logic | `workspace/BackupCodec.kt` + `feeds/OpmlExporter.kt` |
| View | `ui/settings/BackupSettings.kt` + Feeds export button |
| Tests | JSON round-trip; OPML inverse of `OpmlParser`; remap never keeps foreign ids |
| Wiring | Backup hub row; Feeds section |

## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `RestoreDbTask` | allocate + bind from provider; skip missing |
| `LauncherBackupHelper` | JSON file via SAF, not XML favorites |
| default-home | `LivePermissions.homeRoleSettings()` |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty file | `BackupResult.Invalid` |
| Network timeout | N/A (SAF) |
| Race | backup mutex on host store |
| Unhandled exceptions | SAF `runCatching` |
| Stale `appWidgetId` | never persist imported ids; test fake provider |
| Reset vs vault | reset `screenIds` only |

## Tests

- Automated: yes — `OpmlExporterTest`, `BackupCodecTest`, remap test

## Fallback validation

- Why tests are not feasible: N/A for codecs. Live SAF is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Unused export/import strings are wired. OPML export ships. Widget restore remaps host ids.

## Notes

- Vault/SQLCipher is out of backup reset
