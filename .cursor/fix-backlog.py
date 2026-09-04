from pathlib import Path

path = Path(r"C:\Users\edwar\HermesLauncher\HUMAN_BACKLOG.md")
text = path.read_text(encoding="utf-8")
task = "OP12 `b5214fc6` only: contacts deny still searches apps+inbox; one-row cap; no web provider"
reason = (
    "Contacts stay empty without READ_CONTACTS; app-row cap and no-web settings ship. "
    "Live OP12 search chrome still needs a human pass."
)
line = (
    f"| 2026-09-02 | Sprint 24 — Local search | ADB | {task} | {reason} |"
)
out = []
replaced = False
for raw in text.splitlines():
    if "Sprint 24" in raw and "ADB" in raw and "contacts deny" in raw:
        out.append(line)
        replaced = True
    else:
        out.append(raw)
if not replaced:
    out.append(line)
path.write_text("\n".join(out) + "\n", encoding="utf-8")
print("replaced", replaced)
