"""
Quick script to list available Gemini models
"""
import os
from google import genai

# Get API key
API_KEY = os.environ.get("GEMINI_API_KEY", "YOUR_API_KEY_HERE")

if API_KEY == "YOUR_API_KEY_HERE":
    print("❌ Please set GEMINI_API_KEY environment variable")
    exit(1)

# Initialize client
client = genai.Client(api_key=API_KEY)

print("=" * 70)
print("Available Gemini Models")
print("=" * 70)
print()

try:
    # List all available models
    models = client.models.list()

    print(f"Found {len(models.models)} models:\n")

    for model in models.models:
        print(f"Name: {model.name}")
        if hasattr(model, 'display_name'):
            print(f"  Display Name: {model.display_name}")
        if hasattr(model, 'description'):
            print(f"  Description: {model.description}")
        if hasattr(model, 'supported_generation_methods'):
            print(f"  Methods: {', '.join(model.supported_generation_methods)}")
        print()

except Exception as error:
    print(f"❌ Error listing models: {error}")
    print()
    print("Trying alternative method...")

    # Try specific image generation models
    image_models = [
        "imagen-3.0-generate-001",
        "imagen-3.0-fast-generate-001",
        "imagegeneration@006",
        "imagegeneration@005",
        "imagegeneration@002"
    ]

    print("\nTesting specific image generation models:")
    for model_name in image_models:
        try:
            model_info = client.models.get(model=model_name)
            print(f"✅ {model_name} - Available")
            if hasattr(model_info, 'supported_generation_methods'):
                print(f"   Methods: {', '.join(model_info.supported_generation_methods)}")
        except Exception as e:
            print(f"❌ {model_name} - {str(e)[:80]}")
