# ADR-0003: Paging and AppWidgetHost

- **Status:** Accepted
- **Date:** 2026-09-01
- **Deciders:** HUMAN approved the Sprint 0 plan (2026-09-01)

## Context

Posidon was one page only. Hermes must swipe to extra widget screens without treating horizontal motion as dismiss.

## Decision

Root chrome uses Compose `HorizontalPager`.

- Page 0: vertical `LazyColumn` feed. Cards must not use `SwipeToDismiss` or any horizontal dismiss gesture.
- Pages 1+: placeholder in Sprint 1; later `AndroidView` hosting `AppWidgetHost` / `AppWidgetHostView`.
- Nested scroll: vertical deltas belong to the feed; horizontal deltas belong to the pager.
- Dock stays visible (or auto-hides later) below the pager. Swipe-up opens the drawer overlay.

Sprint 1 ships two empty widget pages so the swipe contract is testable without bind/resize.

## Consequences

- Swipe-contract unit tests are required in Sprint 1
- Widget bind/resize/persist is a later feature slice, not part of the shell lock
- RecyclerView / ViewPager2 are forbidden

## Alternatives Considered

| Pattern | Rejected because |
|---------|------------------|
| ViewPager2 + RecyclerView | Posidon-era crash class |
| Per-card horizontal gesture | Steals page swipe and invites accidental dismiss |
| Navigation Compose as the page surface | Pager is the product gesture, not a back stack |
