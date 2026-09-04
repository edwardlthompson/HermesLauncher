from pathlib import Path
import re
import sys

t = Path(r"C:\Users\edwar\HermesLauncher\.cursor\hermes-uidump.xml").read_text(
    encoding="utf-8", errors="replace"
)
needle = sys.argv[1]
for m in re.finditer(r"<node[^>]*>", t):
    node = m.group(0)
    if needle not in node:
        continue
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
    if not b:
        continue
    x1, y1, x2, y2 = map(int, b.groups())
    print(f"{(x1 + x2) // 2} {(y1 + y2) // 2}")
    raise SystemExit(0)
raise SystemExit(1)
