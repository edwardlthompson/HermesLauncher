# Feature: look-and-feel

> Sprint 26. Nova #40–44. `DeviceProfile` look. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: adaptive icon shape + contact photos; wallpaper-extracted palette (`WallpaperColors`); night schedule beyond system; dots vs counts + badge color; label shadow + label color; icon packs stay
- 🔲 Offline/error behavior: no wallpaper colors → theme palette; contact photo miss → app icon; schedule invalid → follow system
- 🔲 Accessibility: contrast for badge and labels
- 🔲 i18n: `look_*`

## Smoke scenario

1. Given system dark and a night schedule that includes now
2. When theme resolves
3. Then dark is used even if “follow system” would be light (schedule wins when enabled)

## Container map

| Layer | Path |
|-------|------|
| Logic | `ui/theme/` + `IconShape` |
| View | `ui/settings/LookSettings.kt` |
| Tests | schedule window + shape enum |
| Wiring | Look & feel section |

## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `IconShape` / `AdaptiveIconDrawable` | clip |
| `WallpaperColors` | palette (no Play Services) |
| `DotInfo` | dots vs counts + color |
| `DeviceProfile` | label paint |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty colors | fallback Material scheme |
| Network timeout | N/A |
| Race | theme DataStore |
| Unhandled exceptions | `WallpaperColors` miss → system |

## Tests

- Automated: yes — schedule + shape parse

## Fallback validation

- Why tests are not feasible: N/A for parse. Live wallpaper extract is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Packs still apply. Palette is AOSP `WallpaperColors` only.

## Notes

- No Pixel themed-icon proprietary APIs
