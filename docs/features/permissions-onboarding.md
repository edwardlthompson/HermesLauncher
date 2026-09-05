# Feature: permissions-onboarding

> Inbox first-run overlay plus a Settings hub to restore grants. About holds version, Venmo, and feedback.

## Acceptance criteria

- ✅ Inbox overlay asks for notification access, battery, photos, usage, and feed alerts
- ✅ Settings → Permissions lists every grant with On or Fix (no Activity Result registry on HOME)
- ✅ Settings → About shows version, Venmo donate link, bug/feature feedback
- ✅ Long-press HOME menu includes About
- ✅ Empty Inbox / News / Podcasts rotate congratulations copy

## Smoke scenario

1. Given a fresh grant set, Inbox overlay or Settings → Permissions can open each Android screen
2. When a grant is already on, the row shows On
3. Then About opens Venmo from `assets/donations.json`

## Container map

| Layer | Path |
|-------|------|
| Logic | `oem/GrantCatalog.kt`, `oem/RepairPolicy`, `ui/inbox/ZeroCopy.kt` |
| View | `ui/settings/SettingsPermissions.kt`, `SettingsAbout.kt`, `ui/onboarding/` |
| Tests | `GrantCatalogTest.kt`, `RepairPolicyTest.kt`, `ZeroCopyTest.kt`, `SettingsHubTest.kt` |
| Wiring | `SettingsScreen` when-branches; `HermesLauncherActivity` About option |
## Tests

- Automated: yes — unit tests above
- Coverage: grant snapshot mapping + empty-copy rotation

## Fallback validation

- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
