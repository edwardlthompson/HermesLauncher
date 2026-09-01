# ADR-0001: Original Compose launcher on Golden Path

- **Status:** Accepted
- **Date:** 2026-09-01
- **Deciders:** HUMAN approved the Sprint 0 plan (2026-09-01) and asked the agent to automate remaining HUMAN items

## Context

Hermes needs a unique FOSS home-screen replacement that keeps every agent-project-bootstrap invariant (FOSS-only, file budgets, strings.xml, reproducible builds). Forking an existing launcher would import a different architecture and license surface.

## Decision

Evolve `examples/android/` in place. Package `org.hermeslauncher.app`. Keep About, theme, DataStore, crash-capture, insets, and the thin `MainActivity`. Replace the greeting `HermesScreen` with a launcher shell. Use Clean + MVI for feed/vault; Compose state for chrome. Manual composition root — no Hilt in Sprint 1.

Theme/scaffold names: `HermesTheme`, `HermesScaffold`.

## Consequences

- Schema and shared types stay Sequential-only
- Scripts that hardcoded `dev.foss.goldenpath` must stay in lockstep with the package
- Hilt is a later DECISION_LOG revisit if the composition root exceeds about ten wires

## Alternatives Considered

| Pattern | Rejected because |
|---------|------------------|
| Fork Lawnchair / KISS / other FOSS launchers | Not inbox-first; license and architecture entanglement |
| Copy AetherFeed domain / SqlCipher | Wrong product; news/booru client, not a launcher |
| Hilt in Sprint 1 | Extra graph for a stub shell; template has no Hilt |
