from feature_extractor import extract_url_features


url = "https://www.example.com/login?user=test"

features = extract_url_features(url)

print("=" * 70)
print("URL FEATURE EXTRACTION")
print("=" * 70)

print("URL:", url)

print(
    "\nNumber of extracted features:",
    len(features)
)

for name, value in features.items():
    print(
        f"{name:<35} {value}"
    )