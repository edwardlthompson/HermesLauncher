# ADR-0004: NotificationListener privacy and threat model

- **Status:** Accepted
- **Date:** 2026-09-01
- **Deciders:** HUMAN approved the Sprint 0 plan (2026-09-01)

## Context

Notification access is a high-privilege trust boundary. The vault can contain chat messages and images. OEM skins (especially Xiaomi/MIUI) reset the listener.

## Decision

- Listener is documented in `docs/THREAT_MODEL.md` (STRIDE + MASVS).
- `android:allowBackup="false"`; vault paths excluded from data extraction.
- Per-app store-content / store-images toggles. Default off.
- Permission loss: stop new writes, keep existing vault, show a repair banner with one-tap settings.
- Export and wipe live in Settings. No telemetry. GitHub Releases update checks stay as in the Golden Path About stub.
- `QUERY_ALL_PACKAGES` is declared for the launcher app list and explained in `docs/PRIVACY.md`.

## Consequences

- Onboarding copy must explain why access is needed and what is stored
- OEM guides are Sprint 6; the banner can ship with the listener
- Stolen unlocked-device risk remains until optional at-rest encryption lands

## Alternatives Considered

| Pattern | Rejected because |
|---------|------------------|
| Cloud vault sync | Explicit non-goal |
| Store everything, hide later | Violates grant/revoke rule |
| allowBackup=true with exclude only | OEM backup agents still leak |
