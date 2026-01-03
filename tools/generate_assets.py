"""
Script Name: generate_assets.py
Description: Batch generate test images using Google Gemini Imagen 4 API
Author: SmartCameraApp Team
Date: 2025-12-26
Version: 2.1.0

Modifications:
- 2025-12-26 v2.1.0: Added Gradle integration support (metadata generation, env var config)
- 2025-12-26 v2.0.0: Refactored to English, added logging, improved code structure
"""

import os
import sys
import time
import argparse
import logging
import csv
import json
from datetime import datetime
from typing import List, Tuple, Optional, Dict, Any
from google import genai
from google.genai import types
from google.api_core import exceptions
from PIL import Image
import io


# ==========================================
# Configuration
# ==========================================

class GeneratorConfig:
    """Configuration settings for image generation"""

    # API Configuration
    API_KEY = os.environ.get("GEMINI_API_KEY", "YOUR_API_KEY_HERE")

    # Output Directory (based on Android project structure)
    OUTPUT_DIR = "app/src/debug/assets/test_images"

    # Free Tier Safety Settings
    # Imagen 3 Free Tier: recommended max 2 requests per minute
    COOLDOWN_SECONDS = 30

    # Retry Configuration
    MAX_RETRIES = 3
    RETRY_WAIT_BASE = 60  # Base wait time in seconds for rate limit retry

    # Image Generation Settings
    # Updated to use Imagen 4.0 model
    IMAGE_MODEL = 'imagen-4.0-generate-001'
    ASPECT_RATIO = "3:4"  # Portrait mode for mobile, can change to "9:16"
    OUTPUT_MIME_TYPE = "image/jpeg"
    NUMBER_OF_IMAGES = 1

    # File Handling
    # Gradle integration: Read from environment variable, default to True for incremental builds
    SKIP_EXISTING_FILES = os.environ.get("SKIP_EXISTING_FILES", "true").lower() == "true"

    # Metadata output (for Gradle validation)
    METADATA_FILE = "image-generation-metadata.json"


# ==========================================
# Logging Configuration
# ==========================================

