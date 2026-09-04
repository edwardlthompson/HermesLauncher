# Human Backlog

> Items automation attempted during autonomous `/build` but could not complete. BUILD_PLAN rows stay open until a human finishes them.

| Deferred | Sprint | Owner | Task | Reason |
|----------|--------|-------|------|--------|
| 2026-09-01 | Sprint 8 — Live home | ADB | Sideload OP12 (`b5214fc6` only); confirm overlay, wallpaper home, Add widget, dock assign | No automation rule for visual Home confirm (overlay, wallpaper, Add widget, dock assign) |
| 2026-09-01 | Sprint 12 — SQLCipher | ADB | Confirm granted vault rows survive migration or show rebuild copy | No automation rule for visual vault-migration confirm |
| 2026-09-01 | Ongoing Maintenance | HUMAN | Approve release tag when product-ready | No GitHub release; product judgment required |
| 2026-09-02 | Sprint 13 — Pixel widgets and grouped inbox | ADB | OP12 `b5214fc6` only: bind-deny, configure-cancel (no ghost), drag onto new page, visible width resize, group expand + per-item X + group X; logcat `HermesWidget` / `HermesVault` | No automation rule for ADB task in sprint Sprint 13 — Pixel widgets and grouped inbox |
| 2026-09-02 | Sprint 15 — Launcher3-look home chrome | ADB | OP12 `b5214fc6` only: empty long-press → Wallpaper / Widgets / Settings; wallpaper chooser; preview-card bind 2×2; four-handle resize; drop on Remove well; logcat `HermesWidget` | No automation rule for ADB task in sprint Sprint 15 — Launcher3-look home chrome |
| 2026-09-02 | Sprint 16 — Inbox views, dock usage, All Apps | ADB | OP12 `b5214fc6` only: unread badge; search close; dismissed history; category/time; usage dock after notification-open; All Apps rail; shortcuts if Home; widget search | No automation rule for ADB task in sprint Sprint 16 — Inbox views, dock usage, All Apps |
| 2026-09-02 | Sprint 17 — Icons, search, gestures, FOSS wallpapers | ADB | OP12 `b5214fc6` only: live widgets tick; All Apps fling; usage banner grant; prune not per-persist; unread dots; Home-again search; double-tap torch/lock; AOSP wallpaper chooser; Hermes Gradient/Clock live wallpaper | No automation rule for ADB task in sprint Sprint 17 — Icons, search, gestures, FOSS wallpapers |
| 2026-09-02 | Sprint 23 — Folders | ADB | OP12 `b5214fc6` only: open folder window; lid badge; fullscreen toggle | Folder window composables ship; desktop has no folder tiles yet so OP12 cannot open a lid. Settings fullscreen toggle is in Folders. |
| 2026-09-02 | Sprint 24 — Local search | ADB | OP12 `b5214fc6` only: contacts deny still searches apps+inbox; one-row cap; no web provider | Contacts stay empty without READ_CONTACTS; app-row cap and no-web settings ship. Live OP12 search chrome still needs a human pass. |
