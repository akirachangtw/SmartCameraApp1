# Manual Test Image Generation Guide
## SmartCameraApp v2.6.0 - HDR & Lighting Analysis Test Images

**Date**: 2025-12-26
**Status**: ⚠️ Gemini Imagen 3 API Currently Unavailable

---

## Issue Encountered

The `generate_assets.py` script encountered an API issue:

```
404 NOT_FOUND: models/imagen-3.0-generate-001 is not found for API version v1beta
```

**Root Cause**: The Imagen 3 model is not currently available through the standard Gemini API endpoint with the current API key configuration.

---

## Alternative Solutions

### Option 1: Manual Image Generation (Recommended for Now)

Use alternative image generation tools to create the 10 required HDR test images based on the specifications in `prompts_v2.6.0_hdr_test.txt`.

#### Recommended Tools:

1. **Midjourney** (https://www.midjourney.com/)
   - High quality, photorealistic results
   - Good at following technical specifications
   - Paid service (~$10/month basic plan)

2. **DALL-E 3** (https://openai.com/dall-e-3)
   - Available through ChatGPT Plus
   - Good prompt following
   - $20/month ChatGPT Plus subscription

3. **Stable Diffusion** (https://stability.ai/)
   - Open source, can run locally
   - Free (if self-hosted)
   - Requires technical setup

4. **Adobe Firefly** (https://firefly.adobe.com/)
   - Commercial-safe images
   - Available with Creative Cloud
   - Good for stock photography style

#### Manual Generation Workflow:

```bash
# 1. Open prompts file
notepad tools\prompts_v2.6.0_hdr_test.txt

# 2. For each image specification:
#    - Copy the prompt text
#    - Paste into your chosen AI image generator
#    - Download the generated image
#    - Rename to match the filename in the prompt file
#    - Save to: app\src\debug\assets\test_images\

# 3. Verify all 10 images are generated:
dir app\src\debug\assets\test_images\hdr_*.jpg
```

---

### Option 2: Use Google AI Studio (Web Interface)

While the API is unavailable, you can use Google AI Studio web interface:

1. Visit https://aistudio.google.com/
2. Navigate to "Imagen" (if available)
3. Enter prompts from `prompts_v2.6.0_hdr_test.txt`
4. Download generated images
5. Save to `app/src/debug/assets/test_images/`

---

### Option 3: Use Stock Photography

#### Free Stock Photo Sources:

1. **Unsplash** (https://unsplash.com/)
   - Search terms: "backlight portrait", "high contrast photo", "overexposed photo"
   - High quality, free to use

2. **Pexels** (https://www.pexels.com/)
   - Similar to Unsplash
   - Good variety of lighting conditions

3. **Pixabay** (https://pixabay.com/)
   - Completely free
   - Good for basic test scenarios

#### Stock Photo Workflow:

```bash
# Search for specific scenarios:
# - "backlight portrait window"
# - "sunset silhouette"
# - "high contrast portrait"
# - "overexposed outdoor"
# - "snow reflection bright"
# - "soft natural light portrait"
# - "outdoor shade portrait"

# Download and rename to match spec:
# hdr_backlight_window_severe.jpg
# hdr_backlight_sunset_extreme.jpg
# (etc.)
```

---

### Option 4: Capture Real Photos

For the most accurate test data, capture real photos with a smartphone:

#### Equipment Needed:
- Android phone with camera
- Various lighting environments

#### Capture Checklist:

**Backlight Scenarios (3 photos)**:
```
☐ Indoor window backlight (severe)
  - Stand in front of bright window
  - Ensure face is in shadow
  - Window extremely bright

☐ Outdoor sunset backlight (extreme)
  - Shoot against sunset sky
  - Person in complete silhouette
  - Dramatic orange/red sky

☐ Partial backlight (moderate)
  - Side/back window light
  - Some facial detail visible
  - Rim lighting effect
```

**High Contrast Scenarios (2 photos)**:
```
☐ Window split lighting
  - Person near large window
  - Half face bright, half dark
  - Extreme contrast

☐ Directional spotlight
  - Strong directional light source
  - Harsh highlights and shadows
  - Theatrical effect
```

**Overexposure Scenarios (2 photos)**:
```
☐ Bright sunny outdoor
  - Midday sunlight
  - Washed out sky
  - Harsh sunlight

☐ Snow glare
  - Bright snowy environment
  - Snow reflecting sunlight
  - Very bright overall
```

**Normal Lighting Scenarios (2 photos)**:
```
☐ Soft natural light
  - Diffused window light
  - Even skin tones
  - Balanced exposure

☐ Outdoor shade
  - Open shade outdoors
  - Soft even lighting
  - No direct sunlight
```

**Edge Case (1 photo)**:
```
☐ Indoor/outdoor doorway
  - Person in doorway
  - Dark interior + bright exterior
  - Mixed lighting zones
```

#### Photo Specifications:
- **Aspect Ratio**: 3:4 (portrait orientation)
- **Format**: JPEG
- **Size**: At least 1080x1440 pixels
- **Quality**: High (minimal compression)

---

## Required Test Images Summary

| # | Filename | Scenario | Technical Specs |
|---|----------|----------|-----------------|
| 1 | `hdr_backlight_window_severe.jpg` | Indoor window backlight | Subject <25%, BG >75% |
| 2 | `hdr_backlight_sunset_extreme.jpg` | Sunset silhouette | Subject <20%, BG >80% |
| 3 | `hdr_high_contrast_window_interior.jpg` | Window split lighting | Highlights >15%, Shadows >15% |
| 4 | `hdr_high_contrast_directional_spotlight.jpg` | Spotlight drama | Highlights >12%, Shadows >18% |
| 5 | `hdr_overexposed_sunny_outdoor.jpg` | Bright outdoor | Highlight ratio >20% |
| 6 | `hdr_overexposed_snow_glare.jpg` | Snow glare | Highlight ratio >25% |
| 7 | `hdr_normal_soft_natural.jpg` | Soft natural light | Balanced, Highlights <10% |
| 8 | `hdr_normal_outdoor_shade.jpg` | Outdoor shade | Even lighting, Shadows <8% |
| 9 | `hdr_mixed_indoor_outdoor_doorway.jpg` | Indoor/outdoor mix | Dynamic range >210 |
| 10 | `hdr_partial_backlight_side.jpg` | Partial backlight | Subject 30-40%, BG 60-70% |

---

## Verification Checklist

After obtaining all 10 images, verify they meet requirements:

```bash
# 1. Check all files exist
ls app/src/debug/assets/test_images/hdr_*.jpg

# Expected output: 10 files
# hdr_backlight_sunset_extreme.jpg
# hdr_backlight_window_severe.jpg
# hdr_high_contrast_directional_spotlight.jpg
# hdr_high_contrast_window_interior.jpg
# hdr_mixed_indoor_outdoor_doorway.jpg
# hdr_normal_outdoor_shade.jpg
# hdr_normal_soft_natural.jpg
# hdr_overexposed_snow_glare.jpg
# hdr_overexposed_sunny_outdoor.jpg
# hdr_partial_backlight_side.jpg

# 2. Check file sizes (should be 50KB - 5MB each)
# PowerShell:
Get-ChildItem app\src\debug\assets\test_images\hdr_*.jpg | Select-Object Name, @{Name="SizeKB";Expression={[math]::Round($_.Length/1KB,2)}}

# 3. Verify aspect ratio (should be 3:4 portrait)
# Use image viewer or:
# PowerShell with ImageMagick:
# identify -format "%f: %wx%h\n" app/src/debug/assets/test_images/hdr_*.jpg
```

---

## Next Steps

### After Manual Image Generation:

1. ✅ Save all 10 images to `app/src/debug/assets/test_images/`

2. ✅ Update `TestImageProvider.kt`:
   ```kotlin
   private val testImages = listOf(
       // v2.6.0 HDR Test Images
       "hdr_backlight_window_severe.jpg",
       "hdr_backlight_sunset_extreme.jpg",
       "hdr_high_contrast_window_interior.jpg",
       "hdr_high_contrast_directional_spotlight.jpg",
       "hdr_overexposed_sunny_outdoor.jpg",
       "hdr_overexposed_snow_glare.jpg",
       "hdr_normal_soft_natural.jpg",
       "hdr_normal_outdoor_shade.jpg",
       "hdr_mixed_indoor_outdoor_doorway.jpg",
       "hdr_partial_backlight_side.jpg"
   )
   ```

3. ✅ Test on emulator:
   ```bash
   ./gradlew assembleEmulatorDebug
   # Install and run on emulator
   ```

4. ✅ Verify lighting detection:
   - Check logs for correct lighting state detection
   - Confirm HDR recommendations appear for backlight/high contrast scenarios
   - Verify normal lighting shows no HDR recommendation

---

## Future: Fix Imagen API Access

### When Imagen 3 API becomes available:

1. **Update API Configuration**: Check Google AI Studio for new model names
2. **Modify Script**: Update `IMAGE_MODEL` in `generate_assets.py`
3. **Regenerate Images**: Run automated generation for consistency
4. **Compare Results**: Validate AI-generated vs manual images

### Potential Model Names to Try:

```python
# Update in generate_assets.py:
IMAGE_MODEL = 'models/imagen-3.0-generate-001'  # Try with full path
# or
IMAGE_MODEL = 'gemini-2.0-flash-exp'  # Use Gemini 2.0's image generation
# or
IMAGE_MODEL = 'imagegeneration@latest'  # Use latest available
```

---

## Contact & Support

For questions about:
- **Imagen API Access**: Contact Google AI support or check Google AI Studio
- **Alternative Tools**: Consult tool-specific documentation
- **Test Image Requirements**: Refer to `prompts_v2.6.0_hdr_test.txt`
- **SmartCameraApp Integration**: See `TestImageProvider.kt`

---

**Status**: 📋 Manual image generation workflow documented
**Priority**: Medium (can use stock photos or real captures as temporary solution)
**Timeline**: Images needed for v2.6.0 testing
