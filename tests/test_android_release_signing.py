"""Release APK signing uses GitHub secrets, never an unsigned upload."""
from __future__ import annotations

import os
import stat
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
WRITE = ROOT / "scripts" / "ci-write-android-keystore.sh"


class AndroidReleaseSigningTests(unittest.TestCase):
    def test_release_job_writes_keystore_and_verifies(self) -> None:
        text = (ROOT / ".github" / "workflows" / "release.yml").read_text(encoding="utf-8")
        self.assertIn("scripts/ci-write-android-keystore.sh", text)
        self.assertIn("secrets.ANDROID_KEYSTORE_BASE64", text)
        self.assertIn("secrets.ANDROID_KEYSTORE_PASSWORD", text)
        self.assertIn("secrets.ANDROID_KEY_ALIAS", text)
        self.assertIn("apksigner", text)
        self.assertNotIn("echo \"$ANDROID_KEYSTORE_PASSWORD\"", text)

    def test_write_script_fails_closed_without_secrets(self) -> None:
        env = {key: value for key, value in os.environ.items() if not key.startswith("ANDROID_KEY")}
        proc = subprocess.run(
            ["bash", str(WRITE)],
            cwd=ROOT,
            env=env,
            check=False,
            capture_output=True,
            text=True,
        )
        self.assertEqual(proc.returncode, 1)
        self.assertIn("ANDROID_KEYSTORE_BASE64", proc.stderr)

    def test_local_secret_helper_is_executable(self) -> None:
        helper = ROOT / "scripts" / "set-android-signing-secrets.sh"
        self.assertTrue(helper.is_file())
        self.assertTrue(stat.S_IXUSR & helper.stat().st_mode)


if __name__ == "__main__":
    unittest.main()
