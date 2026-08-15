from __future__ import annotations

import io
import os
import time
from contextlib import asynccontextmanager
from typing import Any

from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image, ImageOps
from paddleocr import PaddleOCR
import numpy as np

MAX_UPLOAD_BYTES = int(os.getenv("OCR_MAX_UPLOAD_BYTES", "10485760"))
MAX_IMAGE_EDGE = int(os.getenv("OCR_MAX_IMAGE_EDGE", "2400"))
ALLOWED_TYPES = {"image/jpeg", "image/png", "image/webp"}
ENGINE = "PaddleOCR PP-OCRv5 (fr)"


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.ocr = PaddleOCR(
        lang="fr",
        ocr_version="PP-OCRv5",
        use_doc_orientation_classify=True,
        use_doc_unwarping=False,
        use_textline_orientation=True,
    )
    yield
    app.state.ocr = None


app = FastAPI(title="FactoryFlow OCR Runtime", version="1.0.0", lifespan=lifespan)


@app.get("/health")
def health() -> dict[str, Any]:
    ready = getattr(app.state, "ocr", None) is not None
    return {"ready": ready, "engine": ENGINE, "detail": "ready" if ready else "model-not-loaded"}


@app.post("/v1/ocr")
async def recognize(file: UploadFile = File(...), mimeType: str | None = None) -> dict[str, Any]:
    content_type = (mimeType or file.content_type or "").lower()
    if content_type not in ALLOWED_TYPES:
        raise HTTPException(status_code=415, detail="Unsupported image format")
    payload = await file.read(MAX_UPLOAD_BYTES + 1)
    if not payload or len(payload) > MAX_UPLOAD_BYTES:
        raise HTTPException(status_code=413, detail="Image is empty or too large")

    warnings: list[str] = []
    try:
        image = ImageOps.exif_transpose(Image.open(io.BytesIO(payload))).convert("RGB")
    except Exception as exc:
        raise HTTPException(status_code=400, detail="Invalid image") from exc
    if max(image.size) > MAX_IMAGE_EDGE:
        image.thumbnail((MAX_IMAGE_EDGE, MAX_IMAGE_EDGE), Image.Resampling.LANCZOS)
        warnings.append("IMAGE_DOWNSCALED")

    started = time.perf_counter()
    results = app.state.ocr.predict(np.asarray(image))
    lines: list[dict[str, Any]] = []
    for result in results:
        data = result.json.get("res", result.json)
        texts = data.get("rec_texts", [])
        scores = data.get("rec_scores", [])
        boxes = data.get("rec_boxes", [])
        for index, text in enumerate(texts):
            if not str(text).strip():
                continue
            box = boxes[index].tolist() if hasattr(boxes[index], "tolist") else list(boxes[index])
            lines.append({
                "text": str(text).strip(),
                "confidence": round(float(scores[index]), 6),
                "boundingBox": {"left": int(box[0]), "top": int(box[1]), "right": int(box[2]), "bottom": int(box[3])},
            })
    duration_ms = round((time.perf_counter() - started) * 1000)
    confidence = round(sum(line["confidence"] for line in lines) / len(lines), 6) if lines else None
    if confidence is not None and confidence < 0.70:
        warnings.append("LOW_CONFIDENCE")
    return {
        "fullText": "\n".join(line["text"] for line in lines),
        "lines": lines,
        "confidence": confidence,
        "engine": ENGINE,
        "processingTimeMs": duration_ms,
        "warnings": warnings,
    }
