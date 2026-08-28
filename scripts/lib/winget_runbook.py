"""docs/WINGET.md must stay the publish runbook."""
from __future__ import annotations

from pathlib import Path

DOC = Path("docs") / "WINGET.md"
NEEDLES = (
    "generate-winget-manifest.sh",
    "validate-winget-stub.sh",
    "InstallerSha256",
    "microsoft/winget-pkgs",
    "[HUMAN]",
)


def check_repo(root: Path) -> list[str]:
    path = root / DOC
    if not path.is_file():
        return [f"MISSING: {DOC.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    return [f"{DOC.as_posix()} missing {needle}" for needle in NEEDLES if needle not in text]


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Winget runbook check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Winget runbook check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
