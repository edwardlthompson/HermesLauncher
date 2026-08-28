"""Tests for local-first dependency updater policy."""
from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from update_deps import (  # noqa: E402
    GRADLE_FALLBACK,
    audit_jobs,
    discover_langs,
    gradle_fallback_message,
    gradle_pin_lines,
    kotlin_guard_error,
    parse_kotlin_versions,
    pre_release_steps,
    release_please_dry_argv,
    upd_argv,
    with_github_token,
)
from update_deps_cli import parse_mode, run_cmd  # noqa: E402


class ModeTests(unittest.TestCase):
    def test_dry_run_default(self) -> None:
        self.assertEqual(parse_mode([]), "dry-run")

    def test_apply_not_implied(self) -> None:
        self.assertNotIn("--apply", upd_argv("dry-run", ["node"]))
        self.assertIn("--dry-run", upd_argv("dry-run", ["node"]))

    def test_apply_lock(self) -> None:
        argv = upd_argv("apply", ["python", "actions"])
        self.assertIn("--apply", argv)
        self.assertIn("--lock", argv)
        self.assertIn("--update-action-shas", argv)
        self.assertNotIn("--dry-run", argv)

    def test_max_bump_minor(self) -> None:
        argv = upd_argv("apply", ["node"])
        i = argv.index("--max-bump")
        self.assertEqual(argv[i + 1], "minor")


class KotlinTests(unittest.TestCase):
    def test_parses_compose_plugin(self) -> None:
        text = 'id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"'
        self.assertEqual(parse_kotlin_versions(text), [(2, 3, 21)])

    def test_blocks_codeql_ceiling(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            gradle = root / "examples" / "android"
            gradle.mkdir(parents=True)
            (gradle / "build.gradle.kts").write_text(
                'id("org.jetbrains.kotlin.plugin.compose") version "2.3.30"\n',
                encoding="utf-8",
            )
            err = kotlin_guard_error(root)
            self.assertIsNotNone(err)
            self.assertIn("2.3.30", err or "")

    def test_allows_current_pin(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            gradle = root / "examples" / "android"
            gradle.mkdir(parents=True)
            (gradle / "build.gradle.kts").write_text(
                'id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"\n',
                encoding="utf-8",
            )
            self.assertIsNone(kotlin_guard_error(root))

    def test_garbage_version_ignored(self) -> None:
        self.assertEqual(parse_kotlin_versions('version "not-a-semver"'), [])


class GradleFallbackTests(unittest.TestCase):
    def test_message_when_android_present(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            path = root / "examples" / "android"
            path.mkdir(parents=True)
            (path / "build.gradle.kts").write_text("// gradle\n", encoding="utf-8")
            self.assertEqual(gradle_fallback_message(root), GRADLE_FALLBACK)

    def test_lists_plugin_pins(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            path = root / "examples" / "android"
            path.mkdir(parents=True)
            (path / "build.gradle.kts").write_text(
                'id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"\n',
                encoding="utf-8",
            )
            self.assertEqual(
                gradle_pin_lines(root),
                ["org.jetbrains.kotlin.plugin.compose=2.3.21"],
            )
            msg = gradle_fallback_message(root) or ""
            self.assertIn("2.3.21", msg)
            self.assertIn(GRADLE_FALLBACK, msg)

    def test_no_message_without_android(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            self.assertIsNone(gradle_fallback_message(Path(tmp)))


class DiscoverTests(unittest.TestCase):
    def test_skip_missing_ecosystems(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            self.assertEqual(discover_langs(Path(tmp)), [])


class AuditTests(unittest.TestCase):
    def test_zero_scanners_empty_jobs(self) -> None:
        with tempfile.TemporaryDirectory() as tmp, patch("update_deps.shutil.which", return_value=None):
            self.assertEqual(audit_jobs(Path(tmp)), [])


class TimeoutTests(unittest.TestCase):
    def test_timeout_fail_closed(self) -> None:
        with patch("update_deps_cli.subprocess.run", side_effect=subprocess.TimeoutExpired(cmd="x", timeout=1)):
            self.assertEqual(run_cmd(["true"], 1, None), 1)


class ReleasePleaseDryTests(unittest.TestCase):
    def test_always_dry_run(self) -> None:
        argv = release_please_dry_argv("owner/repo")
        self.assertIn("--dry-run", argv)
        self.assertIn("--repo-url", argv)
        self.assertNotIn("github-release", argv)

    def test_token_appended_when_present(self) -> None:
        argv = with_github_token(release_please_dry_argv("owner/repo"), "ghp_test")
        self.assertEqual(argv[-2:], ["--token", "ghp_test"])

    def test_token_omitted_when_empty(self) -> None:
        argv = with_github_token(release_please_dry_argv("owner/repo"), "")
        self.assertNotIn("--token", argv)


class PreReleaseStepsTests(unittest.TestCase):
    def test_default_calls_security_triage(self) -> None:
        self.assertIn("check-security-triage", pre_release_steps(False))
        self.assertNotIn("audit-deps", pre_release_steps(False))

    def test_local_skips_wait_ci(self) -> None:
        local = pre_release_steps(True)
        self.assertIn("audit-deps", local)
        self.assertNotIn("check-security-triage", local)
        self.assertNotIn("branch-protection", local)


if __name__ == "__main__":
    unittest.main()
