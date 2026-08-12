import pandas as pd
import joblib


# Load dataset
df = pd.read_csv("dataset_full.csv")

# Load saved feature names
feature_names = joblib.load(
    "model/feature_names.pkl"
)

# Dataset features excluding target
dataset_features = [
    col for col in df.columns
    if col != "phishing"
]


print("=" * 70)
print("FEATURE VERIFICATION")
print("=" * 70)

print("Dataset shape:", df.shape)

print(
    "Dataset input features:",
    len(dataset_features)
)

print(
    "Saved feature names:",
    len(feature_names)
)

print(
    "\nFeature lists identical:",
    dataset_features == feature_names
)


print("\nFirst 10 features:")

for i, feature in enumerate(
    feature_names[:10],
    start=1
):
    print(f"{i:3}. {feature}")


print("\nLast 10 features:")

start = len(feature_names) - 9

for i, feature in enumerate(
    feature_names[-10:],
    start=start
):
    print(f"{i:3}. {feature}")