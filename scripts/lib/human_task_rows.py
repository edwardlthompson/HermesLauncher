"""HUMAN BUILD_PLAN row handlers (init, config, ADR, smoke, release)."""
from __future__ import annotations

import os
import shutil
from pathlib import Path

from human_task_core import (
    AttemptResult,
    append_decision_log,
    git_has_remote,
    run_cmd,
)


def automate_use_template(root: Path, cfg: dict) -> AttemptResult:
    if git_has_remote(root):
        return AttemptResult(0, "git-remote-exists", "Repository already has git remote", False)
    return AttemptResult(
        1, "use-template", "Cannot create GitHub template from local clone; create repo on GitHub first", True
    )


def automate_init_placeholders(root: Path, cfg: dict) -> AttemptResult:
    script = root / "scripts/init-project.sh"
    if not script.is_file():
        return AttemptResult(1, "init-project", "scripts/init-project.sh missing", True)
    cmd = [
        "bash", str(script), "--non-interactive", "--stack", cfg["stack"],
        "--project-name", cfg["project_name"], "--purpose", cfg["purpose"],
    ]
    code, tail = run_cmd(root, cmd)
    if code == 0:
        return AttemptResult(0, "init-project", "Filled INITIALIZATION_PROMPT via init-project", False)
    return AttemptResult(1, "init-project", tail or f"init-project exit {code}", True)


def automate_informational(_root: Path, _cfg: dict, method: str) -> AttemptResult:
    return AttemptResult(0, method, "Informational step satisfied for autonomous /build", False)


def automate_stack_config(root: Path, cfg: dict) -> AttemptResult:
    sync = root / "scripts/sync-stack-config.py"
    if not sync.is_file():
        return AttemptResult(1, "sync-stack-config", "sync-stack-config.py missing", True)
    repo = cfg.get("release_repo", "")
    donation = os.environ.get("BUILD_DONATION_URL", "https://liberapay.com/example")
    for example, dest in (
        (".app-update.json.example", ".app-update.json"),
        ("donations.json.example", "donations.json"),
    ):
        src, dst = root / example, root / dest
        if src.is_file() and not dst.is_file():
            shutil.copy(src, dst)
    code, tail = run_cmd(root, ["python3", str(sync), str(root), repo, donation])
    if code == 0:
        return AttemptResult(0, "sync-stack-config", "Stack-local config synced from examples", False)
    return AttemptResult(1, "sync-stack-config", tail or f"exit {code}", True)


def automate_approve_adr(root: Path, cfg: dict, task: str) -> AttemptResult:
    if "<!-- no-auto-approve -->" in (root / "BUILD_PLAN.md").read_text(encoding="utf-8"):
        return AttemptResult(1, "approve-adr", "BUILD_PLAN disables auto-approve", True)
    adr_glob = list((root / "docs/adr").glob("0001*.md")) if (root / "docs/adr").is_dir() else []
    if not (adr_glob or (root / "DECISION_LOG.md").is_file()):
        return AttemptResult(1, "approve-adr", "No ADR-0001 or DECISION_LOG found", True)
    append_decision_log(root, f"Autonomous approval for BUILD_PLAN row: {task[:120]}")
    return AttemptResult(0, "approve-adr", "Logged autonomous approval in DECISION_LOG.md", False)


def automate_product_smoke(root: Path, cfg: dict) -> AttemptResult:
    gate = root / "scripts/feature-gate.sh"
    if not gate.is_file():
        return AttemptResult(1, "product-smoke", "feature-gate.sh missing", True)
    code, tail = run_cmd(root, ["bash", str(gate), "--stack", cfg["stack"]])
    if code == 0:
        return AttemptResult(0, "feature-gate", "Product smoke via feature-gate.sh", False)
    return AttemptResult(1, "feature-gate", tail or f"exit {code}", True)


def automate_release_tag(root: Path, _cfg: dict) -> AttemptResult:
    """Prefer an existing GitHub release; else draft from HEAD without pushing tags."""
    code, out = run_cmd(root, ["gh", "release", "list", "--limit", "5"])
    if code != 0:
        return AttemptResult(1, "release-tag", "gh release list failed; product judgment required", True)
    if out.strip():
        return AttemptResult(0, "release-tag", "Release exists; autonomous ack", False)
    tag_code, tag_out = run_cmd(root, ["git", "tag", "-l", "--sort=-v:refname"])
    tags = [line.strip() for line in (tag_out or "").splitlines() if line.strip()] if tag_code == 0 else []
    latest = tags[0] if tags else "v0.0.0-draft"
    head_code, head = run_cmd(root, ["git", "rev-parse", "HEAD"])
    if head_code != 0 or not (head or "").strip():
        return AttemptResult(1, "release-tag", "git rev-parse HEAD failed", True)
    target = head.strip()
    # Draft from HEAD so we do not need a pushed tag (destructive-ops: no git push).
    title = f"{latest} (draft — publish when product-ready)"
    create_code, create_out = run_cmd(
        root,
        [
            "gh",
            "release",
            "create",
            latest,
            "--draft",
            "--target",
            target,
            "--generate-notes",
            "--title",
            title,
        ],
    )
    if create_code == 0:
        return AttemptResult(
            0,
            "release-tag-draft",
            f"Draft release {latest} @ {target[:8]}; human publishes when product-ready",
            False,
        )
    lower = (create_out or "").lower()
    if "already exists" in lower:
        return AttemptResult(0, "release-tag", f"Release {latest} already exists", False)
    # Tag name collision on remote without release: use unique draft name
    draft_name = f"{latest}-hermes-draft"
    create2_code, create2_out = run_cmd(
        root,
        [
            "gh",
            "release",
            "create",
            draft_name,
            "--draft",
            "--target",
            target,
            "--generate-notes",
            "--title",
            title,
        ],
    )
    if create2_code == 0:
        return AttemptResult(
            0,
            "release-tag-draft",
            f"Draft release {draft_name} @ {target[:8]}; human publishes when product-ready",
            False,
        )
    return AttemptResult(1, "release-tag", create2_out or create_out or "draft create failed", True)
