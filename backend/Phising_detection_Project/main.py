from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import pandas as pd

from feature_extractor import extract_url_features
from auth import router as auth_router


# ============================================================
# FastAPI Application
# ============================================================

app = FastAPI(
    title="AI-Based Phishing Detection API",
    description="MLP-based phishing website detection API",
    version="1.0"
)

app.include_router(auth_router)


# ============================================================
# Load Model
# ============================================================

MODEL_PATH = "model/mlp_phishing_pipeline.pkl"
FEATURE_PATH = "model/feature_names.pkl"

model = joblib.load(MODEL_PATH)
feature_names = joblib.load(FEATURE_PATH)


# ============================================================
# Request Model
# ============================================================

class URLRequest(BaseModel):

    url: str


# ============================================================
# Health Check
# ============================================================

@app.get("/")
def home():

    return {
        "status": "online",
        "message": "AI-Based Phishing Detection API",
        "model": "MLP",
        "features": len(feature_names)
    }


# ============================================================
# Prediction Endpoint
# ============================================================

@app.post("/predict")
def predict(request: URLRequest):

    try:

        # ----------------------------------------------------
        # Extract URL features
        # ----------------------------------------------------

        features = extract_url_features(
            request.url
        )


        # ----------------------------------------------------
        # Check features
        # ----------------------------------------------------

        missing = [
            feature
            for feature in feature_names
            if feature not in features
        ]

        if missing:

            raise HTTPException(
                status_code=500,
                detail={
                    "message": "Missing features",
                    "features": missing
                }
            )


        # ----------------------------------------------------
        # Arrange features in training order
        # ----------------------------------------------------

        X = pd.DataFrame(
            [[
                features[name]
                for name in feature_names
            ]],
            columns=feature_names
        )


        # ----------------------------------------------------
        # Prediction
        # ----------------------------------------------------

        prediction = model.predict(X)[0]

        probabilities = model.predict_proba(X)[0]


        # ----------------------------------------------------
        # Convert prediction
        # ----------------------------------------------------

        if prediction == 1:

            result = "PHISHING"

        else:

            result = "LEGITIMATE"


        # ----------------------------------------------------
        # Return response
        # ----------------------------------------------------

        return {

            "url": request.url,

            "prediction": result,

            "legitimate_probability":
                round(
                    float(probabilities[0]),
                    4
                ),

            "phishing_probability":
                round(
                    float(probabilities[1]),
                    4
                ),

            "security_checks": {
                "https_enabled": bool(features.get("tls_ssl_certificate", 0)),
                "no_ip_in_url": not bool(features.get("domain_in_ip", 0)),
                "no_suspicious_redirect": features.get("qty_redirects", 0) <= 1,
                "shortened_url": bool(features.get("url_shortened", 0))
            }
        }


    except HTTPException:

        raise


    except Exception as e:

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )