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

| Resolution    | ResolutionStrategy | ResolutionFilter |
| ------------- | ------------------ | ---------------- |
| **4032×3024** | ❌ Silent Fail      | ❌ Silent Fail    |
| **4000×3000** | ❌ Silent Fail      | ❌ Silent Fail    |
| **4032×2016** | ❌ Silent Fail      | ✅ **Works**      |
| **3840×2160** | ❌ Silent Fail      | ✅ **Works**      |
| **3264×2448** | ❌ Silent Fail      | ❌ Silent Fail    |
| **3264×1836** | ❌ Silent Fail      | ✅ **Works**      |
| **3264×1632** | ❌ Silent Fail      | ✅ **Works**      |
| **3200×2400** | ❌ Silent Fail      | ❌ Silent Fail    |
| **3024×3024** | ❌ Silent Fail      | ❌ Silent Fail    |
| **2976×2976** | ❌ Silent Fail      | ❌ Silent Fail    |
| **2688×1512** | ❌ Silent Fail      | ✅ **Works**      |
| **2592×1944** | ✅ **Works**        | ✅ **Works**      |
| **2448×2448** | ❌ Silent Fail      | ❌ Silent Fail    |
| **2304×1728** | ✅ **Works**        | ✅ **Works**      |
| **2048×1536** | ✅ **Works**        | ✅ **Works**      |
| **1944×1944** | ⚠️ Fallback        | ✅ **Works**      |
| **1920×1080** | ⚠️ Fallback        | ✅ **Works**      |
| **1600×1200** | ✅ **Works**        | ✅ **Works**      |
| **1440×1080** | ✅ **Works**        | ✅ **Works**      |
| **1280×960**  | ✅ **Works**        | ✅ **Works**      |
| **1280×720**  | ⚠️ Fallback        | ✅ **Works**      |
| **864×486**   | ⚠️ Fallback        | ✅ **Works**      |
| **864×480**   | ⚠️ Fallback        | ✅ **Works**      |
| **720×480**   | ⚠️ Fallback        | ✅ **Works**      |
| **640×480**   | ✅ **Works**        | ✅ **Works**      |
| **352×288**   | ⚠️ Fallback        | ✅ **Works**      |
| **320×240**   | ✅ **Works**        | ✅ **Works**      |
| **176×144**   | ⚠️ Fallback        | ✅ **Works**      |
| **144×176**   | ⚠️ Fallback        | ✅ **Works**      |
