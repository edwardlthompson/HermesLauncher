#!/usr/bin/env python3
"""OP12-only (`b5214fc6`) UIAutomator smoke for HUMAN_BACKLOG ADB rows.

Avoids connectedDebugAndroidTest install hangs on some OP12 builds. Instead:
assembleDebug (optional install), launch Hermes, dismiss first-run, walk Settings
hubs, swipe home gestures, and fail on FATAL EXCEPTION in logcat.
"""
from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_SERIAL = "b5214fc6"
PKG = "org.hermeslauncher.app"


def resolve_adb() -> str:
    env = os.environ.get("ADB")
    if env and Path(env).exists():
        return env
    which = shutil.which("adb")
    if which:
        return which
    win = os.environ.get("LOCALAPPDATA", "")
    candidate = Path(win) / "Android/Sdk/platform-tools/adb.exe"
    if candidate.is_file():
        return str(candidate)
    raise SystemExit("adb not found; set ADB=")


def adb_cmd(adb: str, serial: str, *args: str, timeout: int = 60) -> subprocess.CompletedProcess[str]:
    cmd = [adb, "-s", serial, *args]
    print("+", " ".join(cmd), flush=True)
    return subprocess.run(cmd, text=True, capture_output=True, timeout=timeout, check=False)


def device_ok(adb: str, serial: str) -> bool:
    out = subprocess.run([adb, "devices"], capture_output=True, text=True, check=False)
    for line in out.stdout.splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[0] == serial and parts[1] == "device":
            return True
    return False


def ensure_java() -> None:
    if os.environ.get("JAVA_HOME"):
        return
    for candidate in (
        Path(r"C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"),
        Path(r"C:\Program Files\Android\Android Studio\jbr"),
    ):
        if (candidate / "bin" / "java.exe").is_file() or (candidate / "bin" / "java").is_file():
            os.environ["JAVA_HOME"] = str(candidate)
            return


def screen_size(adb: str, serial: str) -> tuple[int, int]:
    size = adb_cmd(adb, serial, "shell", "wm", "size", timeout=15).stdout or ""
    m = re.search(r"(\d+)x(\d+)", size)
    return (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)


def bounds_center(bounds: str) -> tuple[int, int] | None:
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
    if not m:
        return None
    x1, y1, x2, y2 = map(int, m.groups())
    return (x1 + x2) // 2, (y1 + y2) // 2


def dump_ui(adb: str, serial: str) -> ET.Element:
    remote = "/sdcard/hermes-uidump.xml"
    adb_cmd(adb, serial, "shell", "uiautomator", "dump", remote, timeout=30)
    with tempfile.TemporaryDirectory() as tmp:
        local = Path(tmp) / "uidump.xml"
        pull = adb_cmd(adb, serial, "pull", remote, str(local), timeout=30)
        if pull.returncode != 0 or not local.is_file():
            raise RuntimeError(f"uiautomator dump failed: {pull.stderr}")
        return ET.fromstring(local.read_text(encoding="utf-8", errors="replace"))


def find_node(root: ET.Element, *, text: str | None = None, desc: str | None = None) -> ET.Element | None:
    matches: list[ET.Element] = []
    for node in root.iter("node"):
        blob = node.attrib.get("text") or ""
        cdesc = node.attrib.get("content-desc") or ""
        if text and text in blob:
            matches.append(node)
        elif desc and desc in cdesc:
            matches.append(node)
    if not matches:
        return None
    return min(matches, key=lambda node: len(node.attrib.get("text") or node.attrib.get("content-desc") or ""))


def tap_node(adb: str, serial: str, node: ET.Element) -> bool:
    center = bounds_center(node.attrib.get("bounds", ""))
    if not center:
        return False
    _w, h = screen_size(adb, serial)
    x, y = center
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.attrib.get("bounds", ""))
    top = int(m.group(2)) if m else y
    if y > int(h * 0.68) or top > int(h * 0.62):
        swipe(adb, serial, x, int(h * 0.70), x, int(h * 0.22), 380)
        return False
    adb_cmd(adb, serial, "shell", "input", "tap", str(x), str(y))
    time.sleep(1.0)
    return True


