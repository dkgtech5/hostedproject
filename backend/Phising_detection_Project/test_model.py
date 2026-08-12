import joblib
import json


# ------------------------------------------------------------
# Load trained model
# ------------------------------------------------------------

model = joblib.load(
    "model/mlp_phishing_pipeline.pkl"
)


# ------------------------------------------------------------
# Load feature names
# ------------------------------------------------------------

feature_names = joblib.load(
    "model/feature_names.pkl"
)


# ------------------------------------------------------------
# Load metadata
# ------------------------------------------------------------

with open(
    "model/metadata.json",
    "r"
) as file:

    metadata = json.load(file)


# ------------------------------------------------------------
# Display information
# ------------------------------------------------------------

print("=" * 70)
print("MODEL LOADED SUCCESSFULLY")
print("=" * 70)

print(
    "Number of features:",
    len(feature_names)
)

print(
    "Features expected by model:",
    model.named_steps["scaler"].n_features_in_
)

print(
    "\nPipeline:"
)

print(model)

print(
    "\nMetadata:"
)

print(metadata)