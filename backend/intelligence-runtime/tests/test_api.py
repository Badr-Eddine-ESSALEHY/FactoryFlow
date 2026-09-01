import unittest

from fastapi.testclient import TestClient

from app import app
from tests.support import request_for


class RuntimeApiTest(unittest.TestCase):
    def test_health_and_analysis_contract(self):
        client = TestClient(app)
        self.assertTrue(client.get("/health").json()["ready"])
        response = client.post("/v1/analyze", json=request_for([5] * 12).model_dump(mode="json"))
        self.assertEqual(200, response.status_code)
        body = response.json()
        self.assertEqual("analysis-1", body["analysisId"])
        self.assertEqual(9, body["kpiDefinitionId"])
        self.assertEqual("COMPLETED", body["anomaly"]["state"])
        self.assertEqual("COMPLETED", body["forecast"]["state"])

    def test_contract_rejects_unknown_fields(self):
        client = TestClient(app)
        payload = request_for([5] * 12).model_dump(mode="json")
        payload["databaseConnection"] = "forbidden"
        self.assertEqual(422, client.post("/v1/analyze", json=payload).status_code)

    def test_contract_rejects_non_chronological_or_resampled_series(self):
        client = TestClient(app)
        payload = request_for([1, 2, 3, 4, 5, 6, 7, 8]).model_dump(mode="json")
        payload["observations"][0], payload["observations"][1] = (
            payload["observations"][1], payload["observations"][0])
        self.assertEqual(422, client.post("/v1/analyze", json=payload).status_code)

        payload = request_for([1, 2, 3, 4, 5, 6, 7, 8]).model_dump(mode="json")
        payload["cadence"]["resamplingApplied"] = True
        payload["cadence"]["resamplingPolicy"] = "LINEAR_INTERPOLATION"
        self.assertEqual(422, client.post("/v1/analyze", json=payload).status_code)


if __name__ == "__main__":
    unittest.main()
