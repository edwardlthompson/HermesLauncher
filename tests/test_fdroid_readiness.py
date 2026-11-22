"""F-Droid source-side listing: version, licenses, scanner-safe AOSP tree."""
from __future__ import annotations

import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
THIRD_PARTY = ROOT / "examples" / "android" / "third_party"
BINARY_SUFFIXES = {".jar", ".aar", ".so", ".apk", ".dex", ".class", ".jnilib"}
ALLOWED_MAVEN = (
    "https://repo1.maven.org/maven2",
    "https://repo.maven.apache.org/maven2",
    "https://plugins.gradle.org/m2",
    "https://maven.google.com",
)
COMMENT_LINE = re.compile(r"^\s*//")
BLOCK_COMMENT = re.compile(r"/\*.*?\*/", re.DOTALL)
MAVEN_BLOCK = re.compile(r"""maven\s*\{\s*url\s+["']([^"']+)["']""")


class FdroidReadinessTests(unittest.TestCase):
    def test_app_version_is_single_sourced(self) -> None:
        path = ROOT / "schemas" / "golden-path" / "app-version.json"
        data = json.loads(path.read_text(encoding="utf-8"))
        self.assertEqual(data["versionName"], "1.0.4")
        self.assertEqual(data["versionCode"], 104)
        gradle = ROOT / "examples/android/app/build.gradle.kts"
        if not gradle.is_file():
            self.skipTest("android example pruned")
        gradle = gradle.read_text(encoding="utf-8")
        self.assertIn("loadAppVersion", gradle)
        self.assertIn("appVersion.code", gradle)
        self.assertIn("appVersion.name", gradle)
        changelog = ROOT / "fastlane/metadata/android/en-US/changelogs/104.txt"
        self.assertTrue(changelog.is_file())
        self.assertGreater(changelog.stat().st_size, 0)

    def test_root_fastlane_listing_files(self) -> None:
        base = ROOT / "fastlane/metadata/android/en-US"
        for name in ("title.txt", "short_description.txt", "full_description.txt"):
            text = (base / name).read_text(encoding="utf-8").strip()
            self.assertTrue(text, name)
            self.assertNotIn("Golden Path FOSS Android stub", text)
            if name == "short_description.txt":
                self.assertLessEqual(len(text), 80)
        icon = base / "images" / "icon.png"
        self.assertTrue(icon.is_file())
        self.assertGreater(icon.stat().st_size, 400)
        shots = base / "images" / "phoneScreenshots"
        self.assertTrue(shots.is_dir())
        self.assertTrue((shots / "README.md").is_file())

    def test_third_party_apache_license_and_notice(self) -> None:
        if not THIRD_PARTY.is_dir():
            self.skipTest("android example pruned")
        for rel in (
            "LICENSE",
            "NOTICE",
            "launcher3/LICENSE",
            "systemui/LICENSE",
        ):
            path = THIRD_PARTY / rel
            self.assertTrue(path.is_file(), rel)
            body = path.read_text(encoding="utf-8")
            if rel.endswith("LICENSE"):
                self.assertIn("Apache License", body)
            else:
                self.assertIn("android-14.0.0_r28", body)

    def test_third_party_has_no_prebuilt_binaries(self) -> None:
        if not THIRD_PARTY.is_dir():
            self.skipTest("android example pruned")
        hits = [
            path.relative_to(ROOT).as_posix()
            for path in THIRD_PARTY.rglob("*")
            if path.is_file() and path.suffix.lower() in BINARY_SUFFIXES
        ]
        self.assertEqual(hits, [])

    def test_unused_aosp_gradle_has_no_unknown_maven(self) -> None:
        if not THIRD_PARTY.is_dir():
            self.skipTest("android example pruned")
        for gradle in THIRD_PARTY.rglob("*.gradle"):
            raw = BLOCK_COMMENT.sub("", gradle.read_text(encoding="utf-8"))
            lines = [ln for ln in raw.splitlines() if not COMMENT_LINE.match(ln)]
            text = "\n".join(lines)
            for url in MAVEN_BLOCK.findall(text):
                self.assertTrue(
                    any(url.startswith(ok) for ok in ALLOWED_MAVEN),
                    f"unknown maven repo {url} in {gradle}",
                )
            self.assertNotIn(".jar", text, gradle)


if __name__ == "__main__":
    unittest.main()
