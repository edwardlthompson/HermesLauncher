# Feature: vault-encryption

> Sprint 12. SQLCipher at rest after OP12 vault smoke.

## Acceptance criteria

- User-visible behavior: vault opens after upgrade; if the existing file cannot be unlocked, Hermes wipes and recreates an on-disk SQLCipher database; in-memory rebuild copy is last resort
- Offline/error behavior: wrong/corrupt key does not crash Home; wipe-and-regrant copy is stringed
- Accessibility: rebuild dialog actions are labeled
- i18n: keys under `vault_crypto_*`

## Smoke scenario

1. Given plaintext rows from Sprint 8
2. When SQLCipher migration runs
3. Then granted rows are readable or the rebuild copy is shown

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/vault/` |
| View | `ui/onboarding/` rebuild dialog |
| Tests | `src/test/.../vault/` |
| Wiring | `VaultRepository` database open |

## Tests

Automated: yes — passphrase helper unit tests. Full SQLCipher open is Robolectric if the AAR loads; otherwise `assembleDebug` + ADB.

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A for helpers; device confirms migration.

## Definition of Done

Keystore-backed passphrase; documented wipe-on-failure; FOSS SQLCipher only.

## Notes

- Parallel exception: migrates shared `VaultDatabase`
