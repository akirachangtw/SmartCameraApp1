# SmartCameraApp Test Image Generation Guide

## Overview

This guide covers the automated test image generation system for SmartCameraApp v2.6.0, specifically designed to test **HDR Warning** and **Lighting Analysis** features.

## Script Information

**Script**: `generate_assets.py` v2.0.0
**Purpose**: Batch generate test images using Google Gemini Imagen 3 API
**Author**: SmartCameraApp Team
**Date**: 2025-12-26

## Features

### ✅ Enhanced Functionality (v2.0.0)

1. **Multi-Format Support**
   - Format 1: Basic (filename, prompt)
   - Format 2: Custom aspect ratio
   - Format 3: Full control (aspect + MIME type)
   - Format 4: With technical features (NEW!)

2. **Key Features Parameter**
   - Technical specifications for precise image generation
   - Direct mapping to AnalyzeLightingUseCase detection criteria
   - Enhanced prompt construction

3. **CSV Parsing**
   - Properly handles quoted fields with commas
   - Supports complex prompts with multiple clauses

4. **Comprehensive Logging**
   - Timestamped logging with levels (INFO, WARNING, ERROR, DEBUG)
   - Progress tracking
   - Success/failure statistics

## Prerequisites

### 1. Python Environment

```bash
# Install required packages
pip install google-genai pillow
```

### 2. Gemini API Key

