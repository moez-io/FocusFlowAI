# Manual test checklist and screenshot plan

Run the app on a real Android emulator or physical phone after a successful Gradle build.

| Test | Action | Expected result | Screenshot |
|---|---|---|---|
| Launch | Tap icon | Branded splash → dashboard | Capture splash quickly |
| Navigation | Tap Home, Analyze, Timer, History | Each section opens without crashing | Dashboard |
| TFLite | Enter `75,1,12,4` and press Analyze | AI result displayed with score gauge and saved session | Filled form + result |
| Second model run | Enter `35,10,0,2` and press Analyze | Another model score and a second history entry | Result |
| Validation | Leave minutes empty, press Analyze | Required input field error, no analysis saved | Validation state |
| Range validation | Input energy `9` | Field asks for 1..5 | Validation state |
| Offline | Enable Airplane mode, analyze again | Result still calculated on-device | Result in airplane mode |
| History | Open History | Records and trend drawn using *real* saved results | History |
| Clear | Tap Clear History → Cancel, then Clear | Cancel preserves; confirm deletes | Empty history |
| Timer | Start 25 min, pause, resume, reset | Clock changes, pauses and resets | Running timer |
| Orientation | Rotate phone while on various screens | Layout remains readable, scrolled as needed | Optional |
| Lifecycle | Navigate away from timer and return | Running timer reflects elapsed wall time | Optional |
| 16 KB | Build APK and inspect native libs; test compatible emulator | No alignment warnings or load errors | Build/analyzer evidence |

**Model documentation:** synthetic study patterns, no inference over network, tips are deterministic, confidence is not a clinical measurement. Screenshots must be from actual device/emulator runs.
