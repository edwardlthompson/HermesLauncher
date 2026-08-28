"""Shell denylist: git push --force stays denied when only git push is approved."""
from __future__ import annotations

import io
import json
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parent.parent
HOOKS = ROOT / ".cursor" / "hooks"
STATE = ROOT / ".cursor-session-state.json"
if str(HOOKS) not in sys.path:
    sys.path.insert(0, str(HOOKS))

import before_shell_guard as guard  # noqa: E402


def _run(command: str) -> dict:
    stdin = io.StringIO(json.dumps({"command": command}))
    buf = io.StringIO()
    with patch.object(sys, "stdin", stdin), patch.object(sys, "stdout", buf):
        guard.main()
    return json.loads(buf.getvalue())


class ForcePushHonestyTests(unittest.TestCase):
    def setUp(self) -> None:
        self._backup = STATE.read_text(encoding="utf-8") if STATE.is_file() else None
        if STATE.is_file():
            STATE.unlink()

    def tearDown(self) -> None:
        if self._backup is None:
            if STATE.is_file():
                STATE.unlink()
        else:
            STATE.write_text(self._backup, encoding="utf-8")

    def test_force_denied_without_approval(self) -> None:
        result = _run("git push --force origin main")
        self.assertEqual(result.get("permission"), "deny")

    def test_force_denied_when_git_push_approved(self) -> None:
        STATE.write_text(
            json.dumps({"destructive_ops_approved": ["git push"]}),
            encoding="utf-8",
        )
        result = _run("git push --force origin main")
        self.assertEqual(result.get("permission"), "deny")

    def test_force_flag_after_remote_denied_when_approved(self) -> None:
        STATE.write_text(
            json.dumps({"destructive_ops_approved": ["git push"]}),
            encoding="utf-8",
        )
        result = _run("git push origin main --force")
        self.assertEqual(result.get("permission"), "deny")

    def test_short_f_denied_when_git_push_approved(self) -> None:
        STATE.write_text(
            json.dumps({"destructive_ops_approved": ["git push"]}),
            encoding="utf-8",
        )
        result = _run("git push -f origin main")
        self.assertEqual(result.get("permission"), "deny")

    def test_plain_push_denied_without_approval(self) -> None:
        result = _run("git push origin main")
        self.assertEqual(result.get("permission"), "deny")

    def test_plain_push_allowed_when_approved(self) -> None:
        STATE.write_text(
            json.dumps({"destructive_ops_approved": ["git push"]}),
            encoding="utf-8",
        )
        result = _run("git push origin main")
        self.assertEqual(result.get("permission"), "allow")


if __name__ == "__main__":
    unittest.main()
