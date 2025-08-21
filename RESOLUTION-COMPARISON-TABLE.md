# Resolution Comparison Tables

## Overview
This document provides comprehensive comparison tables for CameraX ImageAnalysis resolution behavior on Nokia 7 Plus, comparing ResolutionStrategy vs ResolutionFilter approaches.

## Test Configuration
### Device Specifications
```yaml
SoC: Qualcomm Snapdragon 660 (SDM660)
GPU: Adreno 512
RAM: 4GB/6GB LPDDR4X
Camera:
  - Primary: 12MP f/1.75 (1.4μm pixels)
  - Secondary: 13MP f/2.6 (1.0μm pixels)
  - Hardware Level: LEVEL_3 (Full feature set)
Android Version: 10 (API level 29)
Camera2 API: Full support
```

### Available Camera Configurations
```kotlin
// Standard YUV_420_888 output sizes (from device logs)
val standardSizes = listOf(
    Size(4032, 3024), Size(4000, 3000), Size(3024, 3024),
    Size(2976, 2976), Size(3840, 2160), Size(4032, 2016),
    Size(3264, 2448), Size(3200, 2400), Size(3264, 1836),
    Size(2448, 2448), Size(3264, 1632), Size(2592, 1944),
    Size(2688, 1512), Size(2304, 1728), Size(1944, 1944),
    // ... additional sizes
)

// High resolution YUV_420_888 sizes
val highResSizes = emptyList<Size>() // No high-res sizes available
```

## Actual demo on Nokia 7 plus
[recordings/nokia-7-plus/resolution-filter-demo.mp4](https://github.com/mubaarakhassan/camerax-high-res-analysis-issue/blob/main/recordings/nokia-7-plus/resolution-filter-demo.mp4)

[recordings/nokia-7-plus/resolution-strategy-demo.mp4](https://github.com/mubaarakhassan/camerax-high-res-analysis-issue/blob/main/recordings/nokia-7-plus/resolution-strategy-demo.mp4)

Here are the logs:
[logs/nokia-7-plus/resolution-filter-logs.txt](https://github.com/mubaarakhassan/camerax-high-res-analysis-issue/blob/main/logs/nokia-7-plus/resolution-filter-logs.txt)

[logs/nokia-7-plus/resolution-strategy-logs.txt](https://github.com/mubaarakhassan/camerax-high-res-analysis-issue/blob/main/logs/nokia-7-plus/resolution-strategy-logs.txt)

## Complete Resolution Matrix

| Resolution | Width | Height | MP | Aspect | ResolutionStrategy | ResolutionFilter |
|------------|-------|--------|----|---------|--------------------|------------------|
| **4032×3024** | 4032 | 3024 | 12.2 | 4:3 | ❌ Silent Fail | ❌ Silent Fail |
| **4000×3000** | 4000 | 3000 | 12.0 | 4:3 | ❌ Silent Fail | ❌ Silent Fail |
| **4032×2016** | 4032 | 2016 | 8.1 | 2:1 | ❌ Silent Fail | ✅ **Works** |
| **3840×2160** | 3840 | 2160 | 8.3 | 16:9 | ❌ Silent Fail | ✅ **Works** |
| **3264×2448** | 3264 | 2448 | 8.0 | 4:3 | ❌ Silent Fail | ❌ Silent Fail |
| **3264×1836** | 3264 | 1836 | 6.0 | 16:9 | ❌ Silent Fail | ✅ **Works** |
| **3264×1632** | 3264 | 1632 | 5.3 | 2:1 | ❌ Silent Fail | ✅ **Works** |
| **3200×2400** | 3200 | 2400 | 7.7 | 4:3 | ❌ Silent Fail | ❌ Silent Fail |
| **3024×3024** | 3024 | 3024 | 9.1 | 1:1 | ❌ Silent Fail | ❌ Silent Fail |
| **2976×2976** | 2976 | 2976 | 8.9 | 1:1 | ❌ Silent Fail | ❌ Silent Fail |
| **2688×1512** | 2688 | 1512 | 4.1 | 16:9 | ❌ Silent Fail | ✅ **Works** |
| **2592×1944** | 2592 | 1944 | 5.0 | 4:3 | ✅ **Works** | ✅ **Works** |
| **2448×2448** | 2448 | 2448 | 6.0 | 1:1 | ❌ Silent Fail | ❌ Silent Fail |
| **2304×1728** | 2304 | 1728 | 4.0 | 4:3 | ✅ **Works** | ✅ **Works** |
| **2048×1536** | 2048 | 1536 | 3.1 | 4:3 | ✅ **Works** | ✅ **Works** |
| **1944×1944** | 1944 | 1944 | 3.8 | 1:1 | ⚠️ Fallback | ✅ **Works** |
| **1920×1080** | 1920 | 1080 | 2.1 | 16:9 | ⚠️ Fallback | ✅ **Works** |
| **1600×1200** | 1600 | 1200 | 1.9 | 4:3 | ✅ **Works** | ✅ **Works** |
| **1440×1080** | 1440 | 1080 | 1.6 | 4:3 | ✅ **Works** | ✅ **Works** |
| **1280×960** | 1280 | 960 | 1.2 | 4:3 | ✅ **Works** | ✅ **Works** |
| **1280×720** | 1280 | 720 | 0.9 | 16:9 | ⚠️ Fallback | ✅ **Works** |
| **864×486** | 864 | 486 | 0.4 | 16:9 | ⚠️ Fallback | ✅ **Works** |
| **864×480** | 864 | 480 | 0.4 | 16:9 | ⚠️ Fallback | ✅ **Works** |
| **720×480** | 720 | 480 | 0.3 | 3:2 | ⚠️ Fallback | ✅ **Works** |
| **640×480** | 640 | 480 | 0.3 | 4:3 | ✅ **Works** | ✅ **Works** |
| **352×288** | 352 | 288 | 0.1 | 11:9 | ⚠️ Fallback | ✅ **Works** |
| **320×240** | 320 | 240 | 0.1 | 4:3 | ✅ **Works** | ✅ **Works** |
| **176×144** | 176 | 144 | 0.0 | 11:9 | ⚠️ Fallback | ✅ **Works** |
| **144×176** | 144 | 176 | 0.0 | 9:11 | ⚠️ Fallback | ✅ **Works** |
