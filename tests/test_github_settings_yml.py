"""GitHub settings.yml stays aligned with setup-github-repo.sh."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from github_settings_yml import check_repo  # noqa: E402
from required_checks import load_names  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class GithubSettingsYmlTests(unittest.TestCase):
    def test_settings_file(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_setup_script_lists_same_checks(self) -> None:
        names = load_names(ROOT)
        self.assertTrue(names)
        setup = (ROOT / "scripts" / "setup-github-repo.sh").read_text(encoding="utf-8")
        self.assertIn("required_checks.py", setup)
        verify = (ROOT / "scripts" / "verify-branch-protection.sh").read_text(
            encoding="utf-8"
        )
        self.assertIn("required_checks.py", verify)
        for name in names:
            self.assertIn(name, (ROOT / ".github" / "settings.yml").read_text(encoding="utf-8"))

    def test_workflows_publish_required_check_names(self) -> None:
        ci = (ROOT / ".github" / "workflows" / "ci.yml").read_text(encoding="utf-8")
        sec = (ROOT / ".github" / "workflows" / "security.yml").read_text(encoding="utf-8")
        self.assertIn("\n    name: CI\n", ci)
        self.assertIn("\n    name: Security Scan\n", sec)


if __name__ == "__main__":
    unittest.main()
