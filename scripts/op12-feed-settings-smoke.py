#!/usr/bin/env python3
"""OP12-only smoke: News refresh + Settings → Feeds scan/mobile-data chrome."""
from __future__ import annotations

import os
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SERIAL = os.environ.get("HERMES_ADB_SERIAL", "b5214fc6")


def main() -> int:
    if SERIAL != "b5214fc6" and os.environ.get("HERMES_ADB_ALLOW_OTHER") != "1":
        print(f"ERROR: refusing serial {SERIAL}", flush=True)
        return 1
    sys.path.insert(0, str(ROOT / "scripts"))
    import importlib.util

    spec = importlib.util.spec_from_file_location("op12", ROOT / "scripts" / "op12-device-smoke.py")
    if spec is None or spec.loader is None:
        return 1
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    feed_path = ROOT / "scripts" / "op12-feed-inbox-smoke.py"
    feed_spec = importlib.util.spec_from_file_location("feed", feed_path)
    if feed_spec is None or feed_spec.loader is None:
        return 1
    feed = importlib.util.module_from_spec(feed_spec)
    feed_spec.loader.exec_module(feed)
    adb = mod.resolve_adb()
    if not mod.device_ok(adb, SERIAL):
        print(f"ERROR: serial {SERIAL} not authorized", flush=True)
        return 1
    apks = sorted((ROOT / "examples/android/app/build/outputs/apk/debug").glob("*.apk"))
    if not apks:
        print("ERROR: debug APK missing; assembleDebug first", flush=True)
        return 1
    mod.maybe_install(adb, SERIAL, apks[0])
    mod.adb_cmd(
        adb,
        SERIAL,
        "shell",
        "am",
        "start",
        "-n",
        "org.hermeslauncher.app/.HermesSettingsActivity",
        "-e",
        "extra_section",
        "FEEDS",
        timeout=20,
    )
    time.sleep(2.0)
    blob = feed.ui_blob(mod, adb, SERIAL)
    needles = (
        "Scan interval",
        "Download over mobile data",
        "15 min",
        "Show thumbnails",
        "Scan only while charging",
    )
    missing = [name for name in needles if name not in blob]
    if missing:
        print(f"FAIL settings missing {missing!r}", flush=True)
        return 1
    mod.tap_text(adb, SERIAL, "15 min", required=True)
    time.sleep(0.8)
    mod.adb_cmd(
        adb,
        SERIAL,
        "shell",
        "am",
        "start",
        "-n",
        "org.hermeslauncher.app/.HermesSettingsActivity",
        "-e",
        "extra_section",
        "FEEDS",
        timeout=20,
    )
    time.sleep(1.5)
    again = feed.ui_blob(mod, adb, SERIAL)
    if "15 min" not in again:
        print("FAIL 15 min chip gone after relaunch", flush=True)
        return 1
    print("OK   Feeds settings chrome + 15 min chip", flush=True)
    mod.launch_home(adb, SERIAL)
    feed.dismiss_all_apps(mod, adb, SERIAL)
    feed.go_news(mod, adb, SERIAL)
    news = feed.wait_news(mod, adb, SERIAL)
    if "Refresh feeds" not in news:
        print("FAIL News missing Refresh feeds", flush=True)
        return 1
    mod.tap_text(adb, SERIAL, "Refresh feeds", required=True)
    time.sleep(1.5)
    log = mod.adb_cmd(adb, SERIAL, "logcat", "-d", "-t", "80", timeout=20)
    text = (log.stdout or "") + (log.stderr or "")
    if "FATAL EXCEPTION" in text:
        print("FAIL FATAL after refresh", flush=True)
        return 1
    print("OK   refresh control present and tappable", flush=True)
    print(f"OK   feed-settings smoke passed on {SERIAL}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
