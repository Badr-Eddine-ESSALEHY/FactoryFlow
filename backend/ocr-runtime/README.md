# FactoryFlow OCR runtime

This local adapter is the production OCR boundary for FactoryFlow. It runs PaddleOCR 3.x with the French PP-OCRv5 pipeline and exposes only `GET /health` and `POST /v1/ocr`.

The runtime applies EXIF orientation and a non-destructive safe downscale before inference. It does not threshold images, retain uploads, call cloud services, or persist OCR output. The Spring Boot API owns upload validation and the deterministic parser remains the only component that interprets KPI semantics.

Run locally in an isolated Python environment:

```text
pip install -r requirements.txt
uvicorn app:app --host 127.0.0.1 --port 8091
```

Model downloads occur through PaddleOCR on first startup. Production environments must pre-provision the model cache and keep the runtime bound to a private interface.
