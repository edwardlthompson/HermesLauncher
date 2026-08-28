# GitHub merge queue (optional)

This template does **not** turn on a merge queue. Classic branch protection plus `scripts/setup-github-repo.sh` is the FOSS default (see [`.github/settings.yml`](../.github/settings.yml)).

## When a child org might want one

Use a merge queue when several PRs target `main` at once and you need each to re-run required checks on a temporary merge commit before it lands. Personal repos and quiet template traffic do not need it.

Required check names come from [`.github/required-checks.json`](../.github/required-checks.json) (a name manifest, not a workflow content-hash cache) and must stay the same as [`.github/settings.yml`](../.github/settings.yml) / `verify-branch-protection.sh`:

- CI
- Security Scan
- CodeQL
- Repo Hygiene
- Feature Gate
- Template Upgrade Simulation (Windows)

## Enable (HUMAN, org repos)

1. Settings → Rules → Rulesets (or Branches → Add classic protection) → **Merge queue**.
2. Require the six checks above. Keep **force pushes** off.
3. Do not add the Settings GitHub App as a hard dependency. `setup-github-repo.sh` remains the apply script if you stay on classic protection.

## Honesty

| Claim | True here? |
|-------|------------|
| Merge queue on by default | No |
| Required checks listed | Yes, in `.github/settings.yml` |
| `/push` uses the queue | No — `/push` is a local commit + `git push` after [HUMAN] |

Related: [`docs/SECURITY_TRIAGE.md`](SECURITY_TRIAGE.md), [`docs/PACKAGE_ATTESTATION.md`](PACKAGE_ATTESTATION.md).
