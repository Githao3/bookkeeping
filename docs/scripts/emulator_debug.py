"""Small ADB helpers for reproducible native UI and SQLite verification.

Run from the repository root. UI actions resolve labels from a fresh UI tree.
Database copies always force-stop the app before copying DB and WAL together.
"""
import pathlib
import re
import sqlite3
import subprocess
import sys
import xml.etree.ElementTree as ET

OUT = pathlib.Path("docs/screenshots/fix-2026-11-06")
sys.stdout.reconfigure(encoding="utf-8")
sys.stderr.reconfigure(encoding="utf-8")
OUT.mkdir(parents=True, exist_ok=True)
ADB = [r"D:\Android\Sdk\platform-tools\adb.exe", "-s", "127.0.0.1:7555"]


def adb(*args):
    return subprocess.check_output(ADB + list(args))


def tree():
    # MuMu's uiautomator sometimes SIGSEGVs in its shutdown thread after writing
    # a valid tree. Remove the old file, then verify the new XML explicitly.
    adb("shell", "rm", "-f", "/sdcard/coinflow-window.xml")
    result = subprocess.run(ADB + ["shell", "uiautomator", "dump", "/sdcard/coinflow-window.xml"], capture_output=True)
    if result.returncode != 0 and b"dumped to:" not in result.stdout:
        raise RuntimeError(result.stdout.decode(errors="replace") + result.stderr.decode(errors="replace"))
    xml = adb("exec-out", "cat", "/sdcard/coinflow-window.xml")
    ET.fromstring(xml)
    return xml


def capture(name):
    xml = tree()
    (OUT / f"{name}.xml").write_bytes(xml)
    (OUT / f"{name}.png").write_bytes(adb("exec-out", "screencap", "-p"))
    nodes = ET.fromstring(xml).iter("node")
    print([(n.get("text") or n.get("content-desc"), n.get("bounds")) for n in nodes if n.get("text") or n.get("content-desc")])


def tap(label, index=0, desc=False):
    nodes = [n for n in ET.fromstring(tree()).iter("node") if n.get("content-desc" if desc else "text") == label]
    if len(nodes) <= index:
        raise ValueError(f"Visible label not found: {label!r}, index={index}")
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", nodes[index].get("bounds")))
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))


def start():
    print(adb("shell", "am", "start", "-n", "com.bookkeeping.app/.MainActivity").decode())


def database_copy(name):
    adb("shell", "am", "force-stop", "com.bookkeeping.app")
    for suffix in ["", "-wal", "-shm"]:
        result = subprocess.run(ADB + ["exec-out", "run-as", "com.bookkeeping.app", "cat", "databases/bookkeeping_database" + suffix], capture_output=True)
        if result.returncode == 0:
            (OUT / (name + ".db" + suffix)).write_bytes(result.stdout)
    db = sqlite3.connect(OUT / (name + ".db"))
    db.row_factory = sqlite3.Row
    data = {table: [dict(row) for row in db.execute("SELECT * FROM " + table + " ORDER BY id")] for table in ["accounts", "categories", "transactions"]}
    db.close()
    print(data)
    return data
