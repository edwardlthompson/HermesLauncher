"""Backup, strip, and restore About slices for rust/go/node/python Golden Paths."""

from __future__ import annotations

import argparse
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STUBS = ROOT / "schemas" / "golden-path" / "about-without"
TRACKED = (
    "examples/rust/src/lib.rs",
    "examples/rust/src/main.rs",
    "examples/rust/src/about.rs",
    "examples/go/main.go",
    "examples/go/about.go",
    "examples/go/about_test.go",
    "examples/node/src/app.ts",
    "examples/node/src/about.ts",
    "examples/node/src/about.test.ts",
    "examples/node/src/app.test.ts",
    "examples/python/src/hello/cli.py",
    "examples/python/src/hello/about.py",
    "examples/python/tests/test_about.py",
    "examples/python/tests/test_cli.py",
)


def write_lf(path: Path, text: str) -> None:
    path.write_bytes(text.replace("\r\n", "\n").encode("utf-8"))


def _copy_stub(name: str, dest: Path) -> None:
    write_lf(dest, (STUBS / name).read_text(encoding="utf-8"))


def backup(dest: Path, root: Path) -> None:
    for rel in TRACKED:
        src = root / rel
        if src.is_file():
            out = dest / rel
            out.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, out)


def restore(src: Path, root: Path) -> None:
    copied = False
    for rel in TRACKED:
        bak = src / rel
        if bak.is_file():
            dest = root / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(bak, dest)
            copied = True
    if not copied:
        raise SystemExit("about CLI backup missing")


def _drop(text: str, pattern: str) -> str:
    patched, n = re.subn(pattern, "\n", text, count=1, flags=re.S)
    if n != 1:
        raise SystemExit(f"could not strip About test: {pattern[:48]}")
    return patched


def strip(root: Path) -> None:
    (root / "examples/rust/src/about.rs").unlink(missing_ok=True)
    lib = (root / "examples/rust/src/lib.rs").read_text(encoding="utf-8")
    write_lf(root / "examples/rust/src/lib.rs", lib.replace("pub mod about;\n", ""))
    _copy_stub("rust-main.rs", root / "examples/rust/src/main.rs")
    (root / "examples/go/about.go").unlink(missing_ok=True)
    _copy_stub("go-main.go", root / "examples/go/main.go")
    go_test = root / "examples/go/about_test.go"
    write_lf(go_test, _drop(go_test.read_text(encoding="utf-8"), r"\nfunc TestAboutSummary\([\s\S]*?\n\}\n"))
    for rel in ("examples/node/src/about.ts", "examples/node/src/about.test.ts"):
        (root / rel).unlink(missing_ok=True)
    _copy_stub("node-app.ts", root / "examples/node/src/app.ts")
    node_test = root / "examples/node/src/app.test.ts"
    write_lf(
        node_test,
        _drop(node_test.read_text(encoding="utf-8"), r"\n  it\(\"returns About payload\"[\s\S]*?\n  \}\);\n"),
    )
    for rel in ("examples/python/src/hello/about.py", "examples/python/tests/test_about.py"):
        (root / rel).unlink(missing_ok=True)
    _copy_stub("python-cli.py", root / "examples/python/src/hello/cli.py")
    py_test = root / "examples/python/tests/test_cli.py"
    write_lf(py_test, _drop(py_test.read_text(encoding="utf-8"), r"\n\ndef test_main_about[\s\S]*\Z"))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=("backup", "strip", "restore"))
    parser.add_argument("backup_dir", nargs="?", default="")
    parser.add_argument("--root", default=str(ROOT))
    args = parser.parse_args()
    root = Path(args.root)
    dest = Path(args.backup_dir) if args.backup_dir else Path()
    if args.action == "backup":
        backup(dest, root)
    elif args.action == "strip":
        strip(root)
    else:
        restore(dest, root)


if __name__ == "__main__":
    main()
