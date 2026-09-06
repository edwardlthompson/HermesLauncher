# Feature: inbox-chrome

> Sprint 48. Notification ignore list, even card height, photo quality, recency-ranked app search, GitHub APK. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Inbox settings lists ignored notification apps; search ranks recently opened apps first as each letter is typed; X restores the app
- ✅ User-visible behavior: Even notification height trims long bodies to a chosen character cap so cards stay the same size
- ✅ User-visible behavior: Hide small notification photos skips avatars (`largeIcon`) and images below 240px
- ✅ User-visible behavior: GitHub Releases attach `hermes-launcher-{versionName}-foss.apk`
- ✅ Offline/error behavior: blank package ignored; missing image dimensions with the photo filter on stores text only; missing release APK does not crash About
- ✅ Accessibility: blacklist X uses “Stop ignoring {app}”; switches use visible labels
- ✅ i18n: `blacklist_*`, `inbox_truncate*`, `inbox_hide_small_images*`

## Smoke scenario

1. Given FairEmail is on the ignore list
2. When a FairEmail notification posts
3. Then the vault skips persist and the inbox hides existing FairEmail cards until X restores the app

## Container map

| Layer | Path |
|-------|------|
| Logic | `vault/InboxDisplay`, `vault/InboxFilter`, `icons/AppSearch`, `icons/LaunchRecency` |
| View | `ui/settings/BlacklistSettings`, `ui/settings/InboxCardSettings`, `ui/inbox/InboxCard` |
| Tests | `InboxDisplayTest`, `InboxFilterTest`, `LaunchRecencyTest`, `VaultMapperTest`, `test_release_please_hygiene` |
| Wiring | Inbox settings; `AppCatalog.launch`; L3 `DefaultAppSearchAlgorithm.sLastUsedMs` |

## Tests

- Automated: yes — ignore filter, truncate, image gate, recency rank, release workflow APK upload

## Fallback validation

- Why tests are not feasible: N/A for mapper/search. Live ignore-list is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Blacklisted packages do not appear in the inbox. Truncate and min-photo toggles persist. All Apps search ranks recency letter by letter. Release workflow uploads a FOSS APK.

## Notes

- Hide-from-drawer stays in App drawer settings and is not the notification ignore list
- `largeIcon` is treated as an avatar when Hide small photos is on
