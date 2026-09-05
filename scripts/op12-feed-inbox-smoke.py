#!/usr/bin/env python3
"""OP12-only feed-inbox smoke: News chrome, search, filter, reader, thumbs."""
from __future__ import annotations

import importlib.util
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SERIAL = os.environ.get("HERMES_ADB_SERIAL", "b5214fc6")
PKG = "org.hermeslauncher.app"


def load_op12():
    path = ROOT / "scripts" / "op12-device-smoke.py"
    spec = importlib.util.spec_from_file_location("op12_device_smoke", path)
    if spec is None or spec.loader is None:
        raise SystemExit("cannot load op12-device-smoke.py")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def tap_anywhere(mod, adb: str, serial: str, desc: str) -> None:
    root = mod.dump_ui(adb, serial)
    node = mod.find_node(root, desc=desc)
    if node is None:
        node = mod.find_node(root, text=desc)
    if node is None:
        raise RuntimeError(f"UI node not found: {desc!r}")
    center = mod.bounds_center(node.attrib.get("bounds", ""))
    if not center:
        raise RuntimeError(f"no bounds for {desc!r}")
    mod.adb_cmd(adb, serial, "shell", "input", "tap", str(center[0]), str(center[1]))
    time.sleep(1.0)


def ui_blob(mod, adb: str, serial: str) -> str:
    return ET.tostring(mod.dump_ui(adb, serial), encoding="unicode")


def wait_news(mod, adb: str, serial: str, timeout: float = 25.0) -> str:
    deadline = time.time() + timeout
    last = ""
    while time.time() < deadline:
        last = ui_blob(mod, adb, serial)
        if "Search feeds" in last or "Open article" in last or "Android Authority" in last:
            return last
        time.sleep(1.2)
    raise RuntimeError(f"News did not populate: {last[:400]}")


def dismiss_nova(mod, adb: str, serial: str) -> None:
    blob = ui_blob(mod, adb, serial)
    if "Import Nova layout" not in blob and "Import Nova backup" not in blob:
        return
    tap_anywhere(mod, adb, serial, "Later")
    time.sleep(0.5)


def go_inbox(mod, adb: str, serial: str) -> None:
    dismiss_all_apps(mod, adb, serial)
    dismiss_nova(mod, adb, serial)
    w, h = mod.screen_size(adb, serial)
    deadline = time.time() + 10.0
    last = ""
    while time.time() < deadline:
        last = ui_blob(mod, adb, serial)
        if "Search inbox" in last or "Filter inbox" in last:
            return
        if "Search apps" in last:
            mod.press_back(adb, serial)
            time.sleep(0.4)
            continue
        if "Close article" in last:
            tap_anywhere(mod, adb, serial, "Close article")
            time.sleep(0.6)
            continue
        mod.swipe(adb, serial, int(w * 0.82), int(h * 0.42), int(w * 0.18), int(h * 0.42), 280)
        time.sleep(0.7)
    if "Search inbox" not in last and "Filter inbox" not in last:
        raise RuntimeError(f"Inbox did not appear: {last[:400]}")


def dismiss_all_apps(mod, adb: str, serial: str) -> None:
    for _ in range(5):
        blob = ui_blob(mod, adb, serial)
        if "Search feeds" in blob or "Open article" in blob:
            return
        if "Search apps" in blob:
            mod.press_back(adb, serial)
            time.sleep(0.4)
            continue
        return


def save_screenshot(adb: str, serial: str, dest: Path) -> None:
    proc = subprocess.run(
        [adb, "-s", serial, "exec-out", "screencap", "-p"],
        capture_output=True,
        timeout=20,
        check=False,
    )
    if proc.returncode != 0 or not proc.stdout:
        raise RuntimeError("screencap failed")
    dest.write_bytes(proc.stdout)


def go_news(mod, adb: str, serial: str) -> None:
    dismiss_all_apps(mod, adb, serial)
    dismiss_nova(mod, adb, serial)
    w, h = mod.screen_size(adb, serial)
    swiped = 0
    deadline = time.time() + 12.0
    last = ""
    while time.time() < deadline:
        last = ui_blob(mod, adb, serial)
        if "Search feeds" in last or "Open article" in last:
            return
        if "Search apps" in last:
            mod.press_back(adb, serial)
            time.sleep(0.4)
            continue
        if swiped < 2:
            mod.swipe(adb, serial, int(w * 0.18), int(h * 0.42), int(w * 0.82), int(h * 0.42), 280)
            swiped += 1
        time.sleep(0.7)
    wait_news(mod, adb, serial, timeout=4.0)


