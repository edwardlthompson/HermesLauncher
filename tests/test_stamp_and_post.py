"""Tests for AGENTS.md stamp and optional post hooks."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from bootstrap_post import ensure_git_repo, install_deps  # noqa: E402
from stamp_project import stamp_agents_md  # noqa: E402


class StampTests(unittest.TestCase):
    def test_inserts_and_updates_card(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "AGENTS.md").write_text(
                "# Agent Router\n\n## Project Overview & Architecture\n\nHello\n",
                encoding="utf-8",
            )
            stamp_agents_md(root, name="Demo", purpose="Notes", stack="web")
            text = (root / "AGENTS.md").read_text(encoding="utf-8")
            self.assertIn("**Product:** Demo", text)
            stamp_agents_md(root, name="Demo2", purpose="Notes", stack="python")
            text = (root / "AGENTS.md").read_text(encoding="utf-8")
            self.assertIn("**Product:** Demo2", text)
            self.assertNotIn("**Product:** Demo\n", text)
            self.assertEqual(text.count("<!-- bootstrap-project-card -->"), 1)


class PostHookTests(unittest.TestCase):
    def test_git_already_present(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / ".git").mkdir()
            self.assertIn("already present", ensure_git_repo(root))

    def test_git_init_fails_without_git(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            with patch("bootstrap_post.tool_present", return_value=False):
                with self.assertRaises(RuntimeError) as ctx:
                    ensure_git_repo(Path(tmp))
            self.assertIn("git is required", str(ctx.exception))

    def test_install_deps_noop(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            notes = install_deps(Path(tmp), "web")
            self.assertTrue(any("no stack" in n for n in notes))


if __name__ == "__main__":
    unittest.main()
