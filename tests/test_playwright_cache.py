"""Playwright lockfile cache-hash skip."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from playwright_cache import check_ci, mark_installed, should_skip_install  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class PlaywrightCacheTests(unittest.TestCase):
    def test_skip_only_when_hash_matches(self) -> None:
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            lock = root / "package-lock.json"
            cache = root / "ms-playwright"
            lock.write_text('{"lockfileVersion": 3}\n', encoding="utf-8")
            self.assertFalse(should_skip_install(lock, cache))
            mark_installed(lock, cache)
            self.assertTrue(should_skip_install(lock, cache))
            lock.write_text('{"lockfileVersion": 2}\n', encoding="utf-8")
            self.assertFalse(should_skip_install(lock, cache))

    def test_ci_uses_cache_hash_skip(self) -> None:
        text = (ROOT / ".github" / "workflows" / "ci.yml").read_text(encoding="utf-8")
        self.assertEqual(check_ci(text), [])
        bootstrap = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-playwright-cache.sh", bootstrap)


if __name__ == "__main__":
    unittest.main()
