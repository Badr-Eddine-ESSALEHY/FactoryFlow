from __future__ import annotations

import json
import os
import re
import sys
from pathlib import Path
import requests

ROOT = Path(__file__).parent
CASES = json.loads((ROOT / "expected-transcriptions.json").read_text(encoding="utf-8"))
RUNTIME = os.getenv("OCR_RUNTIME_URL", "http://127.0.0.1:8091")

def tokens(value: str) -> list[str]:
    return re.findall(r"\d+[.,]?\d*|%|---|N/A|[A-Za-zÀ-ÿ]+", value.casefold())

failures = []
for case in CASES:
    path = ROOT / "generated" / f"{case['id']}.jpg"
    with path.open("rb") as stream:
        response = requests.post(f"{RUNTIME}/v1/ocr", files={"file": (path.name, stream, "image/jpeg")}, timeout=60)
    response.raise_for_status()
    actual = response.json()["fullText"]
    expected_tokens = tokens(case["text"])
    actual_tokens = tokens(actual)
    missing = [token for token in expected_tokens if token not in actual_tokens]
    print(f"{case['id']}: expected_tokens={len(expected_tokens)} missing={len(missing)}")
    if missing:
        failures.append({"id": case["id"], "missing": missing, "actual": actual})

if failures:
    print(json.dumps(failures, ensure_ascii=False, indent=2))
    sys.exit(1)
