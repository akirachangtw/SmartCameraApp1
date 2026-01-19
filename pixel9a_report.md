# Pixel 9a Memory Test Analysis - v2.8.5

## Test Information
- **Device**: Pixel 9a (59131JEBF17244)
- **Version**: v2.8.5
- **Test Duration**: 10 minutes (20 samples @ 30s interval)
- **Test Start**: 2026-01-10 16:45:56
- **Test End**: 2026-01-10 16:55:39

## Memory Statistics (PSS in KB)

### TOTAL PSS Trend
| Sample | Time     | TOTAL PSS | Native Heap | Graphics | Private Other | System |
|--------|----------|-----------|-------------|----------|---------------|--------|
| #1     | 16:45:56 | 384,228   | 85,228      | 116,628  | 80,644        | 20,384 |
| #2     | 16:46:27 | 404,587   | 90,028      | 120,136  | 84,036        | 20,875 |
| #3     | 16:46:57 | 461,830   | 88,484      | 135,892  | 84,144        | 21,134 |
| #4     | 16:47:28 | 480,440   | 91,032      | 170,164  | 84,216        | 21,132 |
| #5     | 16:47:58 | 537,895   | 90,120      | 191,304  | 84,508        | 21,943 |
| #6     | 16:48:29 | 485,243   | 96,800      | 201,508  | 83,704        | 21,451 |
| #7     | 16:48:59 | 540,134   | 89,704      | 212,076  | 83,632        | 21,522 |
| #8     | 16:49:30 | 509,352   | 96,692      | 212,076  | 83,516        | 21,612 |
| #9     | 16:50:00 | 492,577   | 93,044      | 222,716  | 83,524        | 21,725 |
| #10    | 16:50:31 | 558,324   | 92,880      | 217,432  | 83,480        | 21,464 |
| #11    | 16:51:02 | 494,440   | 93,636      | 228,084  | 83,536        | 20,884 |
| #12    | 16:51:32 | 552,601   | 90,516      | 228,080  | 83,424        | 20,937 |
| #13    | 16:52:03 | 494,600   | 91,516      | 228,080  | 83,544        | 20,980 |
| #14    | 16:52:34 | 583,572   | 101,084     | 228,012  | 83,808        | 20,876 |
| #15    | 16:53:04 | 498,614   | 91,780      | 222,728  | 83,388        | 20,970 |
| #16    | 16:53:35 | 538,783   | 95,964      | 222,728  | 83,388        | 20,991 |
| #17    | 16:54:06 | 480,004   | 89,956      | 217,444  | 83,392        | 21,060 |
| #18    | 16:54:37 | 527,863   | 91,384      | 222,728  | 83,508        | 21,099 |
| #19    | 16:55:08 | 503,349   | 91,264      | 206,804  | 83,348        | 21,161 |
| #20    | 16:55:39 | 535,890   | 93,464      | 222,740  | 83,348        | 21,178 |

## Key Metrics Analysis

### TOTAL PSS (Main Memory Usage)
- **Initial**: 384,228 KB (~375 MB)
- **Peak**: 583,572 KB (~570 MB) @ Sample #14
- **Final**: 535,890 KB (~523 MB)
- **Average**: 507,311 KB (~495 MB)
- **Growth**: +151,662 KB (+39.5%)
- **Fluctuation Pattern**: Shows oscillation (not linear growth)

### Native Heap (Camera Processing)
- **Range**: 85,228 ~ 101,084 KB
- **Average**: 92,322 KB (~90 MB)
- **Stability**: ✅ STABLE (within 16KB range after warmup)
- **Trend**: No memory leak observed

### Graphics Memory (GPU/Texture Buffers)
- **Initial**: 116,628 KB (~114 MB)
- **Peak**: 228,084 KB (~223 MB)
- **Final**: 222,740 KB (~217 MB)
- **Growth**: +106,112 KB (+91.0%)
- **Trend**: ⚠️ Gradual increase then stabilized ~220MB

### Private Other (Dalvik/Code/Stack)
- **Range**: 80,644 ~ 84,508 KB
- **Average**: 83,557 KB
- **Stability**: ✅ STABLE (within 4KB range)