Obtain an API key from [Google AI Studio](https://aistudio.google.com/app/apikey)

**Set as environment variable (recommended)**:

```powershell
# Windows PowerShell
$env:GEMINI_API_KEY='your_actual_api_key_here'

# Linux/Mac
export GEMINI_API_KEY='your_actual_api_key_here'
```

**Or modify the script**:
```python
# In generate_assets.py line 33
API_KEY = "your_actual_api_key_here"
```

## Usage

### Basic Test Execution

```bash
# Test script validation (no API calls)
python tools/test_generate_assets.py

# Generate v2.6.0 HDR test images
python tools/generate_assets.py tools/prompts_v2.6.0_hdr_test.txt

# Verbose mode for debugging
python tools/generate_assets.py tools/prompts_v2.6.0_hdr_test.txt --verbose
```

### Input File Format

#### Format 4 - With Technical Features (Recommended for Testing)

```csv
"filename.jpg", "prompt", "aspect_ratio", "mime_type", "key_features"
```

**Example**:
```
"hdr_backlight_window.jpg", "Portrait photo, person against bright window, severe backlight, 3:4 portrait aspect ratio, photorealistic", "3:4", "image/jpeg", "subject luminance <25%; background luminance >75%; severe backlight creating silhouette effect"
```

#### Format 1-3 - Backward Compatible

```
"basic.jpg", "Simple prompt"
"custom_aspect.jpg", "Prompt with custom ratio", "16:9"
"full_control.jpg", "Full control prompt", "1:1", "image/png"
```

## SmartCameraApp v2.6.0 Test Scenarios

### Test File: `prompts_v2.6.0_hdr_test.txt`

This file contains 10 carefully crafted test images covering 4 extreme lighting scenarios:

### 1. Backlight (Highest Priority) - 3 images

**HDR Trigger**: Subject luminance <30% AND Background >70%

| Image | Scenario | Key Features |
|-------|----------|--------------|
| `hdr_backlight_window_severe.jpg` | Indoor window backlight | Subject <25%, Background >75%, Silhouette |
| `hdr_backlight_sunset_extreme.jpg` | Sunset backlight | Subject <20%, Background >80%, Dramatic |
| `hdr_partial_backlight_side.jpg` | Partial backlight | Subject 30-40%, Rim lighting |

**Expected Detection**: `LightingState.Backlight` → **HDR Recommended**

### 2. High Contrast - 2 images

**HDR Trigger**: Highlights >10% AND Shadows >10% OR Dynamic Range >180

| Image | Scenario | Key Features |
|-------|----------|--------------|
| `hdr_high_contrast_window_interior.jpg` | Window split lighting | Highlights >15%, Shadows >15%, Range >200 |
| `hdr_high_contrast_directional_spotlight.jpg` | Spotlight drama | Highlights >12%, Shadows >18%, Range >190 |

**Expected Detection**: `LightingState.HighContrast` → **HDR Recommended**

### 3. Overexposure - 2 images

**HDR Trigger**: Highlight ratio >15%

| Image | Scenario | Key Features |
|-------|----------|--------------|
| `hdr_overexposed_sunny_outdoor.jpg` | Bright outdoor | Highlight >20%, Luminance >75%, Blown sky |
| `hdr_overexposed_snow_glare.jpg` | Snow glare | Highlight >25%, Luminance >80%, Extreme bright |

**Expected Detection**: `LightingState.Overexposed` → **HDR May Help**

### 4. Normal Lighting - 2 images

**No HDR Trigger**: Balanced exposure

| Image | Scenario | Key Features |
|-------|----------|--------------|
| `hdr_normal_soft_natural.jpg` | Soft natural light | Highlights <10%, Shadows <10%, Balanced |
| `hdr_normal_outdoor_shade.jpg` | Open shade | Highlights <12%, Shadows <8%, Even lighting |

**Expected Detection**: `LightingState.Normal` → **No HDR Needed**

### 5. Edge Cases - 1 image

| Image | Scenario | Key Features |
|-------|----------|--------------|
| `hdr_mixed_indoor_outdoor_doorway.jpg` | Indoor/outdoor transition | Highlights >15%, Shadows >15%, Range >210 |

**Expected Detection**: `LightingState.HighContrast` → **HDR Recommended**

## Test Execution Results

### Validation Summary

```
✅ ALL VALIDATION CHECKS PASSED
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Total tasks: 10
  All aspect ratios valid: ✅
  All MIME types valid: ✅
  Tasks with key features: 10/10 ✅
  Luminance specifications: ✅

  HDR Test Coverage:
    Backlight:       3/2 ✅
    High Contrast:   2/2 ✅
    Overexposure:    2/2 ✅
    Normal:          2/2 ✅
```

### Estimated Generation Time

```
Total images: 10
API cooldown: 30 seconds/image
Estimated time: ~350 seconds (~5.8 minutes)
```

## Technical Specifications Mapping

### Key Features → Detection Logic

The `key_features` parameter directly maps to AnalyzeLightingUseCase.kt detection criteria:

```kotlin
// Backlight Detection
if (subjectLuminance < 0.3f && backgroundLuminance > 0.7f) {
    return LightingState.Backlight  // HDR Recommended
}
```
**Corresponds to**: `"subject luminance <30%; background luminance >70%"`

```kotlin
// High Contrast Detection
if ((highlightRatio > 0.1f && shadowRatio > 0.1f) || dynamicRange > 180) {
    return LightingState.HighContrast  // HDR Recommended
}
```
**Corresponds to**: `"highlights >10%; shadows >10%; dynamic range >180"`

```kotlin
// Overexposure Detection
if (highlightRatio > 0.15f) {
    return LightingState.Overexposed  // HDR May Help
}
```
**Corresponds to**: `"highlight ratio >15%; average luminance >70%"`

```kotlin
// Normal Lighting
if (highlightRatio < 0.15f && averageLuminance >= 0.2f) {
    return LightingState.Normal  // No HDR Needed
}
```
**Corresponds to**: `"highlights <15%; average luminance 45-55%; balanced histogram"`

## Output

### Generated Files Location

```
app/src/debug/assets/test_images/
├── hdr_backlight_window_severe.jpg
├── hdr_backlight_sunset_extreme.jpg
├── hdr_high_contrast_window_interior.jpg
├── hdr_high_contrast_directional_spotlight.jpg
├── hdr_overexposed_sunny_outdoor.jpg
├── hdr_overexposed_snow_glare.jpg
├── hdr_normal_soft_natural.jpg
├── hdr_normal_outdoor_shade.jpg
├── hdr_mixed_indoor_outdoor_doorway.jpg
└── hdr_partial_backlight_side.jpg
```

### Image Specifications

- **Format**: JPEG
- **Aspect Ratio**: 3:4 (portrait mode)
- **Size**: Variable (typically 200-800 KB per image)
- **Quality**: Photorealistic, optimized for ML Kit detection

## Rate Limits & Costs

### Gemini Imagen 3 Free Tier

- **Limit**: ~100 images/day
- **Rate**: ~2 images/minute
- **Cost**: Free

### Script Protection Mechanisms

- **Cooldown**: 30 seconds between requests
- **Retry Logic**: 3 attempts with exponential backoff
- **Rate Limit Handling**: Automatic 60s+ wait on 429 errors

## Troubleshooting

### Issue 1: API Key Not Found

```
❌ Error: API Key not configured
```

**Solution**: Set `GEMINI_API_KEY` environment variable or modify script

### Issue 2: Module Not Found

```
ModuleNotFoundError: No module named 'google.genai'
```

**Solution**:
```bash
pip install google-genai pillow
```

### Issue 3: Rate Limit Exceeded

```
⚠️ Rate limit exceeded (429)
```

**Solution**: Script automatically retries. For persistent issues:
- Reduce batch size
- Increase `COOLDOWN_SECONDS` in script
- Wait before retrying

### Issue 4: Parsing Errors

```
Warning: Skipping malformed line X
```

**Solution**:
- Ensure proper CSV format
- Use double quotes for fields with commas
- Check for missing closing quotes

## Next Steps

### 1. Run Test Validation

```bash
python tools/test_generate_assets.py
```

Expected output: `🎉 All tests passed!`

### 2. Generate Test Images

```bash
python tools/generate_assets.py tools/prompts_v2.6.0_hdr_test.txt
```

Wait ~6 minutes for completion

### 3. Verify Generated Images

Check `app/src/debug/assets/test_images/` directory

### 4. Update TestImageProvider

Edit `TestImageProvider.kt` to include new images:

```kotlin
private val testImages = listOf(
    // v2.6.0 HDR Test Images
    "hdr_backlight_window_severe.jpg",
    "hdr_backlight_sunset_extreme.jpg",
    // ... add remaining images
)
```

### 5. Run SmartCameraApp Tests

```bash
# Unit tests
./gradlew test

# Emulator tests
./gradlew assembleEmulatorDebug
# Install and test on emulator
```

## Advanced Usage

### Generate Custom Test Images

Create your own prompt file:

```
# my_custom_tests.txt
"my_test_1.jpg", "Custom scenario prompt", "3:4", "image/jpeg", "specific technical requirements"
```

Run generation:
```bash
python tools/generate_assets.py my_custom_tests.txt
```

### Modify Generation Parameters

Edit `generate_assets.py` GeneratorConfig class:

```python
class GeneratorConfig:
    COOLDOWN_SECONDS = 60  # Increase for stricter rate limiting
    MAX_RETRIES = 5        # More retry attempts
    ASPECT_RATIO = "9:16"  # Change default aspect ratio
    OUTPUT_MIME_TYPE = "image/png"  # Change default format
```

## References

### Related Files

- `generate_assets.py` - Main generation script
- `test_generate_assets.py` - Validation test suite
- `prompts_v2.6.0_hdr_test.txt` - v2.6.0 HDR test prompts
- `prompts_lighting_tests.txt` - Comprehensive lighting test prompts (30+ images)
- `prompts_example.txt` - Format examples and demonstrations

### Documentation

- [Google Gemini API Docs](https://ai.google.dev/gemini-api/docs)
- [Imagen 3 Guide](https://ai.google.dev/gemini-api/docs/imagen)
- SmartCameraApp `AnalyzeLightingUseCase.kt`
- SmartCameraApp `TestImageProvider.kt`

## Version History

### v2.0.0 (2025-12-26)
- ✅ Added key_features parameter support
- ✅ CSV parsing for complex prompts
- ✅ Enhanced logging system
- ✅ Technical specifications mapping
- ✅ v2.6.0 HDR test scenarios
- ✅ Comprehensive validation suite

### v1.0.0 (Initial)
- Basic image generation
- Simple prompt format
- Manual aspect ratio and MIME type configuration

---

**For questions or issues, refer to the SmartCameraApp development team.**
