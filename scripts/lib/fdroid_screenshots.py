"""Fail F-Droid listing checks when screenshot files are dummies."""
from __future__ import annotations

import struct
from pathlib import Path

DUMMY_NAME = ("dummy", "placeholder", "sample", "lorem")
SCREEN_DIRS = ("phoneScreenshots", "sevenInchScreenshots", "tenInchScreenshots")
ROOTS = (
    Path("examples") / "android" / "metadata",
    Path("examples") / "android" / "fastlane" / "metadata" / "android",
)


def _png_size(path: Path) -> tuple[int, int] | None:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    width, height = struct.unpack(">II", data[16:24])
    return width, height


def _is_dummy_name(name: str) -> bool:
    lower = name.lower()
    return any(part in lower for part in DUMMY_NAME)


def check_tree(root: Path) -> list[str]:
    errors: list[str] = []
    for base in ROOTS:
        start = root / base
        if not start.is_dir():
            continue
        for folder in start.rglob("*"):
            if not folder.is_dir() or folder.name not in SCREEN_DIRS:
                continue
            for shot in folder.iterdir():
                if not shot.is_file() or shot.name.startswith("."):
                    continue
                rel = shot.relative_to(root).as_posix()
                if _is_dummy_name(shot.name):
                    errors.append(f"dummy screenshot name: {rel}")
                    continue
                size = _png_size(shot)
                if size is None:
                    errors.append(f"screenshot is not a PNG: {rel}")
                elif min(size) <= 8:
                    errors.append(f"dummy screenshot size {size[0]}x{size[1]}: {rel}")
    return errors


def main() -> int:
    errors = check_tree(Path.cwd())
    if errors:
        print("F-Droid screenshot dummy check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("F-Droid screenshot dummy check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
