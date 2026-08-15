# agent-project-bootstrap

![MIT](https://img.shields.io/badge/license-MIT-2ea043?style=flat-square)
![Template](https://img.shields.io/badge/template-0.18.0-0969da?style=flat-square)
![FOSS](https://img.shields.io/badge/FOSS-no_tracking-656d76?style=flat-square)

GitHub Template Repository for bootstrapping FOSS projects with Cursor agents.

**Bootstrap a FOSS project with agent-ready scaffolding** — initialization prompts, labeled sprints, CI guardrails, and Golden Path examples.

You get:

- Labeled BUILD_PLAN sprints (`AGENT` / `HUMAN` / `ADB` / `AUTO`)
- Golden Path stubs (Web, Python, Android, Lightroom)
- CI guardrails + configurable template update checker

## Contents

- [Quick Start](#quick-start)
- [Agent shortcuts (cheat sheet)](#agent-shortcuts-cheat-sheet)
- [Stack Selection](#stack-selection-sprint-0)
- [What's Included](#whats-included)
- [BUILD_PLAN Labels](#build_plan-labels)
- [GitHub Pages Demo](#github-pages-demo)
- [Template Update Checker](#template-update-checker)
- [GitHub CI Gate](#github-ci-gate-post-push)
- [Security](#security)
- [Supported Stacks](#supported-stacks)
- [Repository Layout](#repository-layout)
- [Contributing](#contributing)
- [GitHub About](#github-about)
- [Maintainer Release](#maintainer-release)

---

*Getting started*

## Quick Start

1. Click **Use this template** on GitHub to create your project repo.

2. Clone and run the init script:

   ```bash
   # Linux / macOS / WSL
   ./scripts/init-project.sh

   # Windows PowerShell
   .\scripts\init-project.ps1
   ```

3. Open Cursor and paste the bootstrap prompt from [`docs/START_HERE.md`](docs/START_HERE.md):

   ```
   Read @docs/START_HERE.md, @docs/CURSOR_MODES.md, and @docs/INITIALIZATION_PROMPT.md.
   Pick Cursor mode per CURSOR_MODES.md. Follow Section 8 Startup Sequence.
   Use BUILD_PLAN.md Sequential lane first; respect AGENT/HUMAN/ADB/AUTO labels.
   ```

4. **Agent shortcuts:** bookmark **[docs/help/BATCH_COMMANDS.md](docs/help/BATCH_COMMANDS.md)** — type `/` in Agent chat (`/bootstrap` · `/verify` · `/build` · `/ship`). *Bookmark it for when you come back after a break.*

## Agent shortcuts (cheat sheet)

**[docs/help/BATCH_COMMANDS.md](docs/help/BATCH_COMMANDS.md)** — shortcut commands for Cursor Agent.

- `/bootstrap` — new project Sprint 0 end to end
- `/build` — plan and implement a feature
- `/verify` — checks before merge
- `/ship` — publish a release to GitHub

Type **`/`** in Agent chat to pick a command. *Bookmark it for when you come back after a break.*

## Stack Selection (Sprint 0)

During `init-project`, choose `web`, `python`, `android`, `multi`, or `none` (keep all).

Active modules are synced to `AGENT_MEMORY.md` and recorded in `.cursor/stack-selection.json`.

<p>
  <img src="https://img.shields.io/badge/web-stack-646cff?style=flat-square" alt="web" />
  <img src="https://img.shields.io/badge/python-stack-3776AB?style=flat-square" alt="python" />
  <img src="https://img.shields.io/badge/android-stack-3DDC84?style=flat-square" alt="android" />
  <img src="https://img.shields.io/badge/multi-all_stacks-0969da?style=flat-square" alt="multi" />
  <img src="https://img.shields.io/badge/none-keep_all-656d76?style=flat-square" alt="none" />
</p>

---

*How agents work*

## What's Included

<details>
<summary><strong>Component catalog</strong> — onboarding, memory, security, examples, tooling</summary>

<h4>Onboarding & agents</h4>
<dl>
  <dt>Initialization prompt</dt>
  <dd><a href="docs/INITIALIZATION_PROMPT.md"><code>docs/INITIALIZATION_PROMPT.md</code></a></dd>
  <dt>Agent routing</dt>
  <dd><a href="docs/START_HERE.md"><code>docs/START_HERE.md</code></a>, <a href="docs/CURSOR_MODES.md"><code>docs/CURSOR_MODES.md</code></a>, <a href="docs/FOR_AGENTS.md"><code>docs/FOR_AGENTS.md</code></a>, <a href="AGENTS.md"><code>AGENTS.md</code></a></dd>
  <dt>Agent shortcuts</dt>
  <dd><a href="docs/help/BATCH_COMMANDS.md"><code>docs/help/BATCH_COMMANDS.md</code></a> — slash commands (<code>/bootstrap</code>, <code>/verify</code>, <code>/build</code>, <code>/ship</code>)</dd>
  <dt>Sprint task board</dt>
  <dd><a href="BUILD_PLAN.md"><code>BUILD_PLAN.md</code></a> (active board); archived sprints in <a href="COMPLETED_TASKS.md"><code>COMPLETED_TASKS.md</code></a></dd>
</dl>

<h4>Memory & decisions</h4>
<dl>
  <dt>Workspace memory</dt>
  <dd><a href="AGENT_MEMORY.md"><code>AGENT_MEMORY.md</code></a>, <a href="DECISION_LOG.md"><code>DECISION_LOG.md</code></a>, <a href="KNOWLEDGE_BASE.md"><code>KNOWLEDGE_BASE.md</code></a></dd>
</dl>

<h4>Security & operations</h4>
<dl>
  <dt>Security & privacy</dt>
  <dd><a href="SECURITY.md"><code>SECURITY.md</code></a>, <a href="docs/SECURITY_TRIAGE.md"><code>docs/SECURITY_TRIAGE.md</code></a>, <a href="docs/THREAT_MODEL.md"><code>docs/THREAT_MODEL.md</code></a>, <a href="docs/PRIVACY.md"><code>docs/PRIVACY.md</code></a></dd>
  <dt>Operations</dt>
  <dd><a href="docs/RUNBOOK.md"><code>docs/RUNBOOK.md</code></a></dd>
  <dt>License attribution</dt>
  <dd><a href="THIRD_PARTY_LICENSES.md"><code>THIRD_PARTY_LICENSES.md</code></a>, <a href="LICENSE"><code>LICENSE</code></a></dd>
</dl>

<h4>Examples & tooling</h4>
<dl>
  <dt>Stack modules</dt>
  <dd><code>modules/{web,python,android,lightroom,rust,go}/MODULE.md</code></dd>
  <dt>Golden Path examples</dt>
  <dd><code>examples/{web,python,android,node}/</code>; optional <code>lightroom/</code>, <code>rust/</code>, <code>go/</code> — see <a href="docs/OPTIONAL_STACKS.md"><code>docs/OPTIONAL_STACKS.md</code></a></dd>
  <dt>Agent documentation</dt>
  <dd><code>docs/</code> — prompts, security, design guide (<strong>not</strong> the public website)</dd>
  <dt>Public website source</dt>
  <dd><a href="examples/web/"><code>examples/web/</code></a> (Vite PWA source; see <a href="docs/WEB_PROJECT_LAYOUT.md"><code>docs/WEB_PROJECT_LAYOUT.md</code></a>)</dd>
  <dt>GitHub Pages deploy</dt>
  <dd><code>.github/workflows/pages.yml</code> → <code>examples/web/dist</code> (Actions artifact)</dd>
  <dt>CI guardrails</dt>
  <dd><code>.github/workflows/</code> (incl. OpenSSF Scorecard weekly)</dd>
  <dt>Cursor rules</dt>
  <dd><code>.cursor/rules/*.mdc</code> (incl. <code>cursor-modes.mdc</code>, <code>batch-commands.mdc</code> — see <a href="docs/CURSOR_MODES.md"><code>docs/CURSOR_MODES.md</code></a> and <a href="docs/help/BATCH_COMMANDS.md"><code>docs/help/BATCH_COMMANDS.md</code></a>)</dd>
</dl>

</details>

## BUILD_PLAN Labels

Every task carries an owner label for filtering automated vs human work.

**Status markers:** 🔲 open · ✅ done · ❌ blocked — use emoji on all checklist rows (not `- [ ]` checkboxes) so state is clear in Markdown source and Preview. See [`BUILD_PLAN.md`](BUILD_PLAN.md) legend.

> [!TIP]
> Filter tasks by owner: `grep '\[AGENT\]' BUILD_PLAN.md` (also `HUMAN`, `ADB`, `AUTO`).

<table>
  <thead>
    <tr>
      <th>Label</th>
      <th>Owner</th>
      <th>When to use</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><img src="https://img.shields.io/badge/AGENT-Cursor_Agent-2ea043?style=flat-square" alt="AGENT" /></td>
      <td>Cursor Agent</td>
      <td>Code, docs, scaffolding, tests, CI</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/HUMAN-Human_Developer-0969da?style=flat-square" alt="HUMAN" /></td>
      <td>Human developer</td>
      <td>Approvals, credentials, GitHub settings</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/ADB-Android_Device-bf8700?style=flat-square" alt="ADB" /></td>
      <td>Human (Android)</td>
      <td>Device testing, F-Droid submission</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/AUTO-CI_Scripts-656d76?style=flat-square" alt="AUTO" /></td>
      <td>CI/scripts</td>
      <td>GitHub Actions, Dependabot, pre-commit</td>
    </tr>
  </tbody>
</table>

```bash
grep '\[AGENT\]' BUILD_PLAN.md
grep '\[HUMAN\]' BUILD_PLAN.md
grep '\[ADB\]' BUILD_PLAN.md
grep '\[AUTO\]' BUILD_PLAN.md

```

Each sprint has **Sequential** (ordered) and **Parallel** (isolated scope) lanes in the child-repo playbook. Template maintainers: active board is **maintenance + human open items**; completed maintainer sprints are archived in [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md). See [`BUILD_PLAN.md`](BUILD_PLAN.md).

## GitHub Pages Demo

The `examples/web` PWA deploys to GitHub Pages on push to `main` (workflow: `.github/workflows/pages.yml`). The build sets `VITE_BASE_PATH` for project-site hosting with no analytics or tracking scripts.

> [!WARNING]
> **`docs/` is not your website.** Agent instructions live in `docs/`; only `examples/web/dist/` is deployed. Set Pages source to **GitHub Actions**, not "Deploy from `/docs`". See [`docs/WEB_PROJECT_LAYOUT.md`](docs/WEB_PROJECT_LAYOUT.md).

---

*Operations*

## Template Update Checker

<details>
<summary><strong>Upstream release checks</strong> — intervals, manual commands, devcontainer</summary>

Child repos can check for new upstream template releases on GitHub.

Configure in [`.template-update.json`](.template-update.json):

![default](https://img.shields.io/badge/default-weekly-2ea043?style=flat-square)

<dl>
  <dt><code>off</code></dt>
  <dd>Disabled</dd>
  <dt><code>daily</code></dt>
  <dd>Check at most once per day</dd>
  <dt><code>weekly</code> (default)</dt>
  <dd>Check at most once per week</dd>
  <dt><code>monthly</code></dt>
  <dd>Check at most once per month</dd>
  <dt><code>on_session</code></dt>
  <dd>Check every devcontainer/session start</dd>
</dl>

`notify_method` supports `stdout` only (banner printed to terminal).

**Change interval:** edit `.template-update.json` or re-run the init script.

**Manual check:**

```bash
bash scripts/check-template-updates.sh
# or
pwsh scripts/check-template-updates.ps1

```

Runs automatically on devcontainer start. When a new version is available, see [`docs/UPGRADING_FROM_TEMPLATE.md`](docs/UPGRADING_FROM_TEMPLATE.md).

The devcontainer also runs `check-file-encoding.sh` on start, includes the **GitHub CLI** (`gh`) for `validate-workflow-actions.sh`, and prints a reminder to run `check-github-ci.sh --wait 300` after pushing to `main`.

</details>

## GitHub CI Gate (post-push)

<details>
<summary><strong>Post-push scripts</strong> — CI poll, repo setup, pre-release gate</summary>

After pushing workflow or dependency changes to `main`, poll required workflows:

```bash
bash scripts/check-github-ci.sh --wait 300
# Windows:
pwsh scripts/check-github-ci.ps1 -WaitSeconds 300

```

Required status checks (branch protection via `scripts/setup-github-repo.sh`): **CI**, **Security Scan**, **CodeQL**, **Repo Hygiene**, **Feature Gate**. `check-github-ci` polls the three workflow rollups; **Repo Hygiene** and **Feature Gate** are jobs inside the **CI** workflow.

One-time repo security setup (Dependabot alerts, private reporting, branch protection):

```bash
bash scripts/setup-github-repo.sh
# Windows:
pwsh scripts/setup-github-repo.ps1

```

Before any version bump or release tag:

```bash
bash scripts/pre-release-gate.sh
# Windows:
pwsh scripts/pre-release-gate.ps1

```

</details>

## Security

### Dependabot alerts (one-time setup)

> [!IMPORTANT]
> **`[HUMAN]`** Enable **Dependabot alerts** and **Dependabot security updates** under **Settings → Code security and analysis**.

See [`docs/SECURITY_TRIAGE.md`](docs/SECURITY_TRIAGE.md) for the full setup and weekly triage checklist.

Report vulnerabilities via [`SECURITY.md`](SECURITY.md) (private reporting preferred).

Community standards: [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)

<details>
<summary><strong>Weekly CVE triage</strong> — recommended Monday checklist</summary>

`[HUMAN]` runs a weekly triage pass (recommended: Monday):

1. Review Dependabot alerts (Critical/High first)
2. Triage open Dependabot PRs (fix / defer / dismiss)
3. Confirm Trivy + CodeQL CI green after merges

</details>

---

*Stacks & layout*

## Supported Stacks

<table>
  <thead>
    <tr>
      <th>Stack</th>
      <th>Guide</th>
      <th>Example</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><img src="https://img.shields.io/badge/Web-PWA-646cff?style=flat-square" alt="Web / PWA" /></td>
      <td><a href="modules/web/MODULE.md"><code>modules/web/MODULE.md</code></a></td>
      <td><a href="examples/web/"><code>examples/web/</code></a></td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Python-API-3776AB?style=flat-square" alt="Python" /></td>
      <td><a href="modules/python/MODULE.md"><code>modules/python/MODULE.md</code></a></td>
      <td><a href="examples/python/"><code>examples/python/</code></a></td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Android-F--Droid-3DDC84?style=flat-square" alt="Android / F-Droid" /></td>
      <td><a href="modules/android/MODULE.md"><code>modules/android/MODULE.md</code></a></td>
      <td><a href="examples/android/"><code>examples/android/</code></a></td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/Lightroom-Plugin-31A8FF?style=flat-square" alt="Lightroom plugin" /></td>
      <td><a href="modules/lightroom/MODULE.md"><code>modules/lightroom/MODULE.md</code></a></td>
      <td><a href="examples/lightroom/"><code>examples/lightroom/</code></a></td>
    </tr>
  </tbody>
</table>

<p>
  <strong>Optional stacks:</strong>
  <img src="https://img.shields.io/badge/Node-stack%20picker-2ea44f?style=flat-square" alt="Node" />
  <img src="https://img.shields.io/badge/Rust-optional-656d76?style=flat-square" alt="Rust" />
  <img src="https://img.shields.io/badge/Go-optional-656d76?style=flat-square" alt="Go" />
  — optional stacks in <a href="docs/OPTIONAL_STACKS.md"><code>docs/OPTIONAL_STACKS.md</code></a>.
</p>

Machine-readable catalog: [`TEMPLATE_INDEX.json`](TEMPLATE_INDEX.json)

## Repository Layout

<details>
<summary><strong>Folder map</strong> — docs, modules, examples, scripts</summary>

```
docs/           Agent docs only (NOT the public website) — see docs/WEB_PROJECT_LAYOUT.md
modules/        Stack-specific agent rules (activate matching stack only)
examples/       Golden Path reference implementations
examples/web/   PWA source; dist/ published via GitHub Actions
scripts/        Init, update checker, validation
.cursor/rules/  Persistent Cursor agent directives
.github/        CI workflows, Dependabot, issue templates

```

</details>

---

*Project meta*

## Contributing

MIT licensed. See [`CONTRIBUTING.md`](CONTRIBUTING.md).

Template maintainers: [`docs/MAINTAINING_THE_TEMPLATE.md`](docs/MAINTAINING_THE_TEMPLATE.md)

## GitHub About

Repo description draft for the short About preview: [`docs/GITHUB_ABOUT.md`](docs/GITHUB_ABOUT.md)

## Maintainer Release

Current template version: **0.18.0** (see `.template-version`, Release Please, and `scripts/pre-release-gate.sh`).
