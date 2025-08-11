# camerax-high-res-analysis-issue
Simple test app showing CameraX ImageAnalysis getting stuck at highest resolution.

## The Problem

When you use ImageAnalysis it ignores high resolution requests and nothing happens. This happens even though the camera supports higher resolutions.

## Test Setup

- CameraX 1.4.2
- Tested on Pixel 7 Pro (Android 16) and Nokia 7 Plus (Android 10)
- Macbook Pro M2 Max

## How to Run

1. Clone and run the app
2. Look at the resolution display
3. Tap button to toggle between max resolution and 1920x1080
4. See that high res doesnt run

## What Should Happen

ImageAnalysis should use the resolution you request.

## What Actually Happens

ImageAnalysis seems to do nothing.

## Notes

- If you only bind ImageAnalysis (no Preview), it still doesnt work with high resolution works fine
- The camera definitely supports these resolutions - checked with Camera2 API
- No error messages or warnings

## Related Issue

[Google Issue Tracker #365458483](https://issuetracker.google.com/issues/365458483)