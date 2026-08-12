import joblib

feature_names = joblib.load(
    "model/feature_names.pkl"
)

for i, name in enumerate(feature_names, start=1):
    print(f"{i:3}. {name}")