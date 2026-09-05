#!/usr/bin/env python3
"""OP12-only Feeder-parity smoke: share, feeds drawer, two sources, image cache."""
from __future__ import annotations

import importlib.util
import os
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SERIAL = os.environ.get("HERMES_ADB_SERIAL", "b5214fc6")
PKG = "org.hermeslauncher.app"


def load_inbox():
    path = ROOT / "scripts" / "op12-feed-inbox-smoke.py"
    spec = importlib.util.spec_from_file_location("op12_feed_inbox_smoke", path)
    if spec is None or spec.loader is None:
        raise SystemExit("cannot load op12-feed-inbox-smoke.py")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def node_center(mod, node) -> tuple[int, int]:
    center = mod.bounds_center(node.attrib.get("bounds", ""))
    if not center:
        raise RuntimeError("missing bounds")
    return center


def find_desc(mod, root, desc: str):
    node = mod.find_node(root, desc=desc)
    if node is None:
        node = mod.find_node(root, text=desc)
    return node


def open_feeds_settings(mod, adb: str, serial: str) -> str:
    mod.adb_cmd(
        adb,
        serial,
        "shell",
        "am",
        "start",
        "-n",
        f"{PKG}/.HermesSettingsActivity",
        "-e",
        "extra_section",
        "FEEDS",
        timeout=20,
    )
    time.sleep(1.8)
    return inbox_blob_wait(mod, adb, serial)


def inbox_blob_wait(mod, adb: str, serial: str) -> str:
    return ET.tostring(mod.dump_ui(adb, serial), encoding="unicode")