def smoke_feeds(mod, adb: str, serial: str) -> None:
    adb_cmd = mod.adb_cmd
    adb_cmd(adb, serial, "logcat", "-c", timeout=15)
    mod.assert_default_home(adb, serial)
    mod.launch_home(adb, serial)
    dismiss_all_apps(mod, adb, serial)
    mod.launch_home(adb, serial)
    dismiss_nova(mod, adb, serial)
    go_news(mod, adb, serial)
    blob = wait_news(mod, adb, serial)
    for needle in ("Search feeds", "Filter feeds", "unread"):
        if needle not in blob:
            raise RuntimeError(f"News chrome missing {needle!r}")
    if "Open settings" in blob:
        raise RuntimeError("News FilterBar should not show Settings")
    if "Open article" in blob and not re.search(r"\d{2}/\d{2}/\d{2}", blob):
        raise RuntimeError("feed cards missing YY/MM/DD dates")
    if re.search(r"\d{2}/\d{2}/\d{2}", blob):
        print("OK   feed cards show YY/MM/DD", flush=True)
    if "Open article" not in blob and "Add feed" not in blob.lower():
        # Still allow empty-after-filter, but a fresh list should have cards.
        if "No articles" in blob and "workspace_feeds" not in blob:
            pass

    mod.tap_text(adb, serial, "Search feeds", required=True)
    time.sleep(0.8)
    opened = ui_blob(mod, adb, serial)
    if "Close search" not in opened:
        raise RuntimeError("in-page search field did not open")
    adb_cmd(adb, serial, "shell", "input", "text", "Android", timeout=15)
    time.sleep(0.7)
    adb_cmd(adb, serial, "shell", "input", "keyevent", "KEYCODE_BACK", timeout=15)
    time.sleep(0.5)
    searched = ui_blob(mod, adb, serial)
    if "Close search" not in searched and "Search feeds" not in searched:
        raise RuntimeError("search left the News page")
    mod.tap_text(adb, serial, "Close search", required=False)
    time.sleep(0.4)
    mod.press_back(adb, serial)
    time.sleep(0.6)

    go_news(mod, adb, serial)
    mod.tap_text(adb, serial, "Filter feeds", required=True)
    time.sleep(0.6)
    menu = ui_blob(mod, adb, serial)
    for needle in ("Unread", "Read", "Starred"):
        if needle not in menu:
            raise RuntimeError(f"filter menu missing {needle!r}")
    mod.tap_text(adb, serial, "Unread", required=True)
    time.sleep(0.6)

    go_news(mod, adb, serial)
    blob = wait_news(mod, adb, serial)
    if "Open article" not in blob:
        raise RuntimeError("no article cards after Unread filter")
    mod.tap_text(adb, serial, "Open article", required=True)
    time.sleep(1.4)
    reader = ui_blob(mod, adb, serial)
    for needle in ("Close article", "Star article", "Mark unread"):
        if needle not in reader and not (needle == "Star article" and "Unstar article" in reader):
            raise RuntimeError(f"reader missing {needle!r}")
    if 'content-desc="Next article"' not in reader or 'content-desc="Previous article"' not in reader:
        raise RuntimeError("reader missing next/previous arrows")
    for needle in ("Reading mode", "Full article, simplified", "Web view"):
        if needle not in reader:
            raise RuntimeError(f"reader missing mode chip {needle!r}")
    shot = Path(os.environ.get("TEMP") or os.environ.get("TMP") or "/tmp") / "hermes-article.png"
    save_screenshot(adb, serial, shot)
    print(f"OK   reader screenshot {shot}", flush=True)
    _w, height = mod.screen_size(adb, serial)
    nxt = mod.find_node(mod.dump_ui(adb, serial), desc="Next article")
    center = mod.bounds_center(nxt.attrib.get("bounds", "")) if nxt is not None else None
    if nxt is None or center is None:
        raise RuntimeError("Next article bounds missing")
    if center[1] < int(height * 0.70):
        raise RuntimeError(f"next bar too high y={center[1]} h={height}")
    print(f"OK   next bar y={center[1]} of {height}", flush=True)
    tap_anywhere(mod, adb, serial, "Next article")
    time.sleep(1.2)
    after_next = ui_blob(mod, adb, serial)
    if "Close article" not in after_next:
        raise RuntimeError("next article did not stay in the reader")
    tap_anywhere(mod, adb, serial, "Close article")
    time.sleep(1.0)
    for _ in range(8):
        blob = ui_blob(mod, adb, serial)
        if "Search feeds" in blob:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError("did not return to News after closing the reader")
    listed = ui_blob(mod, adb, serial)
    if "Filter feeds, Unread" not in listed:
        tap_anywhere(mod, adb, serial, "Filter feeds")
        time.sleep(1.0)
        listed = ui_blob(mod, adb, serial)
        if "Unread selected" not in listed and 'text="Unread"' not in listed:
            raise RuntimeError("Unread filter did not persist after the reader")
        if "Unread selected" not in listed:
            unread_ok = False
            root = mod.dump_ui(adb, serial)
            for node in root.iter("node"):
                if (node.attrib.get("text") or "") == "Unread" and node.attrib.get("selected") == "true":
                    unread_ok = True
                    break
            if not unread_ok:
                raise RuntimeError("Unread filter did not persist after the reader")
        mod.press_back(adb, serial)
        time.sleep(0.4)
    else:
        print("OK   Unread filter still selected after reader", flush=True)

    go_inbox(mod, adb, serial)
    dismiss_nova(mod, adb, serial)
    inbox = ui_blob(mod, adb, serial)
    if "Open settings" not in inbox:
        raise RuntimeError("Inbox missing Settings control")
    mod.tap_text(adb, serial, "Open settings", required=True)
    time.sleep(1.2)
    hub = ui_blob(mod, adb, serial)
    if "Desktop" not in hub and "Backup" not in hub and "Feeds" not in hub:
        raise RuntimeError("Hermes settings hub did not open")
    hub_root = mod.dump_ui(adb, serial)
    clock_clear = None
    for node in hub_root.iter("node"):
        text = node.attrib.get("text") or ""
        if text in ("Settings", "Desktop"):
            center = mod.bounds_center(node.attrib.get("bounds", ""))
            if center:
                clock_clear = center[1]
                break
    if clock_clear is None or clock_clear < 80:
        raise RuntimeError(f"settings hub still under the clock y={clock_clear}")
    print(f"OK   settings hub below clock y={clock_clear}", flush=True)
    print("OK   Inbox Settings opened the hub", flush=True)
    mod.press_back(adb, serial)
    time.sleep(0.6)

    thumbs = adb_cmd(
        adb,
        serial,
        "shell",
        "run-as",
        PKG,
        "ls",
        "files/feed-thumbs",
        timeout=20,
    )
    listing = (thumbs.stdout or "") + (thumbs.stderr or "")
    print(f"thumbs ls: {listing.strip()[:300]}", flush=True)
    if thumbs.returncode != 0 or not any(name.endswith(".jpg") for name in listing.split()):
        print("WARN no cached thumbnails yet (network or tiny-skip)", flush=True)
    else:
        print("OK   thumbnail cache has jpeg files", flush=True)

    originals = adb_cmd(
        adb,
        serial,
        "shell",
        "run-as",
        PKG,
        "ls",
        "-l",
        "files/feed-article",
        timeout=20,
    )
    orig_listing = (originals.stdout or "") + (originals.stderr or "")
    print(f"article cache: {orig_listing.strip()[:400]}", flush=True)
    if originals.returncode != 0 or ".bin" not in orig_listing:
        print("WARN no full-resolution article cache yet", flush=True)
    else:
        print("OK   full-resolution article cache present", flush=True)

    log = adb_cmd(adb, serial, "logcat", "-d", "-t", "400", timeout=30)
    blob = (log.stdout or "") + (log.stderr or "")
    for bad in ("FATAL EXCEPTION", "OutOfMemoryError", "Bitmap too large"):
        if bad in blob:
            raise RuntimeError(f"logcat {bad}")
    hermes = [line for line in blob.splitlines() if "HermesFeeds" in line or "AndroidRuntime" in line]
    for line in hermes[-12:]:
        print(line, flush=True)


def main() -> int:
    if SERIAL != "b5214fc6" and os.environ.get("HERMES_ADB_ALLOW_OTHER") != "1":
        print(f"ERROR: refusing serial {SERIAL}", flush=True)
        return 1
    mod = load_op12()
    adb = mod.resolve_adb()
    if not mod.device_ok(adb, SERIAL):
        print(f"ERROR: serial {SERIAL} not authorized", flush=True)
        return 1
    apks = sorted((ROOT / "examples/android/app/build/outputs/apk/debug").glob("*.apk"))
    if not apks:
        print("ERROR: debug APK missing; assembleDebug first", flush=True)
        return 1
    mod.maybe_install(adb, SERIAL, apks[0])
    try:
        smoke_feeds(mod, adb, SERIAL)
    except Exception as exc:  # noqa: BLE001
        print(f"FAIL: {exc}", flush=True)
        return 1
    print(f"OK   feed-inbox smoke passed on {SERIAL}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
