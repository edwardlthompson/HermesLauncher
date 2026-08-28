---
name: update-deps
description: Local-first dependency scan, dry-run, audit, and patch/minor apply. Use when /update-deps, /ship, or /prerelease.
disable-model-invocation: false
---

# Update dependencies (local-first)

See also: `.cursor/commands/update-deps.md`, `scripts/update-deps.sh`

Prefer `depsonar_*` tools when MCP is enabled. After ~60s with no result, use the CLI:

```bash
python3 scripts/agent-run.py update-deps
python3 scripts/agent-run.py update-deps -- --apply
python3 scripts/agent-run.py update-deps -- --audit
```

Dry-run first. Do not git push. Halt on majors, Kotlin >=2.3.30, or HIGH+ audit findings.

Gradle from depsonar: `python3 scripts/agent-run.py apply-depsonar-gradle -- --pins 'id=ver'` then `--apply`. Never write Kotlin `>=2.3.30`.
