"""Optional Nix flake must only wrap existing scripts/."""
from __future__ import annotations

from pathlib import Path

FORBIDDEN = ("init-project.sh", "cargo new", "npm create")
REQUIRED = (
    "not a second generator",
    "scripts/verify.sh",
    "scripts/validate-bootstrap.sh",
    "scripts/feature-gate.sh",
    "scripts/update-deps.sh",
)


def check_repo(root: Path) -> list[str]:
    path = root / "flake.nix"
    if not path.is_file():
        return ["MISSING: flake.nix"]
    text = path.read_text(encoding="utf-8")
    errors = [f"missing snippet: {snip}" for snip in REQUIRED if snip not in text]
    for needle in FORBIDDEN:
        if needle in text:
            errors.append(f"flake must not wrap {needle}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Nix flake check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Nix flake check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
