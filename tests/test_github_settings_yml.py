"""GitHub settings.yml stays aligned with setup-github-repo.sh."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from github_settings_yml import CHECKS, check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class GithubSettingsYmlTests(unittest.TestCase):
    def test_settings_file(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_setup_script_lists_same_checks(self) -> None:
        setup = (ROOT / "scripts" / "setup-github-repo.sh").read_text(encoding="utf-8")
        for name in CHECKS:
            self.assertIn(name, setup)


if __name__ == "__main__":
    unittest.main()
