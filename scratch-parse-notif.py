"""Extract notification keys for a package from dumpsys notification."""
from pathlib import Path
import re
import sys

text = Path(sys.argv[1]).read_text(encoding="utf-8", errors="replace")
pkg = sys.argv[2] if len(sys.argv) > 2 else ""
keys = []
for m in re.finditer(r"key=([^\s]+)", text):
    key = m.group(1)
    if pkg and pkg not in key:
        continue
    keys.append(key)
print("count=%d" % len(keys))
for key in keys[:40]:
    print(key)
