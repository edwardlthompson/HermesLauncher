# AOSP homescreen (vendored)

Provenance:

- `launcher3/` from `platform/packages/apps/Launcher3` tag `android-14.0.0_r28` (Apache-2.0)
- `systemui/iconloaderlib` from `platform/frameworks/libs/systemui` same tag (Apache-2.0)

Hermes compiles the **without Quickstep** source set as a user-installable library.
Quickstep/recents stays in-tree but is not built: it requires a privileged system image.
Do not edit files under these trees except via AOSP tag upgrades.

User-app shims (not upstream):

- `BroadcastReceiver` registration uses `ContextCompat.RECEIVER_EXPORTED` (Android 13+).
- Private `androidprv` color attrs mapped to Material `?attr/color*`.
- `PreloadIconDrawable` HCT uses Material `Hct` instead of hidden `ColorUtils.M3HCT*`.
- `SimpleIconCache` lives in `:iconloader` compat (no `UserHandle.getIdentifier()`).