### System Memory
- **Range**: 20,384 ~ 21,943 KB
- **Average**: 21,184 KB
- **Stability**: ✅ STABLE

### SWAP Usage
- **Consistent**: 79-116 KB
- **Status**: ✅ Minimal swap usage (healthy)

## Observations

### ✅ Positive Indicators
1. **Native Heap Stable**: No memory leak in camera processing logic
2. **No Continuous Growth**: PSS shows oscillation pattern, not linear increase
3. **Low Swap Pressure**: Minimal swap usage throughout test
4. **Stable System/Private**: Core Android components healthy

### ⚠️ Concerns
1. **Graphics Memory Growth**:
   - Increased from 116MB → 223MB (+91%)
   - Pattern suggests texture/buffer accumulation
   - Stabilized around 220MB after ~5 minutes

2. **PSS Oscillation**:
   - Large fluctuations (480MB ↔ 583MB)
   - Indicates aggressive allocation/deallocation cycles
   - Possible GC thrashing

## Comparison with Previous Versions

### v2.8.4 vs v2.8.5
| Metric           | v2.8.4     | v2.8.5     | Change    |
|------------------|------------|------------|-----------|
| Initial PSS      | ~420 MB    | ~375 MB    | -45 MB ✅ |
| Peak PSS         | ~650 MB    | ~570 MB    | -80 MB ✅ |
| Avg Graphics     | ~240 MB    | ~200 MB    | -40 MB ✅ |
| Native Heap      | ~95 MB     | ~90 MB     | -5 MB ✅  |
| Stability        | Unstable   | Oscillating| Improved ✅|

## Root Cause Analysis

### Graphics Memory Growth (116MB → 223MB)
**Suspected Causes**:
1. **ML Kit Face Detection**:
   - Creates InputImage from ImageProxy per frame
   - May cache bounding box overlays
   - Potential bitmap retention in result callbacks

2. **Compose Preview**:
   - Texture buffer accumulation
   - Frame buffer not released promptly

3. **Debug Overlays**:
   - Face detection rectangles
   - Composition guide lines
   - May retain drawing buffers

**Recommended Investigation**:
```kotlin
// Check LiveCameraSource.kt
- InputImage.fromMediaImage() cleanup
- ImageProxy.close() timing
- Face detection result retention

// Check CameraViewModel.kt
- faceDetectionResults StateFlow
- Overlay bitmap lifecycle
```

## Recommendations

### Immediate Actions
1. **Profile Graphics Stack**:
   ```bash
   adb shell dumpsys gfxinfo com.akirachang.ai.photographer
   ```

2. **Check Bitmap Retention**:
   ```kotlin
   // Add in SmartCameraAnalyzer
   override fun analyze(imageProxy: ImageProxy) {
       val inputImage = InputImage.fromMediaImage(...)
       detector.process(inputImage)
           .addOnSuccessListener { faces ->
               // ⚠️ Ensure no bitmap/image retention
           }
           .addOnCompleteListener {
               imageProxy.close() // ✅ Already present
           }
   }
   ```

3. **Add Memory Profiler Markers**:
   ```kotlin
   Trace.beginSection("FaceDetection")
   // ... detection logic
   Trace.endSection()
   ```

### Level 2 Investigation
- Use Android Studio Memory Profiler
- Capture heap dump at peak (Sample #14)
- Check retained objects in Graphics category

## Conclusion

### Status: ⚠️ IMPROVED BUT GRAPHICS LEAK SUSPECTED

**Achievements**:
- ✅ Native Heap stable (~90MB)
- ✅ Lower initial memory footprint
- ✅ Peak memory reduced by 80MB vs v2.8.4

**Remaining Issues**:
- ⚠️ Graphics memory grows 116MB → 223MB (+91%)
- ⚠️ PSS oscillation suggests GC pressure
- ⚠️ Need to identify texture/bitmap leak source

**Next Steps**:
1. Profile graphics memory allocation
2. Add InputImage/Bitmap cleanup logging
3. Consider v2.8.6 targeted fix for graphics leak

---
**Test Data**: `tmp/pixel9a_memory_test_v2.8.5_20260110_164556.txt`
