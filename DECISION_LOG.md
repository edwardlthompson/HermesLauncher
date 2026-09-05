# Decision Log

> Chronological register of major technical trade-offs, accepted architectures, and rejected alternatives.
> **Treat past entries as immutable history; append only.**

## Format

```markdown
### YYYY-MM-DD — [Title]
- **Status:** Accepted | Rejected | Superseded
- **Context:** ...
- **Decision:** ...
- **Alternatives considered:** ...
- **Consequences:** ...

```

## Entries

### 2026-09-05 — Ship tag v1.0.0 while APK is 1.0.1
- **Status:** Accepted
- **Context:** Release Please manifest and `.template-version` are 1.0.0. No git tag existed, so RP prepared `chore(main): release 1.0.0`. Actions could not open the PR. The product APK is versionName 1.0.1.
- **Decision:** Open and merge [PR #5](https://github.com/edwardlthompson/HermesLauncher/pull/5), then push tag `v1.0.0` and publish the GitHub Release so SBOM assets attach. Do not retag as 1.0.1 in this ship.
- **Alternatives considered:** Wait for the Actions PR permission setting (rejected: blocks `/ship`). Tag only `v1.0.1` (rejected: would fail the tag-gate vs `.template-version` 1.0.0).
- **Consequences:** Next RP cycle can bump past 1.0.0. Enable “Allow GitHub Actions to create and approve pull requests” so RP can open PRs unattended (KB-021).

### 2026-09-05 — TIME_TICK on Launcher3 HOME
- **Status:** Accepted
- **Context:** Compose `WidgetHostTick` lived on unused `MainActivity`. Application `startListening()` on host 1024 stole Launcher3 callbacks. Desktop clocks stayed frozen.
- **Decision:** `L3WidgetTick` registers `ACTION_TIME_TICK` on `HermesLauncherActivity`. Merge L3 + leftover Compose ids. Application no longer calls `startListening()`. News unread honors `onAppWidgetOptionsChanged`.
- **Alternatives considered:** Keep tick only in Compose HOME (rejected: HOME is Launcher3). Share host 1024 from Application (rejected: steals callbacks).
- **Consequences:** OP12 Fossify analogue clock advanced 07:59→08:00 with `HermesWidget: tick n=1`. Nova-bound widgets are not migrated.

### 2026-09-04 — Podcasts page left of News
- **Status:** Accepted
- **Context:** Inoreader OPML mixes articles and podcasts; AntennaPod-parity play must not restore `MainActivity` as HOME.
- **Decision:** Reserved screen `-300` prepends Podcasts. `FeedSub.kind` plus `PodcastDetect` split News vs Podcasts. OPML I/O is kind-filtered. `PodcastService` owns MediaSession/`mediaPlayback`; ExoPlayer stays Application-scoped. Sony duplicate stays News.
- **Alternatives considered:** Dump ~70 URLs in `DefaultFeeds` (rejected: token/line limits). Trust Inoreader folder only (rejected: PhotoNetCast / Sony). Chromecast/FCM (rejected: FOSS).
- **Consequences:** Home index fallback is Inbox page 2 when three reserved pages exist. Prefetch HTML stays off for podcast subs. Audio cache is `files/podcast-audio/`.

### 2026-09-04 — News and Inbox are Workspace pages
- **Status:** Accepted (supersedes 2026-09-04 minus-one overlay)
- **Context:** The DragLayer overlay felt like a second activity. Pre-merge screens already treated feeds as a real page; Home is Inbox.
- **Decision:** Overlay `launcher.xml` so `@id/workspace` is `HermesWorkspace`. Reserved IDs `-301`/`-302` host News then Inbox Compose. `moveToDefaultScreen` snaps to Inbox. Long-press Settings is `HermesSettingsActivity`. Stock empty `LauncherOverlayManager`.
- **Alternatives considered:** Restore `MainActivity` as HOME (rejected: third renderer). Keep overlay (rejected: dual pager/physics).
- **Consequences:** Desktop/Dock/Drawer/Labs DataStore knobs still do not restyle L3 DeviceProfile. Wrap/overlap Labs stay no-ops until a real L3 fork.

### 2026-09-04 — Minus-one overlay paints News then Inbox
- **Status:** Accepted
- **Context:** Sprint 28 overlay hook had no UI. OP12 smoke needed swipeable News/Inbox plus RSS add from androidauthority.com.
- **Decision:** Host Compose behind `LauncherRootView` so DragLayer translation reveals it. Page 0 is News, page 1 is Inbox. RSS fetch sends a User-Agent, tries `/feed/`, and XML feature flags must not abort parse on Android's DocumentBuilder.
- **Alternatives considered:** Extra Workspace CellLayouts (deferred). Rubber-band overlay with no settle (rejected: pages would not stay open).
- **Consequences:** FilterBar BackHandler needs `OverlayBackHost` because `Launcher` is not a ComponentActivity. Overlay View stays INVISIBLE at progress 0 so it does not steal a11y. AOSP `mapToRange: range has 0 length` spam during overlay animation is leftover Launcher3 indicator math.

### 2026-09-04 — AOSP Launcher3 is the homescreen
- **Status:** Accepted (supersedes 2026-09-03 “Keep Compose; do not vendor Launcher3”)
- **Context:** Compose widget drag-drop still failed on OP12 after the coord/toast patch. HUMAN directed that homescreen chrome must come from AOSP Launcher3; only the notification inbox and news feed stay custom.
- **Decision:** Vendor `packages/apps/Launcher3` tag `android-14.0.0_r28` (Apache-2.0) plus `iconloaderlib`. Compile the **without Quickstep** source set as Gradle modules `:iconloader` and `:launcher3`. `HermesLauncherActivity` extends `com.android.launcher3.Launcher`. Inbox/news attach via `LauncherOverlayManager` (minus-one). Quickstep/recents is in-tree but not built (privileged system image).
- **Alternatives considered:** More Compose DnD patches (rejected by HUMAN). Full Quickstep (rejected: needs priv-app / system signature). Submodule (rejected: repo policy prefers vendoring).
- **Consequences:** User-app shims: exported `BroadcastReceiver` flags, Material color attrs instead of `androidprv`, HCT via Material `Hct`, `UserHandle.hashCode` in `SimpleIconCache`. Set Hermes as default Home to exercise AOSP widget DnD.

### 2026-09-03 — Keep Compose; do not vendor Launcher3 for widget DnD
- **Status:** Superseded (2026-09-04 AOSP Launcher3 homescreen)
- **Context:** Widget drag-drop failed silently on OP12. Forking AOSP Launcher3 (Apache-2.0) would be license-OK but a second ~100k LOC View launcher colliding with Compose `WorkspacePager` / `DesktopPage` / `WidgetHostController`. Docs already rejected vendoring Launcher3 Java.
- **Decision:** Keep the Compose host. Capture window coordinates at long-press, then accumulate `dragAmount` so picker collapse cannot poison `localToWindow`. Toast + `HermesWidget` log on wrong page, missing grid, occupancy miss, or off-grid drop. High-contrast Option A is the adaptive launcher icon; temple render stays brand art only.
- **Alternatives considered:** Vendoring `packages/apps/Launcher3` (rejected: second codebase). Rewriting drawer/folders/gestures onto Views (rejected). Shipping temple art as `ic_launcher` (rejected: too busy for adaptive).
- **Consequences:** Reopen a Launcher3 View host only if OP12 still cannot bind a widget after this patch (tap or drag). Live check is ADB: long-press empty desktop → Widgets; tap a preview or long-press-drag onto the grid.

### 2026-09-01 — HOME-again search, dots, FOSS live wallpaper
- **Status:** Accepted
- **Context:** Inbox is the real notification list; dock/All Apps need a glanceable unread count. Home again should launch apps quickly. Nova-style double-tap and a Google-free wallpaper path were requested.
- **Decision:** Dots count visible vault unread per package. Extra HOME while already on inbox opens a usage+inbox overlay (`onNewIntent`). Double-tap lock uses Device Admin `lockNow()`, not Accessibility. Wallpaper never `setPackage` Google; prefer AOSP/Lineage if resolved; ship MIT Gradient and Clock `WallpaperService` engines.
- **Alternatives considered:** OEM badge APIs (rejected: not FOSS-portable). Accessibility lock (rejected). Vendoring AOSP WallpaperPicker Java (rejected: keep Compose + in-tree engines).
- **Consequences:** Sprint 17. CAMERA optional for torch. Device admin prompt on first lock. SQLCipher pool stays one writer; prune is throttled.

### 2026-09-01 — Dock ranks usage, not launches
- **Status:** Accepted
- **Context:** Opening an app from a Hermes notification is not a launcher launch. Launch-count ranking would ignore the inbox's main open path.
- **Decision:** Rank the dock by last 7 days `UsageStats.lastTimeUsed`, tie-break `totalTimeInForeground`. No Hermes-only launch counter. `PACKAGE_USAGE_STATS` + Usage access settings; denied keeps seeded order. Custom mode stores slots (codec v2).
- **Alternatives considered:** Launch count (rejected). Hermes-only tap counter (rejected: duplicates OS usage and misses notification opens' foreground time).
- **Consequences:** Sprint 16. OP12 smoke: open from a notification, wait, return — usage dock includes it.

### 2026-09-01 — Inbox categories from ApplicationInfo
- **Status:** Accepted
- **Context:** Play Store category pages are not on-device. Scraping play.google.com is ToS, network, and not FOSS.
- **Decision:** Group Category view by `ApplicationInfo.category` (`android:appCategory`) ints 0–8, else Other. `MixPolicy.merge` is the Time layout.
- **Alternatives considered:** Play HTML scrape (rejected). Manual user tags (deferred).
- **Consequences:** Missing/undefined category → Other. No PackageManager in unit tests (fake ints).

### 2026-09-01 — Launcher3-look home chrome (Compose)
- **Status:** Accepted
- **Context:** Pixel-style occupancy was in, but add/resize/remove chrome was a text list and two square handles. Lawnchair/Kvaesitso/Posidon are GPL. AOSP Launcher3 is Apache-2.0.
- **Decision:** Restyle in Hermes Compose after Launcher3 interaction (OptionsPopup, `ACTION_SET_WALLPAPER` without a Google package, preview cells, four-handle frame, top Remove well, grouped settings). Occupancy/bind stay in Hermes. No Launcher3 Java in tree.
- **Alternatives considered:** Vendoring Launcher3 (rejected: fork). Pinning Pixel wallpaper picker (rejected: not FOSS). PreferenceFragment XML (rejected: Compose settings already exist).
- **Consequences:** Sprint 15 AGENT slice. OP12 `[ADB]` smoke for popup, wallpaper chooser, preview bind, handles, Remove well.

### 2026-09-01 — Pixel widget grid and grouped inbox
- **Status:** Accepted
- **Context:** Tap-to-list picker and flat inbox cards were not a home-screen host. Lawnchair/Launcher3/Kvaesitso/Posidon are GPL or AOSP-mix.
- **Decision:** Implement Pixel interaction in Hermes Compose (`WidgetGrid` 4x5, codec v3, drop after occupancy, trailing empty page cap 20). Inbox stacks by sending app after `InboxFilter.apply`. Inspiration-only; no third-party launcher sources.
- **Alternatives considered:** Vendoring Launcher3/Kvaesitso (license). System `ACTION_APPWIDGET_PICK` (no drag preview). MixPolicy interleave inside app stacks (rejected: feeds list below groups).
- **Consequences:** v2 layouts restack via `stackedFromV2`. Page 0 stays inbox. Full drag/bind/resize gestures remain an OP12 `[ADB]` checklist.

### 2026-09-01 — Pruned-child About gate skip
- **Status:** Accepted
- **Context:** `/build` local `pre-release-gate.sh --local` failed `--strict` about-feature-gate after 180s because `verify-about-feature-gate.sh` still launched `--stack web` on an android-only child.
- **Decision:** Exit 0 with SKIP when no Golden Path About slices exist (web/node/python/rust/go). `check-build-plan-parallel` skips sprints whose body says they are archived in `COMPLETED_TASKS.md`.
- **Alternatives considered:** Raising `FEATURE_GATE_TIMEOUT_about` (hides the prune bug). Restoring template web stacks (wrong for Hermes).
- **Consequences:** Child android pre-release can pass locally. Origin CI on `c2bb2c4` stays red until a `[HUMAN]` push. Weekly AUTO rows for GitHub CI stay ❌ this week.

### 2026-09-01 — Hermes child seed and architecture ADRs
- **Status:** Accepted
- **Context:** Empty `HermesLauncher` workspace; HUMAN approved the Plan Mode architecture and asked the agent to automate HUMAN items (GitHub repo, Dependabot, ADR sign-off, BATCH_COMMANDS bookmark).
- **Decision:** Seed from vanilla agent-project-bootstrap (not AetherFeed). MIT. Package `org.hermeslauncher.app`. Original Compose launcher. ADRs 0001-0004 accepted. SQLCipher and Hilt deferred.
- **Alternatives considered:** GPL-3.0 (HUMAN chose MIT). Fork an existing launcher (rejected: not inbox-first).
- **Consequences:** Sprint 1 may implement the launcher shell. Listener/Room wait for Sprint 2. `template` remote is the local bootstrap clone only.

### 2026-08-28 — First stable v1.0.0
- **Status:** Accepted
- **Context:** Template had shipped through 0.25.0. Cloud agent `/build --lane auto` prepared `Release-As: 1.0.0` on PR #81 so Release Please would not cut 0.26.0. Upgrade-sim failed until stack-specific gates skipped after prune.
- **Decision:** Merge #81 after CI (including both upgrade-sims) was green, then merge RP #82 as v1.0.0. Keep remaining HUMAN/ADB leftovers (CII, Ollama, Android SDK) off the AGENT board.
- **Alternatives considered:** Ship 0.26.0 first (rejected: human asked for v1). Squash #81 (rejected: would drop the `Release-As` footer).
- **Consequences:** `.template-version` is 1.0.0. Child upgrades from 0.x should treat this as a major. Recurring weekly AUTO and CII/Ollama/SDK stay 🔲.

### 2026-08-28 — /build uses auto lane on the template
- **Status:** Accepted
- **Context:** Slack `/build` (bare word `build`) hardcoded `--lane child`, so the next row was Sprint 0 `init-project.sh` on this template.
- **Decision:** `/build`, `AGENTS.md`, and `docs/FOR_AGENTS.md` use `--lane auto`. On this template auto prefers Template Maintainer 🔲 AGENT rows (M46); child repos still walk the playbook. Do not run `init-project.sh` here.
- **Alternatives considered:** Run child Sprint 0 on the template (rejected: destroys template branding). Idle-exit without maintenance (rejected: M46 AGENT rows are the real next work).
- **Consequences:** M46 remaining rows stay 🔲. HUMAN leftovers remain (Ollama, DPIA, mcp.json, Dependabot, Codeowners, product smoke, Android SDK). Skipped bogus `codeql-action@vcodeql-bundle-*` tag from upd.

### 2026-08-27 — /build scoped feature-gate
- **Status:** Accepted
- **Context:** `/build` re-ran all Golden Path stacks after every AGENT row. Android weight 2 plus three RAM-capped slots made a docs-only or web-only row take several minutes.
- **Decision:** `gate_scope.py` maps git-dirty paths to `docs` (preamble only), `stacks` (touched `examples/{name}/`), or `full` (scripts/schemas/modules/shared). `/build` `/feature` `/fix` pass `--scope auto`. Autofix retries the failed stack with `--skip-preamble`. `/gates` and `/prerelease` stay full. `FEATURE_GATE_ONLY` filters the multi-stack parent.
- **Alternatives considered:** Always skip Android locally (rejected: still required when android files change). Default watch-agent-gates to auto (rejected: `/prerelease` must not silently skip stacks).
- **Consequences:** Shared script edits still run every stack. Sprint wrap-up `/gates` remains the honesty backstop.

### 2026-08-27 — /build M46 Golden Path + agent UX (rows 9–22)
- **Status:** Accepted
- **Context:** Continued `/build` on M46 after P0/schemas/Node/Python About. Android sanitizer JVM tests cannot use unmocked `org.json.JSONObject`.
- **Decision:** Robolectric for `SanitizeReportTest`. `verify-about-feature-gate` strips rust/go/node/python About via `about_lego_cli.py`. Feature specs require Tests + Fallback validation. Web↔Android i18n parity. Playwright RTL/reduced-motion/keyboard. WCAG token contrast (dark primary `#D3304E`). CSP on Vite preview + production meta. PWA share-target. UnifiedPush FOSS hook (no FCM). Settings JSON export. Lightroom `LrExportServiceProvider`. `/ideas` refuses silent `do all`; `/build` gate-locks the next feature.
- **Alternatives considered:** JSONObject on plain JVM (rejected: Android stub). CSP meta in source `index.html` (rejected: breaks Vite HMR). UnifiedPush connector Maven dep (deferred: hook + gradle ban until a distributor is in-tree).
- **Consequences:** Next `/build` row is M46-24 (debug recipe + `last-feature-gate.json` + 3-strike). Go About-without is skipped locally when `go` is missing (CI still runs it). HUMAN Scorecard/CII/DPIA remain after AGENT work.

### 2026-08-27 — /build M46 P0 + shared schemas
- **Status:** Accepted
- **Context:** `/build` on the M46 `/allideas` board. P0 was force-push honesty, Windows tool PATH, WSL1 bash, Sacred upgrade sim, plugin version.
- **Decision:** Deny `git push --force` even when session approved `git push`. Prepend `go`/`cargo` in `resolve-tools.sh`. Skip `System32` bash case-insensitively. Upgrade sim cherry-picks AREAS then asserts a child `AGENTS.md` marker. Plugin pack version follows `.template-version`. Shared Golden Path JSON schemas live under `schemas/golden-path/`.
- **Alternatives considered:** Allow force-push when `git push --force` is listed in session state (rejected for P0: `/push` never grants force). jsonschema PyPI dep (rejected: stdlib contract tests).
- **Consequences:** Next `/build` row is Node About + crash sanitize. HUMAN leftovers (Ollama, DPIA, CII, Scorecard) stay after AGENT work.

### 2026-08-27 — /allideas + M46 board
- **Status:** Accepted
- **Context:** `/ideas` caps at 5–8 (20 when asked). Sizeable automated progress needs an uncapped dump that can fill BUILD_PLAN, then `/build` on this template.
- **Decision:** Add `/allideas` (`allideas.md`, bare word `allideas`, `/AllIdeas` alias) with a `docs/help/ALLIDEAS.md` twin. Put the 2026-08-27 dump on the Template Maintainer board as M46. `/build --lane auto` prefers those 🔲 AGENT rows over weekly AUTO. Crash-proxy DPIA stays on M43; Scorecard badge and CII stay HUMAN.
- **Alternatives considered:** Raise `/ideas` cap (rejected: keeps a short ranked menu). Filename `all-ideas.md` (rejected: bare-word triggers are one word with no punctuation).
- **Consequences:** Next `/build` on this repo executes M46-1 (deny `git push --force`). Do not implement `/allideas` dumps until the user names numbers or says `board`.

### 2026-08-27 — /ideas round 2 (M45)
- **Status:** Accepted
- **Context:** `/ideas` asked for 20 in-scope items after M44. Health still showed closed RP #80 CI. Crash-proxy remains a DPIA item.
- **Decision:** Filter health CI off `release-please--branches--*`. Init installs commit-msg + sandbox copy. `--quick` runs action-ref format (API resolve stays full). Local Gradle patch apply via `UPDATE_GRADLE_PINS`. Rust/Go About+crash sanitize without extra crates. F-Droid and Lightroom in feature-gate. Plugin pack + Winget stub in CI. Radar writes gitignored AGENT suggestions. Encoding fail-closed is opt-in. Crash proxy stays disabled (`docs/CRASH_PROXY.md`).
- **Alternatives considered:** Live GitHub App proxy now (rejected: DPIA). `regex` crate on Rust Golden Path (rejected: keep zero-dep). Full GitHub API action resolve in `--quick` (rejected: needs network).
- **Consequences:** `--force` remains allowed when `git push` is session-approved (honesty test). HUMAN DPIA before crash-proxy enable.

### 2026-08-27 — /ideas ship hygiene (M44)
- **Status:** Accepted
- **Context:** Archive `docs(release)` opened 0.25.1; RP merge CI failed Unreleased order; worktree setup `npm ci`'d the primary tree; `.cursor-session-state.json` was untracked; RP PRs showed Dependency Review `ACTION_REQUIRED`.
- **Decision:** Drop `docs`/`chore` from `changelog-sections` so only feat/fix/perf/revert bump. `changelog_unreleased.move_unreleased_first` on `sync-template-version.sh`. Skip worktree installs unless `ROOT_WORKTREE_PATH` resolves to a different directory. Gitignore the JSON session file. Run `dependency-review-action` from `release-please.yml` and attach a check to the PR head. Print Android plugin pins beside the Gradle fallback. `/gates` always uses the canvas skill; `check-pre-commit-hooks.sh` fails locally, skips in CI.
- **Alternatives considered:** `hidden: true` for docs/chore (rejected: still bumps). `pull_request_target` for Dependency Review (rejected: untrusted checkout).
- **Consequences:** Closed leftover 0.25.1 (#80). Local `/gates` needs `pre-commit install --hook-type commit-msg`.

### 2026-08-27 — Ship v0.25.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M42 local-first deps and M43 resource packing. `upd --apply` without `--max-bump minor` wrote GitHub Action majors; About-without restore `cp` hit Windows file lock after a passing gate.
- **Decision:** Cap apply at `--max-bump minor`; prefer `gh auth token` + `--token` for Release Please dry-run; child feature-gate skips missing optional toolchains; retry About restore then `git checkout` fallback. Empty Unreleased before push. Admin-merge Release Please #77 to **v0.25.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Apply GitHub Action majors (rejected: setup-java v6 / CodeQL fake tags). Fail `/prerelease` when Go is missing on the laptop (rejected: optional stack).
- **Consequences:** Template at 0.25.0. HUMAN leftovers: Ollama install, mcp.json copy, Dependabot frequency. ADB leftover: Android SDK licenses.

### 2026-08-27 — Local resource packing (M43)
- **Status:** Accepted
- **Context:** After M42, local deps were fast but `feature-gate` still ran stacks serially, worktrees reinstalled cold, `/best-of-n` was docs-only, and GPU was idle.
- **Decision:** RAM-capped parallel stack waves (CI max 2 slots; Android weight 2); worktree `npm ci --prefer-offline`; real `/best-of-n` and `/emulator` commands; Ollama on 127.0.0.1 with no cloud keys; emulator skip-if-no-SDK and never on `/ship`. GitHub CI remains post-push truth.
- **Alternatives considered:** Require Ollama or emulator for `/ship` (rejected). Auto-accept SDK licenses (rejected). `OLLAMA_ORIGINS=*` (rejected: LAN exposure). Content-hash skip-cache for bootstrap checks (rejected: stale green).
- **Consequences:** Agents use `check-local-compute`, `/best-of-n`, `/emulator`. Humans may install Ollama and accept Android licenses. `FEATURE_GATE_JOBS=1` is the low-RAM escape hatch.

### 2026-08-27 — Local-first dependency updater (M42)
- **Status:** Accepted
- **Context:** GitHub Dependabot PRs and `/prerelease` waiting on CI/Dependabot/Scorecard before push made releases slow.
- **Decision:** Primary path is local `upd-cli==0.6.2` (`scripts/update-deps.sh`, dry-run default) plus optional pinned depsonar MCP. `/ship` is `update-deps → prerelease → push → regress`. `pre-release-gate.sh --local` skips GitHub waits; default full gate remains for `/regress` and `release.yml`. Release Please Action still publishes; `release-please-dry.sh` is preview-only.
- **Alternatives considered:** Replace Dependabot entirely (rejected: keep weekly backup). Run Release Please `github-release` from the laptop (rejected: still needs GitHub API; preview-only). Call `npx depsonar` from the CLI (rejected: slow, optional).
- **Consequences:** Agents use `/update-deps` or “update deps safely before release”. Humans may copy `mcp.foss.example` to `.cursor/mcp.json`. Automerge frequency remains a HUMAN choice.

### 2026-08-23 — Ship v0.24.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M41 privacy-first GitHub crash and feedback. First CI failed `validate-template-index` (four new scripts/workflows unindexed). About-without failed when feedback imported About; a WSL1 `bash` run of the About gate restored tracked files from HEAD.
- **Decision:** Own `isPlaceholderRepo` in `github-feedback`; use `__APP_VERSION__` in FeedbackPanel. Index the new `.sh`/workflow paths. Empty Unreleased before RP. Admin-merge Release Please #72 to **v0.24.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Import About helpers from later features (rejected: breaks About add/remove). Run About-gate via System32 bash (rejected: WSL1 breaks npm and the restore trap can wipe uncommitted wiring).
- **Consequences:** Template at 0.24.0. HUMAN Watch/collaborator + optional About smoke remain on the board.

### 2026-08-22 — Privacy-first GitHub crash and feedback intake
- **Status:** Accepted
- **Context:** Need crash/bug/feature intake without email, PII, or proprietary crash SDKs, and route incoming issues through `/audit` (fixes now) vs `/ideas` (features after approval).
- **Decision:** Approach A (client compose + GitHub Issue Forms). Opt-in capture, sanitize twice, CODEOWNERS assign for maintainer notify. `/audit` executes at most 3 `crash`/`bug` fixes and treats issue text as data. Approach B/C stay off the default path (ADR-0002).
- **Alternatives considered:** mailto (PII); Sentry/Crashlytics (proprietary); PAT in client (secret); anonymous proxy (DPIA + abuse, named follow-up).
- **Consequences:** Reporters need a GitHub account. `feedback-inbox` + `feedback-notify.yml` + issue forms ship with Golden Path review UI.

### 2026-08-21 — Ship v0.23.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M40 Continuum donations/updates. First CI failed Playwright donate-nudge: `addInitScript` reset `lastSeenVersion` on reload. About-without gate failed on Windows `EINVAL` writing `preferences.ts`.
- **Decision:** Seed lastSeen only when unset; write About stubs via tmp+replace and leave theme-only preferences in place. Empty Unreleased and keep it first after RP merge. Admin-merge Release Please #71 to **v0.23.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Force-set lastSeen on every navigation (rejected: hides the once-per-version contract). Overwrite preferences with INTERVAL_KEY stubs (rejected: settings is theme-only).
- **Consequences:** Template at 0.23.0. Optional HUMAN smoke remains on the board. Next `/ship` should keep Unreleased first+empty on `origin/main`.

### 2026-08-21 — M40 donations and updates (Continuum method)
- **Status:** Accepted
- **Context:** Child Golden Path About mixed donate with update nags (settings toggle, home banner, snackbar). Continuum Calendar already had a quieter launch policy.
- **Decision:** Port Continuum: quiet Venmo donate, one note after a version change, silent daily GitHub check of installer filenames (`Golden-Path-X.Y.Z-x64-setup.exe` / `golden-path-X.Y.Z-foss.apk`). Donate short-circuits that launch. Device-local prefs only (`gp.update.*` / `gp_updates`, excluded from Android Auto Backup). Remove the Settings update-check toggle and home banner.
- **Alternatives considered:** Keep the opt-in interval toggle (rejected: Continuum has no daily donate timer and no launch gate). Drive PWA `applyPwaUpdate` from the GitHub installer prompt (rejected: About-only).
- **Consequences:** `docs/features/donations-updates.md` + `productUpdate` / `ProductUpdate` API. `OWNER/REPO` stays silent. HUMAN optional smoke remains on M40.

### 2026-08-20 — Ship v0.22.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after Android same-resolution high-refresh. First CI failed instrumented smoke: `preferredDisplayModeId` stayed 0 because `decorView.display` is null in `onCreate`.
- **Decision:** Apply the mode in `onStart` via `Activity.display` / `windowManager.defaultDisplay`. Empty Unreleased before push. Admin-merge Release Please #70 to **v0.22.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Keep the instrumented assertion optional (rejected: it caught a real no-op). Set the mode only in `onCreate` with `windowManager.defaultDisplay` (weaker on multi-display).
- **Consequences:** Template at 0.22.0. Child Android apps should vote display mode after the window attaches.

### 2026-08-18 — Ship v0.21.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M38+M39. Pre-release green on `f54927e`; feat `8eab392` then `df322af` after `rp_merge_status` import failed once `PYTHONPATH` was stripped.
- **Decision:** Push feat + cwd-relative import fix; wait Ubuntu + Windows upgrade-sim; admin-merge Release Please #69 to **v0.21.0**. Fold leftover Unreleased onto the PR as comments. Archive M39 and empty Unreleased after merge (fold does not rewrite the RP branch). Codex skipped (no key/CLI).
- **Alternatives considered:** Leave leftover Unreleased under `[0.21.0]` (rejected: next `/ship` fails first+empty gates). `pull_request_target` for RP checks (rejected).
- **Consequences:** Template at 0.21.0. Next `/ship` should empty Unreleased in the prepare commit so RP does not carry bullets. SBOM attaches via `release` published workflow.

### 2026-08-17 — M39 /ideas Windows PATH + ship hygiene
- **Status:** Accepted
- **Context:** Fifth `/ideas` pass. Git Bash still missed `gh`; inherited `PYTHONPATH=scripts/lib` broke validate-bootstrap; leftover Unreleased blocked RP merge; Q&A REST create often SKIP'd.
- **Decision:** Shared `resolve-tools.sh` prepends Windows tool dirs and unsets `PYTHONPATH`. `agent-run` uses `child_env()`. Fold Unreleased onto the RP PR comment then empty. No `environment:` on CI/Security/CodeQL. GraphQL list + REST/GraphQL create for Q&A with a one-line HUMAN fallback. Archive M38; name Windows check on child Sprint 0 AUTO.
- **Alternatives considered:** Document PATH-only (rejected: every `gh` script still fails). `pull_request_target` for RP checks (rejected: untrusted checkout). Fail setup when Q&A API 422s (rejected: Settings fallback is enough).
- **Consequences:** Source `resolve-tools.sh` before `command -v gh`. Never export `PYTHONPATH=scripts/lib`. `/ship` comments leftover notes then empties Unreleased locally.

### 2026-08-17 — M38 /ideas ship-hardening
- **Status:** Accepted
- **Context:** Fourth `/ideas` pass after v0.20.0. Live `main` still required only five checks; Windows `jq` and leftover Unreleased still bit `/ship`.
- **Decision:** Fail `pre-release-gate` on missing Windows upgrade-sim; apply that check via `setup-github-repo`. Python-only `TEMPLATE_INDEX`. Skip RP wait on `ACTION_REQUIRED`. Split allowlisted `scripts/lib` to ≤150. Empty-Unreleased gate before RP merge. Archive Coach/M37/M36. Token on upgrade-sim jobs.
- **Alternatives considered:** Keep jq + CR strip (rejected: two paths). Leave lib allowlist (rejected: token-economy lie).
- **Consequences:** `/ship` now fails until protection matches the script. New `scripts/lib` files stay ≤150 with an empty allowlist.

### 2026-08-17 — Ship v0.20.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after three `/ideas` implement-all rounds. First pre-release gate was green on `01e21fc`; feat `14811be` then failed upgrade-sim (stamped purpose, pruned Android link, Windows `jq` CRLF).
- **Decision:** Keep portable-purpose assert on the template repo only; ignore doc links into pruned `modules/`/`examples/`; strip CR from `jq` paths in `validate-template-index`. Push fixes, wait for Ubuntu + Windows upgrade-sim, admin-merge Release Please #68 to **v0.20.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Skip full validate after prune (rejected: hides real child-repo doc breaks). Drop Windows upgrade-sim from required checks (rejected: that was the point of the ideas pass).
- **Consequences:** Template at 0.20.0; `/ship` must treat Windows `jq.exe` CRLF and post-prune links as first-class gates. SBOM attaches via `release` published workflow.

### 2026-08-17 — Implement /ideas backlog (required Windows check, coach twin)
- **Status:** Accepted
- **Context:** Third `/ideas` pass. Windows upgrade-sim existed but `/ship` and branch protection did not name it.
- **Decision:** Add the Windows job to required checks; ship `docs/help/COACH.md`; health notes dirty Unreleased; skip weekly AUTO rows after a green weekly-health run; Codespaces `verify.sh`; citation `date-released`; pin setup-python SHA; split `build_sprint` and gate new `scripts/lib` files at 150 lines (allowlist pre-existing oversize modules).
- **Alternatives considered:** Split every lib file in one pass (rejected: too much risk for `/build`). Fail file-limits on allowlisted modules (rejected: would block unrelated work).
- **Consequences:** `/ship` waits on Windows upgrade-sim once the job has run on HEAD. New `scripts/lib` modules must stay ≤150 lines.

### 2026-08-17 — Implement /ideas backlog (health, Windows CI, links)
- **Status:** Accepted
- **Context:** Second `/ideas` pass after the first eight items shipped locally. Health still pointed at child Sprint 0 on this template.
- **Decision:** Auto lane uses maintainer board when `bootstrap.config.json` still describes this template. Skip `pwsh` when missing; add `windows-latest` upgrade-sim. Split gate hints to JSON. Extend doc-link gate to root `*.md` + pre-commit. Best-effort Q&A category after Discussions enable.
- **Alternatives considered:** Keep auto=child on the template (rejected: wrong next step). Fail upgrade-sim without `pwsh` (rejected: bash path already proved). Require Q&A API success (rejected: Settings fallback).
- **Consequences:** `/coach` and `/ideas` on this repo name Ongoing Maintenance, not init-project. Child repos with their own purpose stay on the child playbook.

### 2026-08-17 — Implement /ideas backlog (8 items)
- **Status:** Accepted
- **Context:** `/ideas` ranked eight in-scope template items after v0.19.0. User asked to implement all.
- **Decision:** Ship Windows pyrepl env, CITATION.cff version sync, glossary, portable stamp copy, verify.sh hints, opt-in welcome issue, docs link gate, and Discussions enablement from `setup-github-repo`.
- **Alternatives considered:** Cursor-only purpose (rejected: portability). Welcome issue on by default (rejected: matches other post hooks). Fail setup if Discussions API cannot toggle (rejected: [HUMAN] Settings fallback).
- **Consequences:** `post_welcome_issue` stays false; `/ship` regress should finish on This Computer; glossary is REQUIRED.

### 2026-08-16 — Ship v0.19.0 (/ship)
- **Status:** Accepted
- **Context:** Coach layer (`2f77fb9`) plus portable first-run polish were unpushed; first pre-release CI wait failed because HEAD had no Actions run.
- **Decision:** Push feat commits, wait for CI/Security/CodeQL, re-run `pre-release-gate`, merge Release Please #67 to **v0.19.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Hold for Codex (rejected: skip is allowed). Invent per-tool rulebooks (rejected: AGENTS.md SoT).
- **Consequences:** Template at 0.19.0; SBOM attaches via `release` published workflow; batch commands are 24 atomic + 5 super.

### 2026-08-16 — Portable first-run (any agent IDE)
- **Status:** Accepted
- **Context:** First-time users needed a scripted tour and readable gate failures; the template was Cursor-weighted while Windsurf, Antigravity, and others already read `AGENTS.md`.
- **Decision:** Keep `AGENTS.md` Sacred. Generate thin pointers (`GEMINI.md` pointer-only, Windsurf, Cline, Aider, Continue). Ship `/tour` plus `docs/help/TOUR.md`. Add adapter drift gate, VS Code tasks, SUPPORT.md, CITATION.cff, good-first-issue, live badges, Codespaces link.
- **Alternatives considered:** Duplicate full rules into `.windsurfrules` / `.agents/agents.md` (rejected: drift and a second SoT). Register `/why` (rejected: `/coach` synonym only).
- **Consequences:** Edit `AGENTS.md` then `--sync-adapters`. Never put real rules in `GEMINI.md`.

### 2026-08-16 — Template Excellence / Coach Layer
- **Status:** Accepted
- **Context:** The template already had gates, memory, and Golden Paths; new users still got files without the industry *why*.
- **Decision:** Add `docs/BEST_PRACTICES.md` + `docs/FIRST_30_DAYS.md`, `/coach` + Welcome Tour, init what/why summary, optional FUNDING.yml/topics, and optional `justfile`s. Do not require `just` in CI. Do not register a second `/why` command.
- **Alternatives considered:** Fold the 30-day list into BEST_PRACTICES (rejected: token bloat); husky instead of just (rejected: pre-commit already covers hooks).
- **Consequences:** Batch-command count is 23 atomic + 5 super. `/bootstrap` ends with a tour. Child product READMEs gain For humans / For agents sections.

### 2026-08-16 — M37 gap close (verify, env, hooks)
- **Status:** Accepted
- **Context:** Checklist audit found core governance/CI present; remaining gaps were docker preflight, unimplemented post-hook flags, no root verify command, no env schema, no commit-msg enforcement, no Dockerfile, and no `.agent/memory` indexes.
- **Decision:** Extend the existing engine. `scripts/verify.sh` is the harness. Env validation is stack-agnostic JSON schema. Skills/memory under `.agent/` are indexes to `.cursor/skills/` and `DECISION_LOG.md` / `KNOWLEDGE_BASE.md`. Post install/test/git-init stay opt-in. Conventional Commits via pre-commit `commit-msg`, not Node-only commitlint.
- **Alternatives considered:** Duplicate skills into `.github/skills/` (rejected); husky + lint-staged (rejected: pre-commit already covers all stacks); auto-commit after init (rejected: destructive-ops).
- **Consequences:** `validate-bootstrap` requires env schema, Dockerfile, `.agent/` indexes, and `verify.sh`. Feature-gate fails if `.env.example` drifts from `env.schema.json`.

### 2026-08-16 — M36 bootstrap standards (AGENTS.md + lifecycle)
- **Status:** Accepted
- **Context:** Audit asked for a generator-style AGENTS.md engine, SDD stubs, security-by-default, manifest, and pre/post hooks. The repo already shipped SECURITY.md, CONTRIBUTING.md, CI, Dependabot, issue/PR templates, and `init-project`.
- **Decision:** Keep the GitHub Template + `init-project` model. Expand `AGENTS.md` as the canonical spec; generate thin Cursor/Claude/Copilot adapters; add `docs/spec.md` / `docs/plan.md`; add `bootstrap.config.json` plus preflight/post hooks and `PROJECT_CHECKLIST.md`. MIT remains default; Apache-2.0 is an init option for child repos. Do not auto-commit or auto-install deps.
- **Alternatives considered:** Separate yeoman-style generator CLI (rejected: would fork the template model); GitHub `- [ ]` checkboxes on the new checklist (rejected: repo-wide 🔲/✅/❌ convention).
- **Consequences:** `validate-bootstrap` requires SDD stubs, adapters, and engine unit tests. Child `AGENTS.md` stays Sacred on upgrade; adapters are Canon via `--sync-adapters`.

### 2026-08-16 — Ship v0.18.3 (/ship)
- **Status:** Accepted
- **Context:** Dependabot #64 Compose BOM bump on main; RP #66 already open
- **Decision:** `/ship` autofix + pre-release gate, then merge Release Please #66 to **v0.18.3**. Codex skipped (no key/CLI).
- **Alternatives considered:** Hold BOM for a later patch (rejected: CI including instrumented Android already green)
- **Consequences:** Template at 0.18.3; SBOM attaches via `release` published workflow

### 2026-08-16 — Ship v0.18.2 (/push)
- **Status:** Accepted
- **Context:** M35 HUMAN Scorecard/Dependabot/radar already on `main` @ `23254e8`; CI/Security/CodeQL green; RP #63 open
- **Decision:** Merge Release Please #63 to **v0.18.2** after local maintainer + pre-release gates (no extra prepare commit)
- **Alternatives considered:** Wait for RP auto-merge (blocked: no checks on release-please branch)
- **Consequences:** Template at 0.18.2; next quarterly radar 2026-11-15; Dependabot #64 left open

### 2026-08-15 — M35 Scorecard SARIF + Dependabot + radar
- **Status:** Accepted
- **Context:** Open HUMAN items after v0.18.1: Scorecard PinnedDependencies / TokenPermissions / VulnerabilitiesID; Dependabot #58–#61; quarterly radar (last report 2026-06-30)
- **Decision:** Job-scope write tokens (`permissions: read-all` at workflow level). Keep `@vX.Y.Z` for GitHub-owned actions. Treat VulnerabilitiesID as stale (hono/nanoid/postcss already patched in 0.18.0). Merge green Dependabot PRs after rebase; rebase #61 (stale web lockfile). Radar max new score is 6 — no BUILD_PLAN row.
- **Alternatives considered:** SHA-pin every `actions/*` (rejected: conflicts with `validate-workflow-actions` + existing policy); add Design Mode / Canvas now (rejected: score 6, below ≥9 suggest threshold)
- **Consequences:** TokenPermissions should clear on next Scorecard run; PinnedDependencies remain accepted; next quarterly radar due 2026-11-15

### 2026-08-15 — Ship v0.18.1 (/push)
- **Status:** Accepted
- **Context:** M35 Windows Store `python3` hang + About-gate restore ready; first `PY="py -3"` broke `"$PY"` in Dependabot count
- **Decision:** Resolve `PY` to `sys.executable` from `py -3`; merge Release Please #62 to **v0.18.1** after CI green on `b4fca9c`
- **Alternatives considered:** Leave `PY="py -3"` and unquote all callers (rejected: `"$PY"` is the safe pattern); wait for RP auto-merge (blocked: no checks on release-please branch)
- **Consequences:** Template at 0.18.1; Scorecard SARIF and Dependabot PRs #58–#61 stay HUMAN

### 2026-08-15 — Audit M35 Windows Python resolver
- **Status:** Accepted
- **Context:** `/ship` autofix hung because `command -v python3` resolved to the Microsoft Store stub under `WindowsApps`
- **Decision:** Add `scripts/lib/resolve-python.sh` (skip Store stub; prefer `py -3`) and source it from gate/autofix scripts; restore About slice from `git checkout HEAD` if the verify-about backup is missing
- **Alternatives considered:** Document-only workaround (rejected: every Windows gate still hangs); require a `python3` symlink in PATH (rejected: Store alias still wins)
- **Consequences:** Local gates on This Computer no longer stall on the stub; HUMAN still owns Scorecard SARIF and Dependabot PRs #58–#61

### 2026-08-15 — Ship v0.18.0 (/ship)
- **Status:** Accepted
- **Context:** M34 prior-art steals ready; pre-release gate blocked on High `extract-zip` (no upstream patch) via LHCI → puppeteer-core
- **Decision:** Override `@puppeteer/browsers` >=3.2.0 (uses `modern-tar`); lock optional peer `proxy-agent` >=8.0.2 so CI `npm ci` matches; bump `hono`/`postcss`/`nanoid`; merge Release Please #56 to **v0.18.0**
- **Alternatives considered:** Dismiss extract-zip as dev-only (rejected: gate requires zero High); vendor a patched fork (rejected: no patch exists)
- **Consequences:** Template at 0.18.0; honesty labels + scratchpad/handoff ship; Codex skipped (no key/CLI)

### 2026-08-14 — Prior-art thin steals (M34)
- **Status:** Accepted
- **Context:** Compared CopperDogma, Barony, Sciensoft, and wshobson/agents against this Cursor-first template. Need mechanisms without vendoring those trees or an 80k playbook.
- **Decision:** Ship honesty labels, parallel handoff stub, Canon/Mixed/Sacred upgrade column, OWASP LLM walk, scratchpad reset, optional marketplace pointer, and bootstrap-doctor alias. Number as **M34** (plan draft said M30; that sprint is already archived).
- **Alternatives considered:** Vendor Barony/`baron` (rejected: second product + PyPI dep); install wshobson catalog by default (rejected: token bloat); Sciensoft one-file playbook (rejected: 300/150 caps).
- **Consequences:** Hooks stay fail-open and labeled; child `AGENTS.md` / init prompt stay Sacred; no new CI scanner or marketplace install on the FOSS default path.

### 2026-08-12 — Ship v0.17.0 branding kit (/ship)
- **Status:** Accepted
- **Context:** Child repos need replaceable logos/colors and pitch-quality READMEs without overwriting the template README
- **Decision:** Ship `branding/` pack + mode-gated `generate-project-readme.py` (`template` preview only; `product` writes root README); extend token sync for official-colors and asset distribution; merge Release Please #55 to **v0.17.0**
- **Alternatives considered:** Generate logos from tokens only (rejected: humans replace art files); always overwrite root README (rejected: clobbers template guide)
- **Consequences:** Sprint 0 fills `product.json` then generate; upstream keeps `mode: template`; store PNGs remain human/ADB exports

_Seed template ADR: `docs/adr/0000-template-baseline.md`. Child repos use `docs/adr/0001-core-architecture.md`._

### 2026-08-10 — Ship v0.16.0 (/ship)
- **Status:** Accepted
- **Context:** Need third-party review + broader autofix before release; `/ship` should stay one command
- **Decision:** Codex read-only reviewer (opt-in CI + `/codex-review`) feeds `CODE_REVIEW.md` → Cursor `/fix`; expand `/prerelease` with multi-stack autofix; merge Release Please #51 to **v0.16.0**
- **Alternatives considered:** Codex writes patches in CI (rejected: destructive-ops / FOSS spend control); chain Codex into every `/maintain` (rejected: API cost)
- **Consequences:** `/ship` runs autofix + optional Codex + hard gate; enable Codex CI by copying workflow example + `OPENAI_API_KEY`

### 2026-08-01 — Ship v0.15.2 (/ship)
- **Status:** Accepted
- **Context:** Plan Mode left risks as open questions; Dependabot High blocked pre-release (js-yaml, then postcss)
- **Decision:** Require Issue→Resolution Critique in always-applied rules + `/plan`; override patched npm transitive CVEs; merge Release Please #50 to **v0.15.2**
- **Alternatives considered:** Soft "list risks" Critique (rejected: humans still had to chase resolutions); defer brace-expansion/postcss (rejected: pre-release gate requires zero Critical/High)
- **Consequences:** Agents must bake mitigations into plan todos; template at 0.15.2 with SBOM release assets

### 2026-07-22 — Ship v0.15.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M33 + local-first compute; first CI failed on duplicate `## [Unreleased]`; web tests failed on Node 25+ localStorage stub
- **Decision:** Polyfill Storage in vitest setup (KB-011); collapse stale Unreleased; merge Release Please #37 to **v0.15.0**
- **Alternatives considered:** `--no-webstorage` only (rejected: may break older Node CI); leave duplicate Unreleased (rejected: gate hard-fail)
- **Consequences:** Template at 0.15.0 with Cursor worktrees/permissions/skills/plugin pack and local-first parallelism

### 2026-07-21 — Local-first compute on This Computer
- **Status:** Accepted
- **Context:** Agents defaulted toward serial work or Cloud handoff even when the desktop has many cores
- **Decision:** Ship `local-compute.mdc` + sessionStart CPU reminder; parallelize independent `validate-bootstrap` checks via `run_checks_parallel.py` (`BOOTSTRAP_CHECK_JOBS`); pytest-xdist `-n auto`; Gradle `--parallel`; document `/scope` + worktrees/`/best-of-n` as the local default over Cloud Agents
- **Alternatives considered:** Always Cloud Agents for parallelism (rejected: wastes local hardware and costs credits); unbounded bash `&` in validate-bootstrap (rejected: harder error aggregation on Windows)
- **Consequences:** Quick bootstrap checks use all cores (e.g. jobs=CPU count); agents are steered to concurrent Task/worktrees when local

### 2026-07-21 — Cursor 3.9–3.11 FOSS integration (M33)
- **Status:** Accepted
- **Context:** Cursor added native worktrees setup, Auto-review `permissions.json`, Skills direction, CLI/GHA, side chats, Design Mode, cloud conversation hooks, Automations, and plugin packaging; registry lagged at 2026-06-30
- **Decision:** Ship FOSS live `worktrees.json` + fail-soft OS setup, committed `permissions.json` (dual layer with hooks), four new skills + checker atomic update, CLI workflow under `.github/workflow-examples/` (never auto-run), plugin via pack-to-`dist/cursor-plugin` (no repo-root symlink); keep commercial as examples (cloud hooks, Automations recipes, Bugbot Autofix map)
- **Alternatives considered:** Custom plugin paths into `.cursor/` (rejected: discovery risk); whole-repo plugin symlink (rejected: double-load); `.example.yml` under `workflows/` (rejected: GHA may load it); weaken shell hook for Auto-review (rejected: hooks stay hard FOSS enforcement)
- **Consequences:** `check-cursor-integrations` requires seven skills + worktrees/permissions; `/best-of-n` documented beside parallel-lock worktrees; Cloud Agents still ignore Run Modes

### 2026-07-12 — Pre-release gate Dependabot counter + FOSS MCP check
- **Status:** Accepted
- **Context:** `/push` pre-release `--strict` failed: Dependabot alerts API used unsupported `page=` form; FOSS integrations check failed whenever gitignored `.cursor/mcp.json` existed locally
- **Decision:** Count alerts via `gh api --paginate` query string; treat live `mcp.json` as OK unless `git ls-files` shows it tracked; multi-stack `--strict` skips missing optional toolchains
- **Alternatives considered:** Require `security_events` refresh always (rejected: false failures blocked release); ban local MCP (rejected: contradicts CURSOR_INTEGRATIONS activation)
- **Consequences:** Maintainer gates pass with local MCP enabled; Release Please #36 published v0.14.1

### 2026-07-12 — Dependabot automerge CI gap (M32)
- **Status:** Accepted
- **Context:** Merges via `GITHUB_TOKEN` (`app/github-actions`) do not start `push` workflows; `main` tip after Dependabot merges had zero CI runs; weekly health failed waiting for missing runs
- **Decision:** Prefer optional `AUTOMERGE_TOKEN` PAT for Dependabot/Release Please merge; add `workflow_dispatch` to CodeQL + Security Scan; `check-github-ci.sh --dispatch-if-missing` (weekly health uses it with `actions: write`); prefer Git Bash in `agent-run.py` on Windows
- **Alternatives considered:** Require PAT only (rejected: blocks FOSS template without secrets); SHA-pin all actions for Scorecard (deferred: conflicts with documented `@vX.Y.Z` policy)
- **Consequences:** Weekly health can self-heal missing runs; post-merge CI still needs HUMAN required-status-checks + optional PAT for true push triggers

### 2026-07-02 — Quiet agent shell (hooks Python + agent-run)
- **Status:** Accepted
- **Context:** Cursor Agent shell execution opened `.sh` hook and script tabs, stealing editor focus while users typed
- **Decision:** Migrate hooks to Python; add `scripts/agent-run.py` for agent gate invocations; ship `.vscode/settings.json` anti-reveal defaults; document KB-010
- **Alternatives considered:** Disable hooks globally (rejected: loses destructive-op guard); rewrite all scripts to PowerShell (rejected: scope); `pythonw.exe` for hooks (rejected: breaks stdout JSON)
- **Consequences:** Agent-facing commands no longer contain `.sh` paths; underlying bash scripts unchanged for CI/humans

### 2026-07-01 — Cursor hook smoke isolation (M31)
- **Status:** Accepted
- **Context:** M31 audit found `check-cursor-hooks.sh --smoke` false-pass when `.cursor-session-state.json` already listed `git push` in `destructive_ops_approved`
- **Decision:** Smoke test clears session approvals before deny assertion; validate hook scripts require shebang on line 1
- **Alternatives considered:** Ignore local session state in smoke (rejected: hides real deny-path bugs); require empty session file (rejected: breaks dev workflow)
- **Consequences:** `--smoke` is deterministic in CI and locally; invalid hook scripts fail validate-bootstrap early

### 2026-06-30 — Cursor hooks as enforcement layer (M30)
- **Status:** Accepted
- **Context:** M27 rejected `beforeSubmitPrompt` hooks; rules alone cannot block destructive shell commands at runtime
- **Decision:** Ship FOSS-safe project hooks (`beforeShellExecution`, `afterFileEdit`, `subagentStart`, `sessionStart`, `beforeMCPExecution`); fail-open guards; session `destructive_ops_approved` for `/push`/`/ship`; opt-out via `<!-- cursor-hooks: off -->`
- **Alternatives considered:** Prompt-rewrite hooks (rejected per M27); broad shell blocklists (rejected: blocks legitimate agent work)
- **Consequences:** `check-cursor-hooks.sh --smoke` in validate-bootstrap; complements `destructive-ops.mdc` without token bloat

### 2026-06-20 — Repo-wide checklist status markers
- **Status:** Accepted
- **Context:** BUILD_PLAN and scattered checklists used mixed ⬜ / `- [ ]` / ✅ formats; inconsistent in Markdown Preview vs source
- **Decision:** Standardize on 🔲 open · ✅ done · ❌ blocked emoji markers repo-wide; document in `BUILD_PLAN.md` legend and agent read order
- **Alternatives considered:** GitHub `- [ ]` task lists (rejected: poor Preview readability and agent parsing); keep ⬜ white square (rejected: visually similar to ✅ in some fonts)
- **Consequences:** All new checklist rows use emoji; `agent-progress.sh` accepts legacy ⬜ for child repos during transition

### 2026-06-18 — Release automation hardening (M29)
- **Status:** Accepted
- **Context:** v0.11.0 release lacked SBOM assets (GITHUB_TOKEN cannot chain `release` → `release.yml`); Release Please skipped `extra-files`; `health-check.yml` registered as path name caused 0-job push failures
- **Decision:** `release-please.yml` runs `sync-template-version.sh` on release PR branches and dispatches `release.yml` on `release_created`; rename workflow to `weekly-health-check.yml`; fix sync script for Windows Git Bash
- **Alternatives considered:** PAT with workflow scope for release chaining (rejected: secrets management); manual SBOM backfill only (rejected: repeated human step each release)
- **Consequences:** Release Please needs `actions: write`; future releases should ship SBOM assets without manual dispatch

### 2026-06-17 — Batch instruction templates (M27)
- **Status:** Accepted
- **Context:** Agents and child-repo owners needed repeatable shortcuts for bootstrap, verify, build, ship, and maintenance workflows without re-pasting long prompts
- **Decision:** Ship 25 slash commands in `.cursor/commands/` (20 atomic + 5 super), bare-word expansion via `batch-commands.mdc`, human cheat sheet at `docs/help/BATCH_COMMANDS.md`, registry at `docs/BATCH_COMMANDS.md`; `/push` and `/ship` grant explicit push approval
- **Alternatives considered:** `beforeSubmitPrompt` hook for bare words (rejected: Cursor API cannot rewrite prompts); single mega-doc for humans and agents (rejected: overwhelms first-time users)
- **Consequences:** `alwaysApply` rule adds ~25 lines per session; `check-batch-commands.sh` prevents registry drift; child repos cherry-pick via `UPGRADING_FROM_TEMPLATE.md`

### 2026-06-30 — Autonomous /build with grouped human section
- **Status:** Accepted
- **Context:** `/build` halted on HUMAN/ADB rows; humans needed a single review block after automation; child repos need scripted attempts before manual follow-up
- **Decision:** Add `build-sprint-status.sh`, `attempt-build-plan-row.sh`, and `HUMAN_BACKLOG.md` (failure-only); restructure BUILD_PLAN with `#### Human & device (after automation)`; AGENT/AUTO runs first, then automation attempts on grouped human rows
- **Alternatives considered:** Skip human rows entirely during /build (rejected: loses automation catalog value); keep human rows interleaved in Sequential (rejected: hard to review after automation)
- **Consequences:** Child repos must place HUMAN/ADB rows in the grouped section; `<!-- no-auto-approve -->` disables autonomous ADR ack

### 2026-06-13 — @lhci/cli npm overrides for transitive CVEs
- **Status:** Accepted
- **Context:** Lighthouse CI (`@lhci/cli`) bundles transitive dependencies (`tmp`, `uuid`) with known CVEs; no patched `@lhci/cli` release available at triage time
- **Decision:** Add npm `overrides` in `examples/web/package.json` forcing `tmp >= 0.2.6` and `uuid >= 11.1.1`; document in KB-007
- **Alternatives considered:** Dismiss Dependabot alert (rejected: hides real risk); remove Lighthouse CI job (rejected: loses performance gate)
- **Consequences:** Lockfile must be regenerated after override changes; overrides should be removed when `@lhci/cli` ships fixed dependencies

### 2026-06-13 — Ship all optional ecosystem modules (M3)
- **Status:** Accepted
- **Context:** Sprint M3 asked whether to ship Lightroom, Rust, and Go optional modules in the template maintainer repo
- **Decision:** Ship all three with Golden Path stubs, MODULE.md guides, and path-gated CI jobs (`lightroom`, `rust`, `go`) that skip when child repos remove the directories
- **Alternatives considered:** Lightroom-only (rejected: Rust/Go stubs are low-cost and popular); defer all optional modules (rejected: COMPLETED_TASKS M3 work already landed)
- **Consequences:** Template CI runs more jobs on `main`; child repos can delete unused `examples/` folders to skip jobs via `hashFiles` guards
