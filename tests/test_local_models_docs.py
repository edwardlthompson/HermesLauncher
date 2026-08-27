"""LOCAL_MODELS.md must not teach cloud keys, ngrok, or LAN bind."""
from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOC = ROOT / "docs" / "LOCAL_MODELS.md"
BANNED = (
    "CURSOR_API_KEY=",
    "OPENAI_API_KEY",
    "sk-",
    "ngrok",
    "OLLAMA_ORIGINS=*",
    "0.0.0.0",
)


class LocalModelsDocTests(unittest.TestCase):
    def test_doc_exists(self) -> None:
        self.assertTrue(DOC.is_file())

    def test_no_secrets_or_lan(self) -> None:
        text = DOC.read_text(encoding="utf-8")
        lower = text.lower()
        for needle in BANNED:
            self.assertNotIn(needle.lower(), lower, needle)
        self.assertIn("127.0.0.1", text)
        self.assertIn("type this in the GUI", text)


if __name__ == "__main__":
    unittest.main()
