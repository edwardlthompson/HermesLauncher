"""Plugin pack version must match .template-version."""
from __future__ import annotations

import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parent.parent


class PluginPackVersionTests(unittest.TestCase):
    def test_plugin_json_tracks_template_version(self) -> None:
        version = (ROOT / ".template-version").read_text(encoding="utf-8").strip()
        plugin = json.loads(
            (ROOT / ".cursor-plugin" / "plugin.json").read_text(encoding="utf-8")
        )
        self.assertEqual(plugin.get("version"), version)
        check = (ROOT / "scripts" / "check-template-version-sync.sh").read_text(
            encoding="utf-8"
        )
        self.assertIn("plugin.json", check)
        sync = (ROOT / "scripts" / "sync-template-version.sh").read_text(encoding="utf-8")
        self.assertIn(".cursor-plugin/plugin.json", sync)


if __name__ == "__main__":
    unittest.main()
