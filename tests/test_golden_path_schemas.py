"""Contract tests for shared Golden Path About/crash/donate JSON schemas."""
from __future__ import annotations

import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parent.parent
SCHEMA_DIR = ROOT / "schemas" / "golden-path"


def _validate(instance: object, schema: dict) -> list[str]:
    errors: list[str] = []

    def walk(val: object, spec: dict, path: str) -> None:
        types = spec.get("type")
        if types is not None:
            allowed = types if isinstance(types, list) else [types]
            if val is None:
                ok = "null" in allowed
            elif isinstance(val, bool):
                ok = "boolean" in allowed
            elif isinstance(val, int) and not isinstance(val, bool):
                ok = "integer" in allowed or "number" in allowed
            elif isinstance(val, float):
                ok = "number" in allowed
            elif isinstance(val, str):
                ok = "string" in allowed
            elif isinstance(val, list):
                ok = "array" in allowed
            elif isinstance(val, dict):
                ok = "object" in allowed
            else:
                ok = False
            if not ok:
                errors.append(f"{path}: type {type(val).__name__} not in {allowed}")
                return
        if "enum" in spec and val not in spec["enum"]:
            errors.append(f"{path}: {val!r} not in enum")
        if spec.get("type") == "string" and isinstance(val, str):
            min_len = spec.get("minLength")
            if isinstance(min_len, int) and len(val) < min_len:
                errors.append(f"{path}: shorter than minLength")
        if spec.get("type") == "object" and isinstance(val, dict):
            for key in spec.get("required") or []:
                if key not in val:
                    errors.append(f"{path}: missing {key}")
            if spec.get("additionalProperties") is False:
                extra = set(val) - set((spec.get("properties") or {}))
                if extra:
                    errors.append(f"{path}: extra {sorted(extra)}")
            props = spec.get("properties") or {}
            for key, child in val.items():
                if key in props:
                    walk(child, props[key], f"{path}.{key}")
        if spec.get("type") == "array" and isinstance(val, list):
            item_spec = spec.get("items")
            if isinstance(item_spec, dict):
                for i, item in enumerate(val):
                    walk(item, item_spec, f"{path}[{i}]")

    walk(instance, schema, "$")
    return errors


class GoldenPathSchemaTests(unittest.TestCase):
    def test_donations_examples(self) -> None:
        schema = json.loads((SCHEMA_DIR / "donations.schema.json").read_text(encoding="utf-8"))
        for rel in (
            "donations.json.example",
            "examples/web/public/donations.json.example",
            "examples/android/app/src/main/assets/donations.json.example",
        ):
            path = ROOT / rel
            if not path.is_file():
                continue
            data = json.loads(path.read_text(encoding="utf-8"))
            self.assertEqual(_validate(data, schema), [], rel)

    def test_app_update_examples(self) -> None:
        schema = json.loads((SCHEMA_DIR / "app-update.schema.json").read_text(encoding="utf-8"))
        for rel in (
            ".app-update.json.example",
            "examples/web/public/app-update.json.example",
            "examples/android/app/src/main/assets/app-update.json.example",
        ):
            path = ROOT / rel
            if not path.is_file():
                continue
            data = json.loads(path.read_text(encoding="utf-8"))
            self.assertEqual(_validate(data, schema), [], rel)

    def test_crash_payload_contract(self) -> None:
        schema = json.loads((SCHEMA_DIR / "crash-report.schema.json").read_text(encoding="utf-8"))
        self.assertEqual(_validate({"message": "e", "stack": "s"}, schema), [])
        self.assertTrue(_validate({"message": "e"}, schema))
        self.assertTrue(_validate({"message": "e", "stack": "s", "email": "x"}, schema))


if __name__ == "__main__":
    unittest.main()
