# ============================================================
# predict_url.py
# Test saved MLP with a URL
# ============================================================

import joblib
import pandas as pd

from feature_extractor import extract_url_features


# ============================================================
# Load model and feature names
# ============================================================

MODEL_PATH = "model/mlp_phishing_pipeline.pkl"
FEATURE_PATH = "model/feature_names.pkl"


print("=" * 70)
print("LOADING MODEL")
print("=" * 70)

model = joblib.load(
    MODEL_PATH
)

feature_names = joblib.load(
    FEATURE_PATH
)

print("Model loaded successfully.")
print("Expected features:", len(feature_names))


# ============================================================
# URL input
# ============================================================

url = input(
    "\nEnter URL to test: "
).strip()


# ============================================================
# Extract features
# ============================================================

print("\nExtracting features...")

features = extract_url_features(
    url
)


# ============================================================
# Arrange features in EXACT training order
# ============================================================

X = pd.DataFrame(
    [[
        features[name]
        for name in feature_names
    ]],
    columns=feature_names
)


print(
    "Features generated:",
    X.shape[1]
)


# ============================================================
# Prediction
# ============================================================

prediction = model.predict(X)[0]

probabilities = model.predict_proba(X)[0]


# ============================================================
# Result
# ============================================================

if prediction == 1:

    result = "PHISHING"

else:

    result = "LEGITIMATE"


print("\n" + "=" * 70)
print("PHISHING DETECTION RESULT")
print("=" * 70)

print(
    "\nURL:",
    url
)

print(
    "\nPrediction:",
    result
)

print(
    "\nLegitimate Probability:",
    f"{probabilities[0] * 100:.2f}%"
)

print(
    "Phishing Probability:",
    f"{probabilities[1] * 100:.2f}%"
)

print("=" * 70)