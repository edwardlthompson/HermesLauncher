# Product Specification

> Spec-driven development. Feature slices use `docs/features/{name}.md`.
> Status markers: open / done / blocked use BUILD_PLAN emoji markers.

## Overview

**Product:** Hermes Launcher
**Tagline:** Your notifications, news, and podcasts — as home.
**Purpose:** FOSS Android home-screen replacement whose primary page is a persistent notification inbox mixed with news feeds and podcasts.
**Users:** Privacy-conscious Android users, former Posidon users, people who treat a notification feed as home, FOSS/F-Droid users, OEM-skin users who struggle with notification access.

## Functional Requirements & User Stories

| ID | Story | Acceptance |
|----|-------|------------|
| FR-1 | As a user I set Hermes as Home so the inbox is my first screen | Activity declares `MAIN` + `HOME` + `DEFAULT`; system chooser lists Hermes |
| FR-2 | As a user I scroll a mixed feed of notifications, news, and podcasts | Page 0 is a vertical `LazyColumn`; cards never swipe-dismiss |
| FR-3 | As a user I swipe horizontally to extra widget pages | `HorizontalPager` changes page; feed cards do not consume horizontal swipe |
| FR-4 | As a user I dismiss a card only by tapping X | X has a 48dp target and content description; one item archived |
| FR-5 | As a user I keep chat text and images after the source app deletes them | Vault persists at post time when per-app store policy allows |
| FR-6 | As a user I search and filter live inbox plus history | Filters: All / Messages / News / System / Podcasts / Unread / Pinned / per-app |
| FR-7 | As a user I recover when OEM kills notification access | Banner + one-tap re-grant; no silent data collection after revoke |
| FR-8 | As a user I play a podcast episode from a feed card | Media3 mini-player; simple RSS enclosures only |
| FR-9 | As a user I customize dock, theme, and icon pack | DataStore prefs survive reboot; strings from `strings.xml` |

## Non-Functional Constraints

- MIT license; no proprietary SDKs (no Play Services, Firebase, or closed telemetry)
- File budgets: 300 lines static/UI+i18n, 150 lines pure logic
- `SOURCE_DATE_EPOCH` reproducible APKs; pinned Gradle wrapper
- User-visible copy only in `res/values/strings.xml`
- Local-only vault; no cloud sync
- Do not store notification content from apps the user has not granted (or later revoked)

## Non-goals

- Not a 1:1 clone of Posidon or Notifications Widget
- No swipe-to-dismiss or swipe-to-archive on cards
- Not a full email client or heavy podcast app
- No cloud sync of the vault

## Success metrics

- NotificationListener survives OEM battery optimizations with onboarding that names the OEM
- Horizontal swipe never dismisses a card
- Vault retains granted chat text + images after source-app deletion
- F-Droid-ready reproducible builds
- Compose `LazyColumn` only (no RecyclerView crash paths)

## Architecture & Data Flow

```mermaid
flowchart LR
  listener[NotificationListenerService]
  vault[(Room vault plus private images)]
  feed[Page0 feed]
  pager[HorizontalPager]
  widgets[Widget host pages]
  dock[Dock]
  drawer[App drawer]
  listener -->|"persist first"| vault
  vault --> feed
  feed --> pager
  pager --> widgets
  dock --> drawer
```

## Test-first rule

Every feature in `docs/plan.md` / BUILD_PLAN must list tests, or state why automation is not feasible and name the fallback command.
