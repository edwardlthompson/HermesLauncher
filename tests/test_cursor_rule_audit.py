"""alwaysApply allowlist and glob frontmatter for Cursor rules."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from cursor_rule_audit import audit_rules, parse_frontmatter  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class CursorRuleAuditTests(unittest.TestCase):
    def test_repo_rules_pass(self) -> None:
        self.assertEqual(audit_rules(ROOT), [])

    def test_parse_always_and_globs(self) -> None:
        text = (
            "---\ndescription: x\nalwaysApply: false\nglobs:\n"
            "  - \"examples/**\"\n---\n# body\n"
        )
        meta = parse_frontmatter(text)
        self.assertEqual(meta["always"], False)
        self.assertEqual(meta["globs"], ["examples/**"])

    def test_unknown_always_apply_fails(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            rules = root / ".cursor" / "rules"
            rules.mkdir(parents=True)
            (rules / "extra.mdc").write_text(
                "---\ndescription: x\nalwaysApply: true\n---\n# x\n",
                encoding="utf-8",
            )
            (rules / "foss-compliance.mdc").write_text(
                "---\ndescription: x\nalwaysApply: true\n---\n# x\n",
                encoding="utf-8",
            )
            (rules / "commercial-compliance.mdc").write_text(
                "---\ndescription: x\nalwaysApply: false\n---\n# x\n",
                encoding="utf-8",
            )
            errors = audit_rules(root)
            self.assertTrue(any("allowlist" in e for e in errors))


if __name__ == "__main__":
    unittest.main()
