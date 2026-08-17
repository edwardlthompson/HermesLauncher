"""Plain-English hints for feature-gate failures."""
from __future__ import annotations

import json
import sys
from typing import Any

# stage -> (means, run, why, suggested)
_HINTS: dict[str, tuple[str, str, str, tuple[str, ...]]] = {
    "web-lint": (
        "TypeScript or ESLint found problems in the web Golden Path.",
        "npm run lint in examples/web (then npm run format if that script exists)",
        "Catching type and style errors on the laptop is cheaper than waiting for CI.",
        (
            "fix TypeScript errors in feature scope",
            "run npm run lint in examples/web",
            "run npm run format in examples/web if format script exists",
        ),
    ),
    "web-format": (
        "Web files are not formatted the way CI expects.",
        "npm run format in examples/web",
        "One formatter keeps diffs small and reviewable.",
        ("run npm run format in examples/web",),
    ),
    "web-test": (
        "A web unit or component test failed.",
        "npm test in examples/web",
        "Tests are the proof the Golden Path still works after your change.",
        ("fix failing vitest in src/{feature}/", "run npm test in examples/web"),
    ),
    "web-build": (
        "The web app did not produce a production build.",
        "npm run build in examples/web",
        "A broken build never reaches users; fix it before the pull request.",
        ("fix build errors", "run npm run build in examples/web"),
    ),
    "python-lint": (
        "Ruff found lint issues in the Python Golden Path.",
        "uv run ruff check --fix in examples/python",
        "Auto-fix what you can locally so CI stays green.",
        ("run uv run ruff check --fix in examples/python",),
    ),
    "python-format": (
        "Python files are not formatted the way CI expects.",
        "uv run ruff format in examples/python",
        "One formatter keeps diffs small and reviewable.",
        ("run uv run ruff format in examples/python",),
    ),
    "python-type": (
        "A type checker rejected the Python Golden Path.",
        "Fix mypy/pyright errors in examples/python",
        "Types catch whole classes of bugs before runtime.",
        ("fix mypy/pyright errors in examples/python",),
    ),
    "python-type-mypy": (
        "mypy rejected the Python Golden Path.",
        "Fix mypy errors in examples/python",
        "Types catch whole classes of bugs before runtime.",
        ("fix mypy errors in examples/python",),
    ),
    "python-type-pyright": (
        "pyright rejected the Python Golden Path.",
        "Fix pyright errors in examples/python",
        "Types catch whole classes of bugs before runtime.",
        ("fix pyright errors in examples/python",),
    ),
    "python-test": (
        "A pytest failed in the Python Golden Path.",
        "uv run pytest in examples/python",
        "Tests are the proof the Golden Path still works after your change.",
        ("fix pytest failures in examples/python",),
    ),
    "file-limits": (
        "A file is over the token-economy size budget.",
        "Split oversized static-data or logic files (300 / 150 lines per AGENTS.md)",
        "Small files keep agents accurate and reviews short.",
        ("split oversized static-data/logic files per AGENTS.md limits",),
    ),
    "android-test": (
        "A JUnit test failed in the Android Golden Path.",
        "./gradlew test in examples/android",
        "Tests are the proof the Golden Path still works after your change.",
        ("fix JUnit failures", "run ./gradlew test in examples/android"),
    ),
    "design-cohesion": (
        "Design tokens or i18n keys are out of sync.",
        "bash scripts/check-design-cohesion.sh",
        "Shared tokens keep Web and Android looking like one product.",
        ("run scripts/check-design-cohesion.sh", "use design tokens and i18n keys"),
    ),
    "about-feature-gate": (
        "The About feature slice failed its add/remove check.",
        "bash scripts/verify-about-feature-gate.sh",
        "The About slice is the template for the next vertical feature.",
        ("run scripts/verify-about-feature-gate.sh", "fix About slice regressions"),
    ),
    "rust-fmt": (
        "Rust sources are not rustfmt-clean.",
        "cargo fmt in examples/rust",
        "One formatter keeps diffs small and reviewable.",
        ("run cargo fmt in examples/rust",),
    ),
    "rust-clippy": (
        "Clippy reported warnings treated as errors.",
        "Fix clippy warnings in examples/rust",
        "Clippy catches common Rust mistakes before review.",
        ("fix clippy warnings in examples/rust",),
    ),
    "rust-test": (
        "A Rust test failed.",
        "cargo test in examples/rust",
        "Tests are the proof the Golden Path still works after your change.",
        ("run cargo test in examples/rust",),
    ),
    "go-vet": (
        "go vet found a problem in the Go Golden Path.",
        "go vet ./... in examples/go",
        "vet catches common Go mistakes before review.",
        ("run go vet in examples/go",),
    ),
    "go-fmt": (
        "Go sources are not gofmt-clean.",
        "gofmt -w in examples/go",
        "One formatter keeps diffs small and reviewable.",
        ("run gofmt -w in examples/go",),
    ),
    "go-test": (
        "A Go test failed.",
        "go test ./... in examples/go",
        "Tests are the proof the Golden Path still works after your change.",
        ("run go test in examples/go",),
    ),
    "node-lint": (
        "Lint failed in the Node Golden Path.",
        "Fix lint in examples/node (then npm run format if that script exists)",
        "Catching style errors on the laptop is cheaper than waiting for CI.",
        (
            "fix lint in examples/node",
            "run npm run format in examples/node if format script exists",
        ),
    ),
    "node-format": (
        "Node files are not formatted the way CI expects.",
        "npm run format in examples/node",
        "One formatter keeps diffs small and reviewable.",
        ("run npm run format in examples/node",),
    ),
    "node-test": (
        "A Node test failed.",
        "npm test in examples/node",
        "Tests are the proof the Golden Path still works after your change.",
        ("fix tests in examples/node",),
    ),
    "environment": (
        "A required toolchain is missing from this machine.",
        "Install the tool named in the log, then re-run the gate.",
        "Gates cannot prove the stack works without its compiler or runtime.",
        ("install the missing toolchain and re-run",),
    ),
}

_DEFAULT = (
    "A quality gate failed in the active feature scope.",
    "bash scripts/feature-autofix.sh (or fix the errors shown above)",
    "Local gates are the same checks CI will run on your pull request.",
    ("run scripts/feature-autofix.sh", "fix errors in active feature scope"),
)


def hint_for(stage: str) -> dict[str, Any]:
    means, run, why, suggested = _HINTS.get(stage, _DEFAULT)
    return {"means": means, "run": run, "why": why, "suggested": list(suggested)}


def format_human(stage: str, log_tail: str = "") -> str:
    hint = hint_for(stage or "unknown")
    lines = [
        f"What failed: {stage or 'unknown'}",
        f"What that means: {hint['means']}",
        f"What to run: {hint['run']}",
        f"Why: {hint['why']}",
    ]
    tail = (log_tail or "").strip()
    if tail:
        lines.append(f"Log: {tail[:400]}")
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    as_json = False
    if args and args[0] == "--json":
        as_json = True
        args = args[1:]
    stage = args[0] if args else "unknown"
    log_tail = args[1] if len(args) > 1 else ""
    if as_json:
        payload = hint_for(stage)
        payload["human_hint"] = format_human(stage, log_tail)
        print(json.dumps(payload, indent=2))
    else:
        print(format_human(stage, log_tail))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
