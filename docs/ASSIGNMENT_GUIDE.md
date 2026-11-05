# Assignment No. 2 — how to explain this application

## Problem statement

Students often know how much time they spent studying but do not summarize how interruptions, break intervals and energy affect the structure of their session. FocusFlow AI combines a reflective input form, an educational ML model and a private history screen to demonstrate on-device inference.

## What's original compared with the instructor reference

The teacher's NumberGuessingAppTflite is a reference for how an Android app loads a `.tflite` model and runs it locally. FocusFlow AI uses an entirely different dataset, model, user experience and input features. Its screens include dashboard, form, result, timer and saved history.

## Verifiable code locations

- `app/src/main/java/com/moez/focusflow/FocusPredictor.java`: loads the FlatBuffer from `assets/`, checks tensors and calls `Interpreter.run`.
- `AnalyzerActivity.java`: input validation, sample loading, prediction flow and result passing through Intent.
- `SessionStore.java`: private SharedPreferences history.
- `HistoryActivity.java`: trend chart and saved entries.
- `TimerActivity.java`: running/paused countdown timer.
- `SplashActivity.java`: branding and navigation to home.
- `ml/train_and_export.py`: educational synthetic model training/export.

## In viva

**Q:** Does it work without the internet?  
**A:** Yes. The model and inference runtime are bundled with the APK; the first Gradle sync on the developer's machine still needs internet.

**Q:** Are the predictions hardcoded?  
**A:** No. `FocusPredictor.predict` calls TensorFlow Lite's Interpreter on four normalized numeric inputs. The friendly tips are separate fixed suggestions, not model output.

**Q:** Is the focus score scientifically validated?  
**A:** No. It's a classroom ML demonstration built using synthetic training samples. A higher value means closer resemblance to the model's synthetic balanced class.

**Q:** Why store history locally?  
**A:** It demonstrates persistence while avoiding unnecessary storage or transfer of personal study records.

## Deliverable evidence

Provide source ZIP or GitHub link, README, a report with real Android screenshots and a short explanation of preprocessing, output tensors and the synthetic training limitation. Do not invent emulator or build screenshots.
