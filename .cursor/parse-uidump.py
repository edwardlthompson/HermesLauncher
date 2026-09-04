from pathlib import Path
import re

t = Path(r"C:\Users\edwar\HermesLauncher\.cursor\hermes-uidump.xml").read_text(
    encoding="utf-8", errors="replace"
)
print("AccuBattery", "AccuBattery" in t)
texts = re.findall(r'text="([^"]*)"', t)
print("TEXTS")
for x in texts[:60]:
    if x:
        print(x)
descs = re.findall(r'content-desc="([^"]*)"', t)
print("DESCS")
for x in descs[:30]:
    if x:
        print(x)