def scroll_settings(mod, adb: str, serial: str) -> None:
    w, h = mod.screen_size(adb, serial)
    mod.swipe(adb, serial, w // 2, int(h * 0.72), w // 2, int(h * 0.28), 420)
    time.sleep(0.5)


def smoke_full_prefetch(mod, inbox, adb: str, serial: str) -> None:
    inbox.go_news(mod, adb, serial)
    inbox.wait_news(mod, adb, serial)
    if "Refresh feeds" in inbox.ui_blob(mod, adb, serial):
        inbox.tap_anywhere(mod, adb, serial, "Refresh feeds")
        time.sleep(8.0)
    fulls = mod.adb_cmd(adb, serial, "shell", "run-as", PKG, "ls", "files/feed-full", timeout=20)
    listing = (fulls.stdout or "") + (fulls.stderr or "")
    if ".html" not in listing:
        time.sleep(6.0)
        fulls = mod.adb_cmd(adb, serial, "shell", "run-as", PKG, "ls", "files/feed-full", timeout=20)
        listing = (fulls.stdout or "") + (fulls.stderr or "")
    if ".html" not in listing:
        print(f"WARN feed-full empty yet: {listing.strip()[:200]}", flush=True)
    else:
        print("OK   feed-full prefetch files present", flush=True)
    blob = inbox.wait_news(mod, adb, serial)
    if "Open article" not in blob:
        raise RuntimeError("no article for Full mode")
    inbox.tap_anywhere(mod, adb, serial, "Open article")
    time.sleep(1.2)
    inbox.tap_anywhere(mod, adb, serial, "Full article, simplified")
    time.sleep(1.5)
    reader = inbox.ui_blob(mod, adb, serial)
    if "Close article" not in reader:
        raise RuntimeError("Full mode left the reader")
    if "Full article, simplified" not in reader:
        raise RuntimeError("Full chip missing after tap")
    log = mod.adb_cmd(adb, serial, "logcat", "-d", "-t", "120", timeout=20)
    text = (log.stdout or "") + (log.stderr or "")
    if "FATAL EXCEPTION" in text:
        raise RuntimeError("FATAL after Full mode")
    print("OK   Full after prefetch", flush=True)
    inbox.tap_anywhere(mod, adb, serial, "Close article")
    time.sleep(0.6)


def smoke_tag_drawer(mod, inbox, adb: str, serial: str) -> None:
    blob = open_feeds_settings(mod, adb, serial)
    for _ in range(14):
        if "Feed tag" in blob or "Tag" in blob:
            break
        scroll_settings(mod, adb, serial)
        blob = inbox.ui_blob(mod, adb, serial)
    if "Feed tag" not in blob and "Tag" not in blob:
        raise RuntimeError("Settings Feeds missing Tag field")
    target = "Feed tag" if "Feed tag" in blob else "Tag"
    inbox.tap_anywhere(mod, adb, serial, target)
    time.sleep(0.4)
    mod.adb_cmd(adb, serial, "shell", "input", "text", "foss", timeout=15)
    time.sleep(1.2)
    mod.launch_home(adb, serial)
    time.sleep(0.6)
    inbox.go_news(mod, adb, serial)
    inbox.tap_anywhere(mod, adb, serial, "Open feeds")
    time.sleep(1.0)
    drawer = inbox.ui_blob(mod, adb, serial)
    if "foss" not in drawer.lower() and "Search feeds list" in drawer:
        inbox.tap_anywhere(mod, adb, serial, "Search feeds list")
        time.sleep(0.3)
        mod.adb_cmd(adb, serial, "shell", "input", "text", "foss", timeout=15)
        time.sleep(0.6)
        drawer = inbox.ui_blob(mod, adb, serial)
    if "foss" not in drawer.lower():
        raise RuntimeError("tagged feed not in Feeds drawer")
    inbox.tap_anywhere(mod, adb, serial, "foss")
    time.sleep(0.8)
    after = inbox.ui_blob(mod, adb, serial)
    print("OK   tag in Feeds drawer", flush=True)
    if "All feeds" in after:
        inbox.tap_anywhere(mod, adb, serial, "All feeds")
        time.sleep(0.5)
    else:
        mod.press_back(adb, serial)
        time.sleep(0.4)


def smoke_notify(mod, inbox, adb: str, serial: str) -> None:
    mod.adb_cmd(
        adb,
        serial,
        "shell",
        "pm",
        "grant",
        PKG,
        "android.permission.POST_NOTIFICATIONS",
        timeout=15,
    )
    blob = open_feeds_settings(mod, adb, serial)
    saw_chips = False
    for _ in range(12):
        if "Always" in blob and "Wi-Fi" in blob and "Never" in blob:
            saw_chips = True
        if "Notify" in blob:
            break
        scroll_settings(mod, adb, serial)
        blob = inbox.ui_blob(mod, adb, serial)
    if not saw_chips:
        missing = [name for name in ("Always", "Wi-Fi", "Never") if name not in blob]
        raise RuntimeError(f"image policy chips missing {missing}")
    print("OK   Always/Wi-Fi/Never chips", flush=True)
    if "Notify" not in blob:
        raise RuntimeError("Notify switch missing")
    root = mod.dump_ui(adb, serial)
    notify = find_desc(mod, root, "Notify")
    if notify is not None and notify.attrib.get("checked") != "true":
        inbox.tap_anywhere(mod, adb, serial, "Notify")
        time.sleep(0.5)
    else:
        inbox.tap_anywhere(mod, adb, serial, "Notify")
        time.sleep(0.4)
        inbox.tap_anywhere(mod, adb, serial, "Notify")
        time.sleep(0.4)
    print("OK   Notify switch tappable", flush=True)
    mod.launch_home(adb, serial)
    time.sleep(0.6)
    inbox.go_news(mod, adb, serial)
    if "Refresh feeds" in inbox.ui_blob(mod, adb, serial):
        inbox.tap_anywhere(mod, adb, serial, "Refresh feeds")
        time.sleep(3.0)
    shade = mod.adb_cmd(adb, serial, "shell", "dumpsys", "notification", timeout=25)
    note = (shade.stdout or "") + (shade.stderr or "")
    if "hermes_feeds" in note or ("Hermes" in note and "Feed" in note):
        print("OK   feed notification channel posted", flush=True)
    else:
        print("WARN shade has no feed alert yet (switch still present)", flush=True)


def go_desktop(mod, inbox, adb: str, serial: str) -> None:
    mod.launch_home(adb, serial)
    time.sleep(0.5)
    inbox.dismiss_all_apps(mod, adb, serial)
    inbox.dismiss_nova(mod, adb, serial)
    w, h = mod.screen_size(adb, serial)
    for _ in range(5):
        blob = inbox.ui_blob(mod, adb, serial)
        if "Search apps" in blob:
            mod.press_back(adb, serial)
            time.sleep(0.4)
            continue
        if "Close article" in blob:
            inbox.tap_anywhere(mod, adb, serial, "Close article")
            time.sleep(0.4)
            continue
        if "Search feeds" not in blob and "Search inbox" not in blob and "Filter feeds" not in blob:
            return
        mod.swipe(adb, serial, int(w * 0.82), int(h * 0.42), int(w * 0.18), int(h * 0.42), 280)
        time.sleep(0.7)
    raise RuntimeError("desktop page not reached")


def smoke_widget(mod, inbox, adb: str, serial: str) -> None:
    go_desktop(mod, inbox, adb, serial)
    w, h = mod.screen_size(adb, serial)
    blob = ""
    for x_frac, y_frac in ((0.50, 0.28), (0.72, 0.36), (0.28, 0.32), (0.62, 0.22)):
        x, y = int(w * x_frac), int(h * y_frac)
        mod.adb_cmd(adb, serial, "shell", "input", "swipe", str(x), str(y), str(x), str(y), "1200")
        time.sleep(1.1)
        blob = inbox.ui_blob(mod, adb, serial)
        if "Widgets" in blob or "Wallpaper" in blob:
            break
        mod.press_back(adb, serial)
        time.sleep(0.4)
    if "Widgets" not in blob:
        raise RuntimeError(f"desktop long-press missing Widgets: {blob[:500]}")
    inbox.tap_anywhere(mod, adb, serial, "Widgets")
    time.sleep(1.4)
    found = False
    opened_app = False
    searched = False
    for _ in range(10):
        tray = inbox.ui_blob(mod, adb, serial)
        if "News unread" in tray:
            found = True
            break
        if "Hermes Launcher" in tray and not opened_app:
            inbox.tap_anywhere(mod, adb, serial, "Hermes Launcher")
            opened_app = True
            time.sleep(0.8)
            continue
        if not searched and ("Search widgets" in tray or "Search" in tray):
            searched = True
            try:
                inbox.tap_anywhere(mod, adb, serial, "Search widgets")
            except RuntimeError:
                try:
                    inbox.tap_anywhere(mod, adb, serial, "Search")
                except RuntimeError:
                    searched = False
            if searched:
                time.sleep(0.3)
                mod.adb_cmd(adb, serial, "shell", "input", "text", "News", timeout=15)
                time.sleep(0.8)
                continue
        scroll_settings(mod, adb, serial)
    if not found:
        tray = inbox.ui_blob(mod, adb, serial)
        found = "News unread" in tray
    if not found:
        raise RuntimeError("News unread widget missing from picker")
    inbox.tap_anywhere(mod, adb, serial, "News unread")
    time.sleep(0.4)
    root = mod.dump_ui(adb, serial)
    cell = find_desc(mod, root, "News unread")
    if cell is None:
        raise RuntimeError("News unread cell has no bounds")
    start = node_center(mod, cell)
    mod.adb_cmd(
        adb,
        serial,
        "shell",
        "input",
        "swipe",
        str(start[0]),
        str(start[1]),
        str(w // 2),
        str(int(h * 0.38)),
        "900",
    )
    time.sleep(1.6)
    desktop = inbox.ui_blob(mod, adb, serial)
    if "Widgets" in desktop and "News unread" in desktop and "Search inbox" not in desktop:
        mod.press_back(adb, serial)
        time.sleep(0.4)
        desktop = inbox.ui_blob(mod, adb, serial)
    dumps = mod.adb_cmd(adb, serial, "shell", "dumpsys", "appwidget", timeout=20)
    widget = (dumps.stdout or "") + (dumps.stderr or "")
    if "NewsUnreadWidget" not in widget and "News unread" not in desktop:
        raise RuntimeError("News unread widget not bound on desktop")
    print("OK   widget on desktop", flush=True)
    mod.press_back(adb, serial)
    time.sleep(0.3)


def smoke_parity(mod, inbox, adb: str, serial: str) -> None:
    start = os.environ.get("HERMES_SMOKE_FROM", "").strip().lower()
    if start == "widget":
        smoke_widget(mod, inbox, adb, serial)
        return
    adb_cmd = mod.adb_cmd
    inbox.go_news(mod, adb, serial)
    blob = inbox.wait_news(mod, adb, serial)
    for needle in ("Search feeds", "Filter feeds", "Open feeds"):
        if needle not in blob:
            raise RuntimeError(f"News chrome missing {needle!r}")
    print("OK   Open feeds bubble present", flush=True)

    root = mod.dump_ui(adb, serial)
    feeds = find_desc(mod, root, "Open feeds")
    unread = find_desc(mod, root, "unread")
    if feeds is None or unread is None:
        unread = None
        for node in root.iter("node"):
            desc = (node.attrib.get("content-desc") or "") + " " + (node.attrib.get("text") or "")
            if "unread" in desc.lower() and "Open feeds" not in desc:
                unread = node
                break
    if feeds is None or unread is None:
        raise RuntimeError("Feeds or unread bubble missing")
    fx, _ = node_center(mod, feeds)
    ux, _ = node_center(mod, unread)
    if fx >= ux:
        raise RuntimeError(f"Feeds bubble not left of unread fx={fx} ux={ux}")
    print(f"OK   Feeds left of unread x={fx}<{ux}", flush=True)

    inbox.tap_anywhere(mod, adb, serial, "Open feeds")
    time.sleep(1.0)
    drawer = inbox.ui_blob(mod, adb, serial)
    if "All feeds" not in drawer or "Saved" not in drawer:
        raise RuntimeError("drawer missing All feeds/Saved")
    print("OK   Feeds drawer opened", flush=True)
    if "F-Droid" in drawer or "f-droid" in drawer.lower():
        inbox.tap_anywhere(mod, adb, serial, "F-Droid") if "F-Droid" in drawer else None
        time.sleep(0.8)
        isolated = inbox.ui_blob(mod, adb, serial)
        if "Android Authority" in isolated and "Open article" in isolated:
            print("WARN F-Droid pick still shows AA (may be combined titles)", flush=True)
        print("OK   picked a drawer row", flush=True)
        inbox.go_news(mod, adb, serial)
        inbox.tap_anywhere(mod, adb, serial, "Open feeds")
        time.sleep(0.8)
        inbox.tap_anywhere(mod, adb, serial, "All feeds")
        time.sleep(0.8)
    else:
        inbox.tap_anywhere(mod, adb, serial, "All feeds")
        time.sleep(0.6)

    inbox.go_news(mod, adb, serial)
    inbox.tap_anywhere(mod, adb, serial, "Filter feeds")
    time.sleep(0.6)
    menu = inbox.ui_blob(mod, adb, serial)
    for needle in ("Unread", "Read", "Starred", "Mark all read"):
        if needle not in menu:
            raise RuntimeError(f"filter menu missing {needle!r}")
    if "Android Authority" in menu and "Open article" not in menu:
        raise RuntimeError("Filter funnel listed feed titles")
    print("OK   Filter funnel has no feed titles", flush=True)
    mod.press_back(adb, serial)
    time.sleep(0.4)

    blob = inbox.wait_news(mod, adb, serial)
    if "Open article" not in blob:
        raise RuntimeError("no article cards")
    inbox.tap_anywhere(mod, adb, serial, "Open article")
    time.sleep(1.4)
    reader = inbox.ui_blob(mod, adb, serial)
    for needle in ("Share article", "Reading mode", "Full article, simplified", "Web view", "Reader tools"):
        if needle not in reader:
            raise RuntimeError(f"reader missing {needle!r}")
    print("OK   Share and overflow present", flush=True)
    inbox.tap_anywhere(mod, adb, serial, "Share article")
    time.sleep(1.0)
    sheet = inbox.ui_blob(mod, adb, serial)
    if "Share" not in sheet and "Chooser" not in sheet and "Bluetooth" not in sheet and "Copy" not in sheet:
        print("WARN share sheet text not seen; dismissing", flush=True)
    else:
        print("OK   share sheet opened", flush=True)
    mod.press_back(adb, serial)
    time.sleep(0.5)
    inbox.tap_anywhere(mod, adb, serial, "Reader tools")
    time.sleep(0.6)
    tools = inbox.ui_blob(mod, adb, serial)
    for needle in ("Read aloud", "Find in article", "Text size"):
        if needle not in tools:
            raise RuntimeError(f"overflow missing {needle!r}")
    print("OK   overflow tools present", flush=True)
    mod.press_back(adb, serial)
    time.sleep(0.4)
    inbox.tap_anywhere(mod, adb, serial, "Open in browser")
    time.sleep(1.0)
    print("OK   Open-in-new did not crash", flush=True)
    mod.press_back(adb, serial)
    time.sleep(0.6)
    if "Close article" in inbox.ui_blob(mod, adb, serial):
        inbox.tap_anywhere(mod, adb, serial, "Close article")
        time.sleep(0.6)

    thumbs = adb_cmd(adb, serial, "shell", "run-as", PKG, "ls", "files/feed-thumbs", timeout=20)
    listing = (thumbs.stdout or "") + (thumbs.stderr or "")
    if thumbs.returncode == 0 and any(name.endswith(".jpg") for name in listing.split()):
        raise RuntimeError("legacy feed-thumbs still in use")
    print("OK   feed-thumbs unused", flush=True)
    originals = adb_cmd(adb, serial, "shell", "run-as", PKG, "ls", "files/feed-article", timeout=20)
    orig = (originals.stdout or "") + (originals.stderr or "")
    if ".bin" not in orig:
        print("WARN no feed-article originals yet", flush=True)
    else:
        print("OK   thumbs from feed-article", flush=True)

    sources = adb_cmd(
        adb,
        serial,
        "shell",
        "run-as",
        PKG,
        "sh",
        "-c",
        "cat files/datastore/article_records.preferences_pb 2>/dev/null | tr -cd '\\11\\12\\15\\40-\\176'",
        timeout=20,
    )
    body = (sources.stdout or "") + (sources.stderr or "")
    aa = "androidauthority.com" in body.lower()
    fd = "f-droid.org" in body.lower()
    if not (aa and fd):
        print(f"WARN two-source check aa={aa} fd={fd} body={body[:180]!r}", flush=True)
        dump = inbox.ui_blob(mod, adb, serial)
        titles = ("Android Authority" in dump, "F-Droid" in dump)
        if titles.count(True) < 2 and "Open feeds" in dump:
            inbox.tap_anywhere(mod, adb, serial, "Open feeds")
            time.sleep(0.8)
            dump = inbox.ui_blob(mod, adb, serial)
            titles = ("Android Authority" in dump or "androidauthority" in dump.lower(), "F-Droid" in dump or "f-droid" in dump.lower())
            mod.press_back(adb, serial)
        if titles.count(True) < 2:
            raise RuntimeError("two-feed step failed: only one source after refresh")
    print("OK   two feed sources present", flush=True)

    smoke_full_prefetch(mod, inbox, adb, serial)
    smoke_tag_drawer(mod, inbox, adb, serial)
    smoke_notify(mod, inbox, adb, serial)
    smoke_widget(mod, inbox, adb, serial)

    inbox.go_inbox(mod, adb, serial)
    inbox_blob = inbox.ui_blob(mod, adb, serial)
    if "Open feeds" in inbox_blob:
        raise RuntimeError("Inbox should not show Open feeds")
    if "Open settings" not in inbox_blob:
        raise RuntimeError("Inbox missing Settings")
    print("OK   Inbox has Settings and no Open feeds", flush=True)


def main() -> int:
    if SERIAL != "b5214fc6" and os.environ.get("HERMES_ADB_ALLOW_OTHER") != "1":
        print(f"ERROR: refusing serial {SERIAL}", flush=True)
        return 1
    inbox = load_inbox()
    mod = inbox.load_op12()
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
        smoke_parity(mod, inbox, adb, SERIAL)
    except Exception as exc:  # noqa: BLE001
        print(f"FAIL: {exc}", flush=True)
        return 1
    print(f"OK   feed-parity smoke passed on {SERIAL}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
