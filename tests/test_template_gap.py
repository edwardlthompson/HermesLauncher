"""Template gap reporter: Sacred never-apply, stack filter, fail-soft compare."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
ROOT = Path(__file__).resolve().parent.parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from template_gap import (  # noqa: E402
    classify,
    feature_gaps,
    load_json,
    recommended_apply,
    report,
)


class TemplateGapTests(unittest.TestCase):
    def setUp(self) -> None:
        self.rules = load_json(ROOT / "schemas/golden-path/upgrade-policy.json")["rules"]
        self.catalog = load_json(ROOT / "schemas/golden-path/feature-catalog.json")

    def test_sacred_never_in_apply(self) -> None:
        sacred = (
            "AGENTS.md",
            "docs/spec.md",
            "docs/plan.md",
            "docs/INITIALIZATION_PROMPT.md",
            ".env",
            "scratchpad.md",
            "CODE_REVIEW.md",
            "LICENSE",
            "examples/web/src/about/types.ts",
        )
        for path in sacred:
            self.assertEqual(classify(path, self.rules), "sacred", path)
        mixed = [{"path": p, "policy": classify(p, self.rules)} for p in sacred]
        mixed.append({"path": ".cursor/commands/upgrade.md", "policy": "canon"})
        apply = recommended_apply(mixed)
        self.assertEqual(apply, [".cursor/commands/upgrade.md"])
        for path in sacred:
            self.assertNotIn(path, apply)

    def test_env_example_is_mixed_not_sacred(self) -> None:
        self.assertEqual(classify(".env.example", self.rules), "mixed")
        self.assertEqual(classify("CODE_REVIEW.md.example", self.rules), "canon")
        self.assertEqual(classify("scripts/check-batch-commands.sh", self.rules), "canon")
        self.assertEqual(classify(".github/workflows/ci.yml", self.rules), "mixed")

    def test_stack_filter_omits_android_only(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            gaps_web = feature_gaps(root, self.catalog, "web")
            ids_web = {g["id"] for g in gaps_web}
            self.assertIn("about", ids_web)
            self.assertIn("crash-capture", ids_web)
            self.assertNotIn("display-refresh", ids_web)
            gaps_android = feature_gaps(root, self.catalog, "android")
            self.assertIn("display-refresh", {g["id"] for g in gaps_android})

    def test_present_feature_is_not_a_gap(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            about = root / "examples" / "web" / "src" / "about"
            about.mkdir(parents=True)
            (about / "types.ts").write_text("x", encoding="utf-8")
            ids = {g["id"] for g in feature_gaps(root, self.catalog, "web")}
            self.assertNotIn("about", ids)
            self.assertIn("settings", ids)

    def test_timeout_empty_diff(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / ".template-version").write_text("0.21.0\n", encoding="utf-8")
            (root / ".template-update.json").write_text(
                json.dumps({"upstream": "acme/tpl"}), encoding="utf-8"
            )
            schema = root / "schemas" / "golden-path"
            schema.mkdir(parents=True)
            (schema / "upgrade-policy.json").write_text(
                (ROOT / "schemas/golden-path/upgrade-policy.json").read_text(encoding="utf-8"),
                encoding="utf-8",
            )
            (schema / "feature-catalog.json").write_text(
                (ROOT / "schemas/golden-path/feature-catalog.json").read_text(encoding="utf-8"),
                encoding="utf-8",
            )
            (root / ".cursor").mkdir()
            (root / ".cursor/stack-selection.json").write_text(
                json.dumps({"stack": "web"}), encoding="utf-8"
            )

            def boom(*_a, **_k):
                return [], "timeout or unreachable"

            data = report(root, compare=boom, latest_fn=lambda _u: ("0.26.0", ""))
            self.assertEqual(data["files"], [])
            self.assertEqual(data["apply"], [])
            self.assertIn("timeout", data["warning"])
            self.assertTrue(data["ok"])

    def test_missing_config_skip(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            data = report(Path(tmp), compare=lambda *_a, **_k: ([], ""), latest_fn=lambda _u: ("", ""))
            self.assertFalse(data["ok"])
            self.assertTrue(any("template-version" in s for s in data["skip"]))
            self.assertTrue(any("template-update" in s for s in data["skip"]))
            self.assertEqual(data["files"], [])

    def test_upgrade_command_refuses_do_all(self) -> None:
        cmd = (ROOT / ".cursor/commands/upgrade.md").read_text(encoding="utf-8")
        help_twin = (ROOT / "docs/help/UPGRADE.md").read_text(encoding="utf-8")
        for text in (cmd, help_twin):
            self.assertIn("do all", text)
            self.assertIn("wait", text.lower())
            self.assertIn("check-template-gaps", text)


if __name__ == "__main__":
    unittest.main()
