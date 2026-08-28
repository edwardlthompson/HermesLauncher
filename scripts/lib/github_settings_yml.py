"""Assert .github/settings.yml matches setup-github-repo.sh defaults."""
from __future__ import annotations

from pathlib import Path

CHECKS = (
    "CI",
    "Security Scan",
    "CodeQL",
    "Repo Hygiene",
    "Feature Gate",
    "Template Upgrade Simulation (Windows)",
)
REL = Path(".github") / "settings.yml"


def check_repo(root: Path) -> list[str]:
    path = root / REL
    if not path.is_file():
        return [f"MISSING: {REL.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors = [f"missing required check: {name}" for name in CHECKS if name not in text]
    if "allow_force_pushes: false" not in text:
        errors.append("allow_force_pushes must be false")
    if "setup-github-repo.sh" not in text:
        errors.append("must point at setup-github-repo.sh as the FOSS apply path")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("GitHub settings.yml check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("GitHub settings.yml check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
