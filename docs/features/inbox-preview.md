# Feature: inbox-preview

> Ignore ongoing notifications; persist rich preview photos in app-private files. One-shot Photos permission plus a global keep-photos setting.

## Acceptance criteria

- User-visible behavior: Settings toggle skips persistent/ongoing posts; inbox cards show full-frame BigPicture/large-icon/MessagingStyle photos plus big text and subtext; every app is kept without a per-app grant sheet
- Offline/error behavior: photos write under `filesDir/vault/images/`; URI read failures keep text; `READ_MEDIA_IMAGES` (or storage on API 32) is requested once from setup/Settings, not per app
- Accessibility: preview image has a content description; ignore-ongoing and keep-photos switches are labeled
- i18n: keys under `grant_*`, `settings_ignore_ongoing*`, `settings_store_photos*`, `inbox_preview`, `home_setup_photos`, `home_setup_media`

## Smoke scenario

1. Given notification access is on, Photos is granted once, and an app is Keep-granted
2. When a MessagingStyle or BigPicture notification posts on OP12 (`b5214fc6`)
3. Then the card shows the full photo (Fit, not Crop) from Hermes private storage; wallpaper remains visible around cards; ongoing media/VPN posts are skipped when the toggle is on

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/vault/` |
| View | `ui/inbox/`, `ui/settings/`, `ui/onboarding/` |
| Tests | `src/test/.../vault/` |
| Wiring | `HermesApplication`, `VaultRepository`, Settings switches |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `PostedNotification.ongoing` | from `StatusBarNotification.isOngoing` |
| `VaultMapper.decide(..., ignoreOngoing, storePhotos)` | `SKIP` when ongoing and ignore is on; images when `storePhotos` or `policy.storeImages` |
| `VaultPreview` | extras JSON: subText, bigText, infoText, summaryText, imageRef |
| `VaultImageStore` | JPEG under `ImageLimits.RELATIVE_DIR`; no MediaStore |
| `InboxPrefs.ignoreOngoing` | DataStore, default **true** |
| `InboxPrefs.storePhotos` | DataStore, default **true** |

## Tests

Automated: yes — `VaultMapperTest` ongoing skip + global photos, `VaultPreviewTest`, `VaultImageStoreTest`, `InboxPrefsTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix` locally; `adb -s b5214fc6 logcat -s HermesVault:D` after sideload

Why tests are not feasible: N/A for mapper/store. Live photo URI is OP12 smoke.

## Definition of Done

Toggle, private JPEG persist, full-frame card, one-shot Photos grant, OP12 logcat.

## Notes

NotificationListener already receives bitmaps. Photos/storage is a one-shot OS grant so messaging `content://` URIs can be read without a per-app Gallery prompt.
