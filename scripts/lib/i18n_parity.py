"""Web locale keys must have Android string counterparts (dots → underscores)."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
WEB = ROOT / "examples/web/src/locales/en.json"
ANDROID = ROOT / "examples/android/app/src/main/res/values/strings.xml"
ALLOW = ROOT / "schemas/golden-path/i18n-parity.allowlist.json"
NAME = re.compile(r'<string\s+name="([^"]+)"')


def android_names(text: str) -> set[str]:
    return set(NAME.findall(text))


def mapped_name(web_key: str, aliases: dict[str, str]) -> str:
    return aliases.get(web_key, web_key.replace(".", "_"))


def check_files(web: dict, android: set[str], allow: dict) -> list[str]:
    aliases = allow.get("aliases") or {}
    web_only = set(allow.get("web_only") or [])
    android_only = set(allow.get("android_only") or [])
    errors: list[str] = []
    mapped: set[str] = set()
    for key in web:
        if key in web_only:
            continue
        name = mapped_name(key, aliases)
        mapped.add(name)
        if name not in android:
            errors.append(f"web {key!r} → missing Android string {name!r}")
    extra = android - mapped - android_only
    for name in sorted(extra):
        errors.append(f"Android {name!r} has no web locale key")
    unknown_web = web_only - set(web)
    if unknown_web:
        errors.append(f"web_only stale: {sorted(unknown_web)}")
    unknown_android = android_only - android
    if unknown_android:
        errors.append(f"android_only stale: {sorted(unknown_android)}")
    return errors


def check_repo(root: Path | None = None) -> list[str]:
    base = root or ROOT
    web_path = base / "examples/web/src/locales/en.json"
    android_path = base / "examples/android/app/src/main/res/values/strings.xml"
    if not web_path.is_file() or not android_path.is_file():
        return []
    web = json.loads(web_path.read_text(encoding="utf-8"))
    android = android_names(android_path.read_text(encoding="utf-8"))
    allow = json.loads(
        (base / "schemas/golden-path/i18n-parity.allowlist.json").read_text(encoding="utf-8")
    )
    if not isinstance(web, dict):
        return ["en.json must be a JSON object"]
    return check_files(web, android, allow)


def main() -> int:
    root = Path.cwd()
    web_path = root / "examples/web/src/locales/en.json"
    android_path = root / "examples/android/app/src/main/res/values/strings.xml"
    if not web_path.is_file() or not android_path.is_file():
        print("SKIP i18n key parity (web or android locales missing)")
        return 0
    errors = check_repo(root)
    if errors:
        print("\n".join(errors))
        return 1
    print("i18n key parity passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
