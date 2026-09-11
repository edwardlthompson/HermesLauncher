#!/usr/bin/env python3
"""OP12 smokes for Sprint 48–49 leftover ADB rows. Uses the installed APK (no assemble)."""
from __future__ import annotations

import importlib.util
import os
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
_SPEC = importlib.util.spec_from_file_location(
    "op12_device_smoke", ROOT / "scripts" / "op12-device-smoke.py"
)
assert _SPEC and _SPEC.loader
s = importlib.util.module_from_spec(_SPEC)
_SPEC.loader.exec_module(s)

SERIAL = os.environ.get("HERMES_ADB_SERIAL", "b5214fc6")


def ui_blob(adb: str, serial: str) -> str:
    return ET.tostring(s.dump_ui(adb, serial), encoding="unicode")


def inbox_ignore_and_search(adb: str, serial: str) -> None:
    s.open_settings(adb, serial, "INBOX")
    s.assert_text(adb, serial, "Ignored apps", scrolls=4)
    s.tap_text(adb, serial, "Ignored apps", required=False, scrolls=2)
    s.launch_home(adb, serial)
    s.assert_text(adb, serial, "Inbox")
    s.press_back(adb, serial)
    time.sleep(0.5)
    w, h = s.screen_size(adb, serial)
    s.swipe(adb, serial, w // 2, int(h * 0.92), w // 2, int(h * 0.22), 420)
    time.sleep(1.0)
    blob = ui_blob(adb, serial)
    if "Search apps" not in blob and "All Apps" not in blob:
        raise RuntimeError("All Apps did not open from dock swipe")
    s.adb_cmd(adb, serial, "shell", "input", "text", "a", timeout=15)
    time.sleep(0.8)
    blob = ui_blob(adb, serial)
    if "Search" not in blob:
        raise RuntimeError("All Apps search field missing after typing")


def feeds_hub_unsubscribe(adb: str, serial: str) -> None:
    s.open_settings(adb, serial)
    s.assert_text(adb, serial, "Home", "Inbox", "Feeds", "System")
    s.open_settings(adb, serial, "FEEDS_SUBS")
    s.assert_text(adb, serial, "Notify all", scrolls=4)
    s.tap_text(adb, serial, "Subscriptions", required=False, scrolls=2)
    blob = ui_blob(adb, serial)
    if "Unsubscribe" not in blob and "Notify all" not in blob:
        raise RuntimeError("Feeds hub missing unsubscribe/notify chrome")


def feeds_drawer_long_press(adb: str, serial: str) -> None:
    s.launch_home(adb, serial)
    w, h = s.screen_size(adb, serial)
    s.swipe(adb, serial, int(w * 0.18), int(h * 0.42), int(w * 0.82), int(h * 0.42), 280)
    time.sleep(1.2)
    s.tap_text(adb, serial, "Open feeds", required=True, scrolls=3)
    time.sleep(1.0)
    blob = ui_blob(adb, serial)
    if "All feeds" not in blob and "Saved" not in blob:
        raise RuntimeError("Feeds drawer did not open")
    root = s.dump_ui(adb, serial)
    target = None
    for node in root.iter("node"):
        text = node.attrib.get("text") or node.attrib.get("content-desc") or ""
        if any(skip in text for skip in ("All feeds", "Saved", "Open feeds", "Feeds")):
            continue
        if text and len(text) > 2:
            target = node
            break
    if target is None:
        raise RuntimeError("no feed/folder row to long-press")
    center = s.bounds_center(target.attrib.get("bounds", ""))
    if not center:
        raise RuntimeError("feed row has no bounds")
    x, y = center
    s.adb_cmd(adb, serial, "shell", "input", "swipe", str(x), str(y), str(x), str(y), "900")
    time.sleep(1.0)
    blob = ui_blob(adb, serial)
    if not any(n in blob for n in ("Unsubscribe", "Move", "Mark read", "Mark as read")):
        raise RuntimeError("feed long-press menu missing mark-read/move/unsubscribe")


def main() -> int:
    serial = SERIAL
    adb = s.resolve_adb()
    if serial != s.DEFAULT_SERIAL and os.environ.get("HERMES_ADB_ALLOW_OTHER") != "1":
        print(f"ERROR: refusing serial {serial}", flush=True)
        return 1
    if not s.device_ok(adb, serial):
        print(f"ERROR: serial {serial} not authorized", flush=True)
        return 1
    if not s.package_installed(adb, serial):
        print("ERROR: Hermes not installed", flush=True)
        return 1
    s.assert_default_home(adb, serial)
    try:
        inbox_ignore_and_search(adb, serial)
        print("OK   inbox ignore chrome + All Apps letter search", flush=True)
        feeds_hub_unsubscribe(adb, serial)
        print("OK   four-group hub + Notify all", flush=True)
        feeds_drawer_long_press(adb, serial)
        print("OK   feeds drawer long-press", flush=True)
    except Exception as exc:  # noqa: BLE001
        print(f"FAIL: {exc}", flush=True)
        return 1
    print(f"OK   OP12 inbox/feeds ADB on {serial}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
