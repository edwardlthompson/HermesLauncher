# Privacy Policy (Draft)

> Customize for your application. Required when collecting any user data.

## Data We Collect

| Data | Purpose | Lawful Basis | Retention |
|------|---------|--------------|-----------|
| _Example: app settings_ | _Feature functionality_ | _Legitimate interest_ | _Until user deletes_ |
| _Example: crash logs (opt-in)_ | _Debugging_ | _Consent_ | _90 days_ |
## App update checks

- Release endpoint: GitHub Releases API (`/repos/OWNER/REPO/releases/latest`), once per 24 hours
- Not telemetry: User-Agent is product name and version only; no analytics identifiers
- Stored locally (device-only, not peer-synced): `last_check_at`, `last_seen_version`, `dismissed_version`
- Android Auto Backup excludes `gp_updates`; web uses `localStorage` (`gp.update.*`)
- Failed fetch, timeout, or unmatched installer assets stay silent
- Donate reminder is once per installed version after an update — not a daily nag

## Data We Do Not Collect

- No tracking without explicit opt-in
- No sale of personal data
- No PII in logs without user consent

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