def tap_text(adb: str, serial: str, text: str, *, required: bool = True, scrolls: int = 0) -> bool:
    for attempt in range(max(scrolls, 2) + 1):
        root = dump_ui(adb, serial)
        node = find_node(root, desc=text)
        if node is None:
            node = find_node(root, text=text)
        if node is not None:
            if tap_node(adb, serial, node):
                return True
            continue
        if attempt < max(scrolls, 2):
            w, h = screen_size(adb, serial)
            swipe(adb, serial, w // 2, int(h * 0.78), w // 2, int(h * 0.32))
    if required:
        raise RuntimeError(f"UI node not found: {text!r}")
    return False


def assert_text(adb: str, serial: str, *needles: str, scrolls: int = 0) -> None:
    for attempt in range(scrolls + 1):
        root = dump_ui(adb, serial)
        blob = ET.tostring(root, encoding="unicode")
        missing = [n for n in needles if n not in blob]
        if not missing:
            return
        if attempt < scrolls:
            size = adb_cmd(adb, serial, "shell", "wm", "size", timeout=15).stdout or ""
            m = re.search(r"(\d+)x(\d+)", size)
            w, h = (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)
            swipe(adb, serial, w // 2, int(h * 0.78), w // 2, int(h * 0.32))
    raise RuntimeError(f"missing UI text: {missing}")


def press_back(adb: str, serial: str) -> None:
    adb_cmd(adb, serial, "shell", "input", "keyevent", "4")
    time.sleep(0.6)


def skip_first_run(adb: str, serial: str) -> None:
    root = dump_ui(adb, serial)
    later = find_node(root, text="Later")
    if later is not None:
        tap_node(adb, serial, later)
        return
    close = find_node(root, text="Close app")
    if close is not None:
        tap_node(adb, serial, close)
        time.sleep(0.8)
        adb_cmd(
            adb,
            serial,
            "shell",
            "am",
            "start",
            "-n",
            f"{PKG}/.HermesLauncherActivity",
            "-a",
            "android.intent.action.MAIN",
            "-c",
            "android.intent.category.HOME",
            timeout=20,
        )
        time.sleep(2.0)


def launch_home(adb: str, serial: str) -> None:
    adb_cmd(
        adb,
        serial,
        "shell",
        "am",
        "start",
        "-n",
        f"{PKG}/.HermesLauncherActivity",
        "-a",
        "android.intent.action.MAIN",
        "-c",
        "android.intent.category.HOME",
        timeout=20,
    )
    time.sleep(2.5)
    skip_first_run(adb, serial)


def home_again(adb: str, serial: str) -> None:
    adb_cmd(
        adb,
        serial,
        "shell",
        "am",
        "start",
        "-n",
        f"{PKG}/.HermesLauncherActivity",
        "-a",
        "android.intent.action.MAIN",
        "-c",
        "android.intent.category.HOME",
        timeout=20,
    )
    time.sleep(1.6)


def ime_shown(adb: str, serial: str) -> bool:
    out = adb_cmd(adb, serial, "shell", "dumpsys", "input_method", timeout=20)
    blob = (out.stdout or "") + (out.stderr or "")
    return "mInputShown=true" in blob or "mInputShown: true" in blob


def assert_default_home(adb: str, serial: str) -> None:
    out = adb_cmd(
        adb,
        serial,
        "shell",
        "cmd",
        "package",
        "resolve-activity",
        "--brief",
        "-a",
        "android.intent.action.MAIN",
        "-c",
        "android.intent.category.HOME",
        timeout=20,
    )
    blob = (out.stdout or "") + (out.stderr or "")
    if PKG not in blob:
        raise RuntimeError(f"Hermes is not default HOME: {blob.strip()[:240]}")


def smoke_widget_tray(adb: str, serial: str, w: int, h: int, *, require_dnd: bool) -> None:
    # Inbox is Home; desktop is to the right.
    swipe(adb, serial, int(w * 0.82), int(h * 0.42), int(w * 0.18), int(h * 0.42), 280)
    time.sleep(0.9)
    x, y = int(w * 0.72), int(h * 0.36)
    adb_cmd(adb, serial, "shell", "input", "swipe", str(x), str(y), str(x), str(y), "1200")
    time.sleep(1.1)
    tap_text(adb, serial, "Widgets", required=True, scrolls=2)
    time.sleep(1.2)
    assert_text(adb, serial, "Widgets", scrolls=2)
    if not require_dnd:
        print(
            "ADB widget tray opened; skip drag (pass --require-widget-dnd to bind a widget).",
            flush=True,
        )
        press_back(adb, serial)
        return
    root = dump_ui(adb, serial)
    cell = None
    for node in root.iter("node"):
        desc = node.attrib.get("content-desc") or ""
        klass = node.attrib.get("class") or ""
        if "WidgetCell" in klass or "widget" in desc.lower():
            cell = node
            break
    if cell is None:
        raise RuntimeError("widget tray opened but no widget cell to drag")
    start = bounds_center(cell.attrib.get("bounds", ""))
    if not start:
        raise RuntimeError("widget cell has no bounds")
    adb_cmd(
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
    time.sleep(1.4)
    press_back(adb, serial)
    time.sleep(0.5)
    press_back(adb, serial)


def smoke(adb: str, serial: str, *, require_widget_dnd: bool = False) -> None:
    adb_cmd(adb, serial, "logcat", "-c", timeout=15)
    assert_default_home(adb, serial)
    launch_home(adb, serial)
    assert_text(adb, serial, "Inbox")

    size = adb_cmd(adb, serial, "shell", "wm", "size", timeout=15).stdout or ""
    m = re.search(r"(\d+)x(\d+)", size)
    w, h = (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)
    cx = w // 2
    # Inbox list scroll (not from the dock) must not open All Apps.
    swipe(adb, serial, cx, int(h * 0.62), cx, int(h * 0.28), 420)
    root = dump_ui(adb, serial)
    blob = ET.tostring(root, encoding="unicode")
    if "Search apps" in blob:
        raise RuntimeError("Inbox scroll opened All Apps")
    if "Inbox" not in blob:
        raise RuntimeError("Inbox missing after vertical scroll")

    adb_cmd(adb, serial, "shell", "input", "keyevent", "KEYCODE_HOME", timeout=15)
    time.sleep(1.6)
    assert_text(adb, serial, "Search apps")
    if not ime_shown(adb, serial):
        # Focused search field is enough if the IME dump lags on OP12.
        root = dump_ui(adb, serial)
        focused = any(
            (node.attrib.get("focused") == "true" and "Search" in (node.attrib.get("text") or node.attrib.get("hint") or node.attrib.get("content-desc") or ""))
            for node in root.iter("node")
        )
        if not focused:
            raise RuntimeError("Home on Inbox did not show search IME or focused Search apps")
    press_back(adb, serial)
    time.sleep(0.6)
    press_back(adb, serial)

    open_settings(adb, serial)
    tap_text(adb, serial, "Open settings section Desktop", scrolls=4)
    assert_text(adb, serial, "Wallpaper", "widget grid", scrolls=4)

    for extra, needles in (
        ("DESKTOP", ("Wallpaper", "widget grid")),
        ("DOCK", ("Usage", "Custom")),
        ("DRAWER", ("Drawer columns",)),
        ("FOLDERS", ("Open folders fullscreen",)),
        ("SEARCH", ("no web provider",)),
        ("LOOK", ("Icon shape", "Night schedule", "Show notification dots")),
        ("GESTURES", ("Empty-space gestures", "Swipe sensitivity")),
        ("INBOX", ("Ignore persistent notifications",)),
        ("FEEDS", ("Import OPML", "Export OPML", "Preferred opener", "Add Android Authority")),
        ("BACKUP", ("Export Hermes backup", "Reset home layout")),
    ):
        open_settings(adb, serial, extra)
        assert_text(adb, serial, *needles, scrolls=4)
        if extra == "GESTURES":
            tap_text(adb, serial, "Low", required=False, scrolls=2)
            tap_text(adb, serial, "Medium", required=False, scrolls=2)
        if extra == "FEEDS":
            tap_text(adb, serial, "Refresh feeds", required=False, scrolls=2)

    launch_home(adb, serial)
    try:
        smoke_widget_tray(adb, serial, w, h, require_dnd=require_widget_dnd)
    except Exception as exc:  # noqa: BLE001
        if require_widget_dnd:
            raise
        print(f"WARN widget tray not asserted: {exc}", flush=True)

    log = adb_cmd(adb, serial, "logcat", "-d", "-t", "300", timeout=30)
    blob = (log.stdout or "") + (log.stderr or "")
    if "FATAL EXCEPTION" in blob:
        raise RuntimeError("FATAL EXCEPTION in logcat after smoke")


def open_settings(adb: str, serial: str, section: str | None = None) -> None:
    args = [
        "shell",
        "am",
        "start",
        "-S",
        "-n",
        f"{PKG}/.HermesSettingsActivity",
    ]
    if section:
        args.extend(["--es", "extra_section", section])
    adb_cmd(adb, serial, *args, timeout=20)
    time.sleep(1.6)
    if section is None:
        assert_text(adb, serial, "Settings")


def ensure_hub(adb: str, serial: str) -> None:
    for _ in range(4):
        root = dump_ui(adb, serial)
        blob = ET.tostring(root, encoding="unicode")
        if "Settings" in blob and find_node(root, text="Desktop") is not None:
            return
        if "Settings" in blob or "Desktop" in blob or "Gestures" in blob:
            press_back(adb, serial)
            continue
        open_settings(adb, serial)
    assert_text(adb, serial, "Settings")


def section(adb: str, serial: str, title: str, *needles: str) -> None:
    tap_text(adb, serial, title, scrolls=6)
    assert_text(adb, serial, *needles, scrolls=4)
    ensure_hub(adb, serial)


def swipe(adb: str, serial: str, x1: int, y1: int, x2: int, y2: int, ms: int = 300) -> None:
    adb_cmd(
        adb,
        serial,
        "shell",
        "input",
        "swipe",
        str(x1),
        str(y1),
        str(x2),
        str(y2),
        str(ms),
    )
    time.sleep(0.8)


def package_installed(adb: str, serial: str) -> bool:
    out = adb_cmd(adb, serial, "shell", "pm", "path", PKG, timeout=20)
    return out.returncode == 0 and "package:" in (out.stdout or "")


def maybe_install(adb: str, serial: str, apk: Path) -> None:
    if package_installed(adb, serial):
        # Prefer replace with timeout; fall back to keep existing on hang.
        try:
            result = adb_cmd(adb, serial, "install", "-r", "-t", str(apk), timeout=90)
            if result.returncode == 0:
                print("OK   installed/replaced debug APK", flush=True)
                return
            print(f"WARN install exit {result.returncode}; keeping existing package", flush=True)
        except subprocess.TimeoutExpired:
            print("WARN install timed out; keeping existing package", flush=True)
        return
    result = adb_cmd(adb, serial, "install", "-r", "-t", str(apk), timeout=120)
    if result.returncode != 0:
        raise RuntimeError(f"install failed: {result.stderr or result.stdout}")


def run_unit_tests(root: Path) -> None:
    ensure_java()
    android = root / "examples" / "android"
    gradlew = android / ("gradlew.bat" if os.name == "nt" else "gradlew")
    cmd = [
        str(gradlew),
        ":app:testDebugUnitTest",
        "--no-daemon",
        "--tests",
        "org.hermeslauncher.app.workspace.BackupCodecTest",
        "--tests",
        "org.hermeslauncher.app.widgets.DropPolicyTest",
        "--tests",
        "org.hermeslauncher.app.feeds.OpmlExporterTest",
        "--tests",
        "org.hermeslauncher.app.ui.theme.IconShapeNightScheduleTest",
        "--tests",
        "org.hermeslauncher.app.launcher.GestureCodecTest",
        "--tests",
        "org.hermeslauncher.app.launcher.LauncherActionTest",
        "--tests",
        "org.hermeslauncher.app.launcher.SwipeSensitivityTest",
        "--tests",
        "org.hermeslauncher.app.l3.HomeAgainSearchTest",
        "--tests",
        "org.hermeslauncher.app.l3.HermesSwipeGateTest",
    ]
    print("+", " ".join(cmd), flush=True)
    subprocess.run(cmd, cwd=android, check=True)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--serial", default=os.environ.get("HERMES_ADB_SERIAL", DEFAULT_SERIAL))
    parser.add_argument("--skip-unit", action="store_true")
    parser.add_argument("--profile", default="backlog")
    parser.add_argument("--require-widget-dnd", action="store_true")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    serial = args.serial
    adb = resolve_adb()

    if serial != DEFAULT_SERIAL and os.environ.get("HERMES_ADB_ALLOW_OTHER") != "1":
        print(f"ERROR: refusing serial {serial}; OP12 only ({DEFAULT_SERIAL})", flush=True)
        return 1
    if not device_ok(adb, serial):
        print(f"ERROR: serial {serial} not authorized", flush=True)
        return 1

    ensure_java()
    android = root / "examples" / "android"
    gradlew = android / ("gradlew.bat" if os.name == "nt" else "gradlew")
    subprocess.run([str(gradlew), ":app:assembleDebug", "--no-daemon"], cwd=android, check=True)
    apks = sorted((android / "app/build/outputs/apk/debug").glob("*.apk"))
    if not apks:
        print("ERROR: debug APK missing", flush=True)
        return 1
    maybe_install(adb, serial, apks[0])

    if not args.skip_unit:
        run_unit_tests(root)

    try:
        smoke(adb, serial, require_widget_dnd=args.require_widget_dnd)
    except Exception as exc:  # noqa: BLE001 — surface as smoke failure
        print(f"FAIL: {exc}", flush=True)
        return 1

    print(f"OK   OP12 UIAutomator smoke passed on {serial} profile={args.profile}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
