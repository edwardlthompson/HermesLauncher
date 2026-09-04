from pathlib import Path
import re
import sys

xml = Path(sys.argv[1]).read_text(encoding="utf-8", errors="replace")
print("---nodes---")
for m in re.finditer(r"<node [^>]*>", xml):
    node = m.group(0)
    text = re.search(r'text="([^"]*)"', node)
    desc = re.search(r'content-desc="([^"]*)"', node)
    bounds = re.search(r'bounds="([^"]*)"', node)
    label = (text.group(1) if text else "") or (desc.group(1) if desc else "")
    if label and bounds:
        print("%s @ %s" % (label, bounds.group(1)))
