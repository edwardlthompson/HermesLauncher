# Feature: github-feedback

> Compose GitHub issue-form URLs, clipboard fallback, fail-soft duplicate search.

## Acceptance criteria

- ✅ User-visible behavior: small fields prefill `issues/new?template=...`; large bodies use clipboard + short URL
- ✅ Offline/error behavior: `OWNER/REPO` never hits the network; 403/timeout/placeholder return `[]`
- ✅ Accessibility: N/A (logic); Open GitHub is `https` only
- ✅ i18n: N/A in this container (copy lives in `feedback.*`)

## Smoke scenario

1. Given `release_repo` `acme/app` and fingerprint `a1b2c3d4e5f6`
2. When the composer builds a crash title
3. Then the title is `[crash] a1b2c3d4e5f6 TypeError` and a second search inside 60s does not fetch again

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| Logic | `examples/web/src/github-feedback/` | `examples/android/.../githubfeedback/` |
| Tests | `*.test.ts` | `src/test/.../githubfeedback/` |
| Wiring | none (Feedback UI calls this) |

## Definition of Done

See `docs/FEATURE_MODULES.md`. Fallback: `cd examples/web && npm test -- github-feedback`.

## Notes

- Own `isPlaceholderRepo` in this container (do not import About) so About add/remove still passes. Default feedback repo is `release_repo`
- Share the 60s cooldown with Open GitHub (Search API 10 req/min)
