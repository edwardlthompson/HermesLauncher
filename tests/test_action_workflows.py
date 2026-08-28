"""actionlint/zizmor wrapper skip vs CI require."""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from action_workflows import check_action_workflows, require_tools  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class ActionWorkflowsTests(unittest.TestCase):
    def test_local_skip_when_missing(self) -> None:
        env = {k: v for k, v in os.environ.items() if k not in {"CI", "GITHUB_ACTIONS", "REQUIRE_ACTION_LINT"}}
        with patch.dict(os.environ, env, clear=True):
            self.assertFalse(require_tools())
            code = check_action_workflows(ROOT, which=lambda _name: None)
            self.assertEqual(code, 0)

    def test_ci_requires_tools(self) -> None:
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "true"}):
            self.assertTrue(require_tools())
            code = check_action_workflows(ROOT, which=lambda _name: None)
            self.assertEqual(code, 1)

    def test_wired_into_quick(self) -> None:
        text = (ROOT / "scripts/validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-action-workflows.sh", text)
        self.assertTrue((ROOT / ".github/zizmor.yml").is_file())
        ci = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
        self.assertIn("actionlint", ci)
        self.assertIn("zizmor", ci)

    def test_actionlint_argv_omits_bare_never(self) -> None:
        seen: list[list[str]] = []

        def runner(cmd: list[str], _cwd: Path) -> tuple[int, str]:
            seen.append(cmd)
            return 0, ""

        code = check_action_workflows(ROOT, which=lambda name: name, runner=runner)
        self.assertEqual(code, 0)
        lint = next(cmd for cmd in seen if cmd and cmd[0] == "actionlint")
        self.assertNotIn("never", lint)


if __name__ == "__main__":
    unittest.main()
