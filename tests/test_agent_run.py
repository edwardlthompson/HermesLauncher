"""agent-run.py never selects WSL1 System32 bash."""
from __future__ import annotations

import importlib.util
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parent.parent
SCRIPTS = ROOT / "scripts"
LIB = SCRIPTS / "lib"
for path in (LIB, SCRIPTS):
    if str(path) not in sys.path:
        sys.path.insert(0, str(path))

_spec = importlib.util.spec_from_file_location("agent_run", SCRIPTS / "agent-run.py")
assert _spec and _spec.loader
agent_run = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(agent_run)


class ResolveBashTests(unittest.TestCase):
    def test_windows_skips_system32_any_case(self) -> None:
        env = {
            "ProgramFiles": r"C:\missing-pf",
            "ProgramFiles(x86)": r"C:\missing-pf86",
            "LOCALAPPDATA": r"C:\missing-local",
        }
        with (
            patch.object(agent_run.os, "name", "nt"),
            patch.dict(agent_run.os.environ, env, clear=False),
            patch.object(
                agent_run.shutil,
                "which",
                return_value=r"C:\Windows\system32\bash.exe",
            ),
        ):
            self.assertIsNone(agent_run.resolve_bash())


if __name__ == "__main__":
    unittest.main()
