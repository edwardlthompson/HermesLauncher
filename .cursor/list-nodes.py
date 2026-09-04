from pathlib import Path
import re

t = Path(r"C:\Users\edwar\HermesLauncher\.cursor\hermes-uidump.xml").read_text(
    encoding="utf-8", errors="replace"
)
needle = "AccuBattery"
for m in re.finditer(r"<node[^>]*>", t):
    node = m.group(0)
    if needle not in node:
        continue
    b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', node)
    cls = re.search(r'class="([^"]+)"', node)
    txt = re.search(r'text="([^"]*)"', node)
    if b:
        x1, y1, x2, y2 = map(int, b.groups())
        print(cls.group(1) if cls else "?", txt.group(1) if txt else "", (x1 + x2) // 2, (y1 + y2) // 2)
