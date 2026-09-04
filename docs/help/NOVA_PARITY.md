# Nova / Launcher3 parity backlog

Observed on OP12 (`b5214fc6`) from Nova Launcher settings on 2026-09-02. **Boarded** in `BUILD_PLAN.md` Sprints 18–27. Specs under `docs/features/`. Skip proprietary Nova Plus / Nova AI cloud.

Hermes Settings hub target: **Desktop, Dock, App drawer, Folders, Search, Look & feel, Gestures, Inbox, Feeds, Backup**.

| Nova # | Sprint | Spec |
|--------|--------|------|
| 6 + workspace IDs | 18 | `docs/features/workspace-screens.md` |
| 1–3, 9, 12 schema | 19 | `docs/features/desktop-cell-layout.md` |
| 4–5, 7–8, 10–11 | 20 | `docs/features/paged-view-motion.md` |
| 13–15, 17 (skip 16) | 21 | `docs/features/hotseat-dock.md` |
| 18–27 + notification blacklist | 22 | `docs/features/all-apps-drawer.md` |
| 28–32 | 23 | `docs/features/folders.md` |
| 33–36 local only | 24 | `docs/features/local-search.md` |
| 37–39 | 25 | `docs/features/launcher-gestures.md` |
| 40–44 | 26 | `docs/features/look-and-feel.md` |
| 45–48 + OPML export | 27 | `docs/features/backup-opml.md` |

## Skip (not FOSS / not Hermes)

- Nova #16 drawer handle (keep Apps + swipe-up All Apps)
- Nova AI, Nova Plus, Restore purchase, cloud insights
- Closed Google Discover / Now page as a required pane
- Web search

## Future features (source list)

Unchanged from the OP12 dump; implement via the sprint table above, not by picking numbers ad hoc.

### Desktop (Home screen)

1. Desktop grid independent of widget grid
2. Home icon size + labels
3. Desktop padding / page insets
4. Home scroll physics
5. Infinite scroll / wrap pages (Labs)
6. Page indicator
7. Widget overlap / overlap wallpaper (Labs)
8. Subgrid / overlapping widgets (Labs)
9. Card / sheet background behind icons
10. Home screen search bar placement
11. Pinch to overview / pinch to drawer
12. Home screen folders as first-class containers

### Dock

13. Dock pages / swipeable dock
14. Dock background, height, radius, draw behind nav bar
15. Dock icon size independent of desktop
16. **Skipped** — drawer handle
17. Hide dock until swipe

### App drawer

18–27 as in Sprint 22, plus ignore-notifications blacklist with X.

### Folders / Search / Gestures / Look / Backup

28–48 as in Sprints 23–27. Search is apps + inbox + local contacts + shortcuts; no web.
