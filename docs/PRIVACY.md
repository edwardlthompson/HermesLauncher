# Privacy Policy (Draft)

> Customize for your application. Required when collecting any user data.

## Data We Collect

| Data | Purpose | Lawful Basis | Retention |
|------|---------|--------------|-----------|
| App settings (theme, save-crashes toggle, feed prefs) | Feature functionality | Legitimate interest | Until the user clears app data |
| Notification metadata and, if granted, content + images | Local inbox / vault | Consent (notification access + per-app store toggles) | Until the user archives, wipes, or uninstalls |
| Installed-app labels/icons | App drawer, dock, search | Legitimate interest (launcher) | Until the package is uninstalled |
| Crash / bug / feature report (opt-in, user-reviewed) | Debugging and product planning | Consent | GitHub issue retention; app keeps at most one pending crash |

Notification bodies and images are stored **only** for packages the user has granted store-content / store-images. Revoking a grant stops new writes. Shade dismissal does not delete vault rows.

`QUERY_ALL_PACKAGES` is required so a launcher can list user-installed apps. It is not used to upload an app inventory.
## App update checks

- Release endpoint: GitHub Releases API (`/repos/OWNER/REPO/releases/latest`), once per 24 hours
- Not telemetry: User-Agent is product name and version only; no analytics identifiers
- Stored locally (device-only, not peer-synced): `last_check_at`, `last_seen_version`, `dismissed_version`
- Android Auto Backup excludes `gp_updates`; web uses `localStorage` (`gp.update.*`)
- Failed fetch, timeout, or unmatched installer assets stay silent
- Donate reminder is once per installed version after an update — not a daily nag

## Crash and feedback reports

- Filing requires a GitHub account (no email field in the app; no anonymous proxy on the default path)
- Payload may include: report kind, app version, OS family, exception type, sanitized stack, crash fingerprint, optional user text
- Payload must not include: email, name, user/device/advertising ids, IP, GPS, screenshots, logcat, cookies, tokens, `.env` values, or home-directory paths
- Capture is off by default. The user reviews sanitized markdown, then taps Open GitHub or Copy
- A self-hosted crash inbox (GlitchTip / Bugsink) is a child escape hatch only — do not enable without a DPIA
- Anonymous intake (GitHub App proxy) is a named follow-up — see [`CRASH_PROXY.md`](CRASH_PROXY.md) — and needs a new DPIA before enable

## Data We Do Not Collect

- No tracking without explicit opt-in
- No sale of personal data
- No PII in logs without user consent
- No install UUID “just for dedup”

## User Rights (GDPR / CCPA)

- **Access:** Users can request a copy of their data
- **Deletion:** Users can request data deletion
- **Opt-out:** Telemetry and analytics are opt-in only
- **Portability:** Export settings where technically feasible

## Data Minimization

- Collect only what each feature requires
- Use local-first storage where possible
- Anonymize or aggregate analytics data

## DPIA Checklist (`[HUMAN]`)

If processing EU personal data:

- 🔲 Document processing purpose and legal basis
- 🔲 Assess necessity and proportionality
- 🔲 Identify risks and mitigations
- 🔲 Record in `DECISION_LOG.md` or ADR

## Contact

Privacy inquiries: see maintainers in `.github/CODEOWNERS` or `SECURITY.md`.
