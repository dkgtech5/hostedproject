import pandas as pd
import joblib

# Load dataset
df = pd.read_csv("dataset_full.csv")

# Load saved feature names
feature_names = joblib.load(
    "model/feature_names.pkl"
)

print("=" * 70)
print("FEATURE STATISTICS")
print("=" * 70)

# Calculate statistics
stats = df[feature_names].describe().T[
    ["min", "max", "mean"]
]

# Save statistics
stats.to_csv("feature_statistics.csv")

print("Feature statistics saved successfully.")

print("\nFeature Statistics:")
pd.set_option("display.max_rows", 200)
pd.set_option("display.max_columns", 10)
pd.set_option("display.width", 200)

print(stats)