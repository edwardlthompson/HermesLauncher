"""Named GitHub required checks (not a content-hash cache)."""
from __future__ import annotations

import json
import sys
from pathlib import Path

REL = Path(".github") / "required-checks.json"


def load_names(root: Path) -> list[str]:
    data = json.loads((root / REL).read_text(encoding="utf-8"))
    names = data.get("required_status_checks")
    if not isinstance(names, list) or not names:
        raise ValueError("required_status_checks must be a non-empty list")
    return [str(item) for item in names]


def main() -> int:
    root = Path.cwd()
    if "--json" in sys.argv:
        print(json.dumps(load_names(root)))
        return 0
    print("\n".join(load_names(root)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
