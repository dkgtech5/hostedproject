import joblib
from feature_extractor import extract_url_features


feature_names = joblib.load(
    "model/feature_names.pkl"
)


url = "https://www.example.com/login?user=test"


extracted = extract_url_features(url)


missing = [
    feature
    for feature in feature_names
    if feature not in extracted
]


extra = [
    feature
    for feature in extracted
    if feature not in feature_names
]


print("=" * 70)
print("FEATURE MATCHING")
print("=" * 70)

print(
    "Expected features:",
    len(feature_names)
)

print(
    "Extracted features:",
    len(extracted)
)

print(
    "Missing features:",
    len(missing)
)

print(
    "Extra features:",
    len(extra)
)


print("\n" + "=" * 70)
print("MISSING FEATURES")
print("=" * 70)

for i, feature in enumerate(
    missing,
    start=1
):

    print(
        f"{i:3}. {feature}"
    )


print("\n" + "=" * 70)
print("EXTRA FEATURES")
print("=" * 70)

for feature in extra:

    print(feature)