import joblib
from feature_extractor import extract_url_features


# Load exact training feature order
feature_names = joblib.load(
    "model/feature_names.pkl"
)


# Test URL
url = "https://www.example.com/login?user=test"


# Extract
features = extract_url_features(url)


print("=" * 70)
print("FEATURE ORDER CHECK")
print("=" * 70)

print(
    "Expected:",
    len(feature_names)
)

print(
    "Extracted:",
    len(features)
)


# Check every feature
for i, name in enumerate(
    feature_names,
    start=1
):

    if name in features:

        print(
            f"{i:3}. {name:<35} "
            f"{features[name]}"
        )

    else:

        print(
            f"{i:3}. {name:<35} MISSING"
        )