# FocusFlow AI v2.0 — Study Intelligence

**A complete, redesigned Android Assignment No. 2 project**  
Java • XML • Offline TensorFlow Lite inference • Private local history • Focus timer

> This is the upgraded version of FocusFlow AI, not Spam Shield. It is intended to demonstrate real on-device ML in a university Android application. The ML model is trained on *synthetic* study-session records; it does **not** measure concentration or health scientifically.

## What the app does

FocusFlow AI helps students reflect on a study session by entering four metrics:

1. **Duration:** 10–120 minutes
2. **Interruptions:** 0–15
3. **Breaks:** 0–25 minutes
4. **Energy:** 1–5

A real `.tflite` model (`app/src/main/assets/focusflow.tflite`) processes these four normalized numbers with `org.tensorflow.lite.Interpreter.run(...)`. The AI produces two probabilities; the app displays the *balanced-pattern score* as a visual gauge and provides clearly separated, rule-based study tips. Results are saved privately on the device, with a history chart showing the user's own analyses.

## Screens and features

| Screen | Purpose |
|---|---|
| **SplashActivity** | Branded splash screen with custom visual mark, tagline and entrance animation |
| **MainActivity** | Modern dashboard with greeting, cumulative summaries, actual trend chart and shortcuts |
| **AnalyzerActivity** | Four validated input cards, useful sample presets, model-ready status and genuine TFLite inference |
| **ResultActivity** | Animated score ring, category interpretation, metric summary, separate recommendations and share action |
| **TimerActivity** | Start/pause/reset 25- or 45-minute focus sessions and a 5-minute break; retains running state across screens |
| **HistoryActivity** | Local records, a seven-session trend chart, tap-to-review, confirm-to-clear and empty state |

**Design:** navy/violet/teal theme, original vector branding, rounded XML cards, fixed four-tab navigation, clear typographic hierarchy, scrolling layouts, and no required third-party UI library.

**Privacy:** No app account, ad SDK, analytics tracker, internet permission or remote database. Session history uses private `SharedPreferences` and JSON; the last 60 analyses are retained until cleared. The ML model and runtime are packaged inside the application.

## Open in Android Studio — IMPORTANT

1. Extract `FocusFlowAI_Pro_AndroidStudio_Project.zip` with **Extract All**.
2. Open **Android Studio → File → Open**.
3. Select the single extracted folder **`FocusFlowAI_Pro_Android_Project`**. **It directly contains `settings.gradle`, `build.gradle`, `gradlew.bat` and `app/`.** Do not open the `app` subfolder or the folder above it.
4. Select **Trust Project** if prompted.
5. Ensure **JDK 17** and **Android SDK Platform 35** are available (Android Studio Settings → Build Tools → Gradle, and Tools → SDK Manager).
6. Allow the initial Gradle dependency sync to download Android Gradle Plugin **8.7.3**, Gradle **8.9**, and LiteRT **1.4.2** (first sync needs internet; model inference does not).
7. Wait for **Gradle sync**; choose the **app** run configuration and a phone/emulator, then click ▶ **Run**.
8. You should see **Splash → Dashboard**. Tap **Start an AI analysis**, choose a preset, then **Analyze with AI**.

**If the Run button says “Add Configuration”**: check that you opened the folder with `settings.gradle`, then choose **File → Sync Project with Gradle Files**. You may need to create an **Android App** run configuration targeting module `app` after a successful Gradle sync.

### Gradle-wrapper disclosure

The bundled `gradle/wrapper/gradle-wrapper.jar` is the same small **transparent educational bootstrap** included with the earlier FocusFlow project, **not Gradle's standard official wrapper JAR**. Its inspectable Java source is `gradle/wrapper/GradleWrapperMain.java`; it downloads and launches the official Gradle 8.9 distribution from Gradle's distribution server. For normal long-term use, replace it with the official wrapper: install Gradle and run `gradle wrapper --gradle-version 8.9`. The bootstrap and official Gradle dependencies require internet initially. Do not mistake this bundled bootstrap for a Gradle-signed official JAR.

## Project map

```text
FocusFlowAI_Pro_Android_Project/    ← OPEN THIS FOLDER IN ANDROID STUDIO
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/focusflow.tflite
│       ├── java/com/moez/focusflow/
│       │   ├── SplashActivity.java
│       │   ├── MainActivity.java
│       │   ├── AnalyzerActivity.java
│       │   ├── ResultActivity.java
│       │   ├── TimerActivity.java
│       │   ├── HistoryActivity.java
│       │   ├── FocusPredictor.java
│       │   ├── SessionStore.java
│       │   ├── TrendChartView.java
│       │   ├── ScoreRingView.java
│       │   └── AppUi.java
│       └── res/{layout,drawable,values,mipmap-anydpi-v26}/
├── ml/                         # synthetic-data training/export and verification tools
├── docs/                       # assignment walkthrough, test plan, screenshot instructions
├── MODEL_CARD.md                # model limitations and data disclosure
├── BUILD_STATUS.md              # exactly what was/was not verified
├── gradle/wrapper/*
├── gradlew.bat
└── settings.gradle
```

## Example demonstration cases

| Duration | Interruptions | Breaks | Energy | Intended pattern |
|---:|---:|---:|---:|---|
| 75 | 1 | 12 | 4 | Generally resembles balanced synthetic examples |
| 35 | 10 | 0 | 2 | Generally resembles the less-balanced examples |

The actual result comes from the `.tflite` model, **not fixed prediction rules**. The recommendations are based on transparent, non-ML conditions to help a student reflect on their inputs; they are not represented as neural-network explanations.

## Model pipeline

```text
XML EditTexts → input validation → normalization (duration/120,
interruptions/15, breaks/25, energy/5) → [1,4] float32 input →
Interpreter.run() → [1,2] float32 softmax output → score ring →
local JSON history + basic tips
```

The model is educational and trained with generated examples; see [MODEL_CARD.md](MODEL_CARD.md) and `ml/train_and_export.py` for exact logic. Model assets are **not** downloaded at runtime.

## Android 16 KB page-size compatibility

The project uses Android Gradle Plugin 8.7.3 and LiteRT 1.4.2. The APK will include native LiteRT code, so **16 KB compatibility must be inspected on the actual APK** and tested with a 16 KB emulator where available. Do not claim verification based on source metadata. Inspect **Build → Analyze APK → lib/** and check ELF LOAD and packaging alignment with the latest Android tooling.

## Submission evidence

Capture **real screenshots from your emulator or phone**. Suggested screenshots: splash, dashboard, filled analyzer, TFLite result, history with two actual results, running focus timer. See [docs/TEST_PLAN.md](docs/TEST_PLAN.md). No generated screenshots are provided as execution evidence.

## Credit and references

- Instructor-supplied reference: [NumberGuessingAppTflite](https://github.com/Uzair390-Del/NumberGuessingAppTflite). This is a separately built app and model, not a copy of the teacher's code.
- [Google AI Edge LiteRT](https://ai.google.dev/edge/litert)
- [Android 16 KB page-size support](https://developer.android.com/guide/practices/page-sizes)

**Assignment context:** KICSIT / IST · Android Assignment No. 2 · Java + XML + TFLite demonstration.
