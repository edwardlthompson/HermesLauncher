"""Classify dirty paths into docs / stack / full feature-gate scopes."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from gate_scope import classify, main, retry_stack  # noqa: E402


class ClassifyTests(unittest.TestCase):
    def test_cursor_commands_are_docs(self) -> None:
        d = classify([".cursor/commands/build.md"])
        self.assertEqual(d["mode"], "docs")

    def test_docs_only(self) -> None:
        d = classify(["BUILD_PLAN.md", "docs/FEATURE_MODULES.md", ".cursor/commands/build.md"])
        self.assertEqual(d["mode"], "docs")
        self.assertEqual(d["stacks"], [])

    def test_web_example(self) -> None:
        d = classify(["examples/web/src/about/index.ts"])
        self.assertEqual(d["mode"], "stacks")
        self.assertEqual(d["stacks"], ["web"])

    def test_web_and_android(self) -> None:
        d = classify(["examples/web/src/a.ts", "examples/android/app/src/main/java/Foo.kt"])
        self.assertEqual(d["mode"], "stacks")
        self.assertEqual(d["stacks"], ["android", "web"])

    def test_design_tokens_web_android(self) -> None:
        d = classify(["design-tokens/design-tokens.json"])
        self.assertEqual(d["mode"], "stacks")
        self.assertEqual(d["stacks"], ["android", "web"])

    def test_scripts_are_full(self) -> None:
        d = classify(["scripts/lib/gate_scope.py"])
        self.assertEqual(d["mode"], "full")

    def test_ephemeral_ignored(self) -> None:
        d = classify(["examples/android/.gradle/9.7.0/fileHashes.bin", "BUILD_PLAN.md"])
        self.assertEqual(d["mode"], "docs")

    def test_empty_is_docs(self) -> None:
        self.assertEqual(classify([])["mode"], "docs")

    def test_retry_stack(self) -> None:
        self.assertEqual(retry_stack("web-lint"), "web")
        self.assertEqual(retry_stack("android-test"), "android")
        self.assertIsNone(retry_stack("stack-parallel"))
        self.assertIsNone(retry_stack("hygiene"))

    def test_shell_emit(self) -> None:
        from io import StringIO
        from unittest.mock import patch

        buf = StringIO()
        with patch("sys.stdout", buf):
            self.assertEqual(main(["--shell", "--paths", "examples/node/src/app.ts"]), 0)
        self.assertIn("GATE_MODE=stacks", buf.getvalue())
        self.assertIn("GATE_STACKS=node", buf.getvalue())


class CommandContractTests(unittest.TestCase):
    def test_build_uses_scope_auto(self) -> None:
        build = (ROOT / ".cursor/commands/build.md").read_text(encoding="utf-8")
        self.assertIn("--scope auto", build)
        self.assertIn("watch-agent-gates", build)
        watch = (ROOT / "scripts/watch-agent-gates.sh").read_text(encoding="utf-8")
        self.assertIn("--scope", watch)
        gates = (ROOT / ".cursor/commands/gates.md").read_text(encoding="utf-8")
        self.assertIn("--stack multi", gates)
        fg = (ROOT / "scripts/feature-gate.sh").read_text(encoding="utf-8")
        self.assertIn('STACK" = "docs"', fg)


if __name__ == "__main__":
    unittest.main()
