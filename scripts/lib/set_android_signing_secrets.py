"""Pipe local keystore.properties + JKS into gh secret set without printing values."""
from __future__ import annotations

import subprocess
import sys
from pathlib import Path


def load_props(path: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def gh_set(name: str, body: bytes) -> None:
    subprocess.run(
        ["gh", "secret", "set", name],
        input=body,
        check=True,
        stdout=subprocess.DEVNULL,
    )


def main(argv: list[str] | None = None) -> int:
    props_path = Path((argv or sys.argv)[1]).resolve()
    props = load_props(props_path)
    needed = ("storeFile", "storePassword", "keyAlias", "keyPassword")
    missing = [key for key in needed if not props.get(key)]
    if missing:
        print(f"FAIL: {props_path} missing {', '.join(missing)}", file=sys.stderr)
        return 1
    store = Path(props["storeFile"])
    if not store.is_absolute():
        store = (props_path.parent / store).resolve()
    if not store.is_file():
        print(f"FAIL: storeFile not found: {store}", file=sys.stderr)
        return 1
    import base64

    gh_set("ANDROID_KEYSTORE_BASE64", base64.standard_b64encode(store.read_bytes()))
    gh_set("ANDROID_KEYSTORE_PASSWORD", props["storePassword"].encode("utf-8"))
    gh_set("ANDROID_KEY_ALIAS", props["keyAlias"].encode("utf-8"))
    gh_set("ANDROID_KEY_PASSWORD", props["keyPassword"].encode("utf-8"))
    print("OK   set ANDROID_KEYSTORE_BASE64 ANDROID_KEYSTORE_PASSWORD ANDROID_KEY_ALIAS ANDROID_KEY_PASSWORD")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