def setup_logging() -> logging.Logger:
    """Setup logging configuration"""
    logger = logging.getLogger('ImageGenerator')
    logger.setLevel(logging.INFO)

    # Console handler with custom format
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)

    formatter = logging.Formatter(
        '%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )
    console_handler.setFormatter(formatter)

    logger.addHandler(console_handler)
    return logger


# Initialize logger
logger = setup_logging()


# ==========================================
# Core Logic
# ==========================================

def setup_client() -> genai.Client:
    """
    Initialize and return Gemini API client

    Returns:
        genai.Client: Initialized Gemini client

    Raises:
        SystemExit: If API key is not configured
    """
    if GeneratorConfig.API_KEY == "YOUR_API_KEY_HERE":
        logger.error("API Key not configured")
        logger.info("Configuration methods:")
        logger.info("  Method 1: Modify API_KEY in GeneratorConfig class")
        logger.info("  Method 2: Set environment variable GEMINI_API_KEY")
        logger.info("  Example (Windows): $env:GEMINI_API_KEY='your_key_here'")
        logger.info("  Example (Linux/Mac): export GEMINI_API_KEY='your_key_here'")
        sys.exit(1)

    logger.info("Gemini API client initialized successfully")
    return genai.Client(api_key=GeneratorConfig.API_KEY)


def parse_input_file(file_path: str) -> List[Tuple[str, str, str, str, str]]:
    """
    Read and parse input file containing generation tasks

    Expected formats per line:
    - Format 1 (basic): "filename.jpg", "prompt description"
    - Format 2 (with aspect): "filename.jpg", "prompt description", "aspect_ratio"
    - Format 3 (full): "filename.jpg", "prompt description", "aspect_ratio", "mime_type"
    - Format 4 (with features): "filename.jpg", "prompt", "aspect_ratio", "mime_type", "key_features"

    Lines starting with # are treated as comments and skipped

    Args:
        file_path: Path to the input text file

    Returns:
        List of tuples containing (filename, prompt, aspect_ratio, mime_type, key_features)
        If optional params not specified, uses defaults from GeneratorConfig or empty string

    Raises:
        SystemExit: If file does not exist
    """
    task_list = []

    if not os.path.exists(file_path):
        logger.error(f"Input file not found: {file_path}")
        sys.exit(1)

    with open(file_path, 'r', encoding='utf-8') as file:
        # Use CSV reader to properly handle quoted fields with commas
        csv_reader = csv.reader(file, quotechar='"', skipinitialspace=True)

        for line_number, row in enumerate(csv_reader, 1):
            # Skip empty rows
            if not row:
                continue

            # Skip comments (first field starts with #)
            if row[0].strip().startswith("#"):
                continue

            # Parse format: "filename", "prompt", ["aspect_ratio"], ["mime_type"], ["key_features"]
            try:
                if len(row) < 2:
                    logger.warning(f"Skipping malformed line {line_number}: insufficient fields")
                    continue

                filename = row[0].strip()
                prompt = row[1].strip()
                aspect_ratio = row[2].strip() if len(row) > 2 else GeneratorConfig.ASPECT_RATIO
                mime_type = row[3].strip() if len(row) > 3 else GeneratorConfig.OUTPUT_MIME_TYPE
                key_features = row[4].strip() if len(row) > 4 else ""

                # Validate aspect ratio format
                valid_ratios = ["1:1", "3:4", "4:3", "9:16", "16:9"]
                if aspect_ratio not in valid_ratios:
                    logger.warning(
                        f"Line {line_number}: Invalid aspect ratio '{aspect_ratio}', "
                        f"using default '{GeneratorConfig.ASPECT_RATIO}'"
                    )
                    aspect_ratio = GeneratorConfig.ASPECT_RATIO

                # Validate MIME type
                valid_mimes = ["image/jpeg", "image/png"]
                if mime_type not in valid_mimes:
                    logger.warning(
                        f"Line {line_number}: Invalid MIME type '{mime_type}', "
                        f"using default '{GeneratorConfig.OUTPUT_MIME_TYPE}'"
                    )
                    mime_type = GeneratorConfig.OUTPUT_MIME_TYPE

                task_list.append((filename, prompt, aspect_ratio, mime_type, key_features))

            except Exception as error:
                logger.warning(f"Error parsing line {line_number}: {error}")

    logger.info(f"Successfully parsed {len(task_list)} tasks from {file_path}")
    return task_list


def generate_metadata(
    task_list: List[Tuple[str, str, str, str, str]],
    success_count: int,
    failure_count: int,
    successful_images: List[str]
) -> None:
    """
    Generate metadata JSON file for Gradle validation

    Args:
        task_list: Original list of generation tasks
        success_count: Number of successful generations
        failure_count: Number of failed generations
        successful_images: List of successfully generated filenames
    """
    metadata: Dict[str, Any] = {
        "generated_at": datetime.now().isoformat(),
        "generator_version": "2.1.0",
        "total_tasks": len(task_list),
        "successful": success_count,
        "failed": failure_count,
        "skip_existing_enabled": GeneratorConfig.SKIP_EXISTING_FILES,
        "images": successful_images,
        "configuration": {
            "model": GeneratorConfig.IMAGE_MODEL,
            "default_aspect_ratio": GeneratorConfig.ASPECT_RATIO,
            "default_mime_type": GeneratorConfig.OUTPUT_MIME_TYPE,
            "cooldown_seconds": GeneratorConfig.COOLDOWN_SECONDS
        }
    }

    metadata_path = os.path.join(GeneratorConfig.OUTPUT_DIR, GeneratorConfig.METADATA_FILE)

    try:
        with open(metadata_path, 'w', encoding='utf-8') as f:
            json.dump(metadata, f, indent=2, ensure_ascii=False)
        logger.info(f"Metadata written to: {metadata_path}")
    except Exception as error:
        logger.warning(f"Failed to write metadata file: {error}")


def generate_image_safe(
    client: genai.Client,
    filename: str,
    prompt: str,
    aspect_ratio: str = None,
    mime_type: str = None,
    key_features: str = ""
) -> bool:
    """
    Generate image with retry mechanism and rate limit protection

    Args:
        client: Initialized Gemini API client
        filename: Output filename
        prompt: Generation prompt
        aspect_ratio: Image aspect ratio (e.g., "3:4", "16:9"). Uses default if None.
        mime_type: Output MIME type ("image/jpeg" or "image/png"). Uses default if None.
        key_features: Technical specifications for precise image generation (optional)

    Returns:
        bool: True if generation succeeded, False otherwise
    """
    save_path = os.path.join(GeneratorConfig.OUTPUT_DIR, filename)

    # Use defaults if not specified
    aspect_ratio = aspect_ratio or GeneratorConfig.ASPECT_RATIO
    mime_type = mime_type or GeneratorConfig.OUTPUT_MIME_TYPE

    # Check if file exists and skip if configured to do so
    if GeneratorConfig.SKIP_EXISTING_FILES and os.path.exists(save_path):
        logger.info(f"File already exists, skipping: {filename}")
        return True

    # Construct enhanced prompt with key features
    enhanced_prompt = prompt
    if key_features:
        enhanced_prompt = f"{prompt}. Technical specifications: {key_features}"
        logger.info(f"Generating image: {filename}")
        logger.info(f"  Aspect ratio: {aspect_ratio}, Format: {mime_type}")
        logger.info(f"  Key features: {key_features}")
        logger.debug(f"  Enhanced prompt: {enhanced_prompt[:100]}{'...' if len(enhanced_prompt) > 100 else ''}")
    else:
        logger.info(f"Generating image: {filename}")
        logger.info(f"  Aspect ratio: {aspect_ratio}, Format: {mime_type}")
        logger.debug(f"  Prompt: {prompt[:80]}{'...' if len(prompt) > 80 else ''}")

    for attempt in range(GeneratorConfig.MAX_RETRIES):
        try:
            # Call Imagen 3 model with enhanced prompt
            response = client.models.generate_images(
                model=GeneratorConfig.IMAGE_MODEL,
                prompt=enhanced_prompt,
                config=types.GenerateImagesConfig(
                    number_of_images=GeneratorConfig.NUMBER_OF_IMAGES,
                    aspect_ratio=aspect_ratio,
                    output_mime_type=mime_type
                )
            )

            if response.generated_images:
                # Process image data
                image_data = response.generated_images[0].image.image_bytes
                image = Image.open(io.BytesIO(image_data))

                # Optional: Resize specific images
                # Example for dice assets
                # if "dice" in filename.lower():
                #     image = image.resize((240, 240))

                # Save image
                image.save(save_path)
                logger.info(f"Successfully saved: {save_path}")

                # Mandatory cooldown to protect rate limit
                logger.info(f"Cooldown period: {GeneratorConfig.COOLDOWN_SECONDS} seconds")
                time.sleep(GeneratorConfig.COOLDOWN_SECONDS)
                return True
            else:
                logger.error(f"Generation failed (no image returned): {filename}")
                return False

        except exceptions.ResourceExhausted:
            wait_time = GeneratorConfig.RETRY_WAIT_BASE * (attempt + 1)
            logger.warning(
                f"Rate limit exceeded (429). "
                f"Waiting {wait_time} seconds before retry {attempt + 1}/{GeneratorConfig.MAX_RETRIES}"
            )
            time.sleep(wait_time)

        except Exception as error:
            logger.error(f"Unexpected error during generation: {error}")
            # Non-rate-limit errors usually don't benefit from retry
            break

    logger.error(f"Giving up on generation: {filename}")
    return False


# ==========================================
# Main Program
# ==========================================

def main():
    """Main execution function"""
    # Setup command-line argument parser
    parser = argparse.ArgumentParser(
        description='Batch generate test images using Gemini API',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Example usage:
  python generate_assets.py prompts.txt
  python generate_assets.py prompts.txt --verbose

Input file formats (one per line):

  Format 1 (Basic - uses default settings):
    "output_filename.jpg", "detailed prompt description"

  Format 2 (Custom aspect ratio):
    "output_filename.jpg", "detailed prompt description", "aspect_ratio"

  Format 3 (Full control):
    "output_filename.jpg", "detailed prompt description", "aspect_ratio", "mime_type"

  Format 4 (With technical features):
    "output_filename.jpg", "prompt", "aspect_ratio", "mime_type", "key_features"

Supported values:
  - Aspect ratios: 1:1, 3:4, 4:3, 9:16, 16:9
  - MIME types: image/jpeg, image/png
  - Key features: Technical specifications for precise generation (optional)

Examples:
  "portrait.jpg", "Professional portrait photo", "3:4", "image/jpeg"
  "landscape.png", "Beautiful landscape", "16:9", "image/png"
  "square.jpg", "Product photo", "1:1"

  With key features for testing:
  "overexposed.jpg", "Outdoor portrait", "3:4", "image/jpeg", "highlight ratio >15%; average luminance >70%; detail loss in highlights"
  "backlight.jpg", "Portrait against window", "3:4", "image/jpeg", "subject luminance <30%; background >70%; severe backlight"
  "high_contrast.jpg", "Portrait with directional light", "3:4", "image/jpeg", "highlights >10%; shadows >10%; dynamic range >180"

Notes:
  - Lines starting with # are treated as comments
  - API key should be set via GEMINI_API_KEY environment variable
  - Free tier has rate limits, expect ~30 seconds per image
  - Default aspect ratio: 3:4 (portrait)
  - Default MIME type: image/jpeg
  - Key features are appended to prompt for technical precision
        """
    )
    parser.add_argument(
        'input_file',
        help='Text file containing filenames and prompts (e.g., prompts.txt)'
    )
    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Enable verbose logging (DEBUG level)'
    )

    args = parser.parse_args()

    # Adjust logging level if verbose
    if args.verbose:
        logger.setLevel(logging.DEBUG)
        for handler in logger.handlers:
            handler.setLevel(logging.DEBUG)

    # Initialize client
    logger.info("=" * 60)
    logger.info("Gemini Image Generator - Starting")
    logger.info("=" * 60)

    client = setup_client()

    # Create output directory
    os.makedirs(GeneratorConfig.OUTPUT_DIR, exist_ok=True)
    logger.info(f"Output directory: {GeneratorConfig.OUTPUT_DIR}")

    # Read tasks
    task_list = parse_input_file(args.input_file)

    if not task_list:
        logger.warning("No tasks found in input file. Exiting.")
        sys.exit(0)

    logger.info(f"Total tasks to process: {len(task_list)}")
    logger.info("Mode: Free Tier (expect slower processing)")
    logger.info("-" * 60)

    # Execute tasks
    success_count = 0
    failure_count = 0
    successful_images = []

    for index, (filename, prompt, aspect_ratio, mime_type, key_features) in enumerate(task_list, 1):
        logger.info(f"Task {index}/{len(task_list)}: {filename}")

        if generate_image_safe(client, filename, prompt, aspect_ratio, mime_type, key_features):
            success_count += 1
            successful_images.append(filename)
        else:
            failure_count += 1

        logger.info("-" * 60)

    # Summary
    logger.info("=" * 60)
    logger.info("Generation Complete!")
    logger.info(f"Successful: {success_count}/{len(task_list)}")
    logger.info(f"Failed: {failure_count}/{len(task_list)}")
    logger.info("=" * 60)

    # Generate metadata file for Gradle integration
    generate_metadata(task_list, success_count, failure_count, successful_images)

    # Exit with appropriate code
    sys.exit(0 if failure_count == 0 else 1)


if __name__ == "__main__":
    main()
