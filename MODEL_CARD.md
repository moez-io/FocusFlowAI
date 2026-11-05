# Model Card — FocusFlow AI educational classifier

## Objective

Classify whether four numeric study-session properties resemble a **synthetically generated balanced pattern** or a **synthetically generated needs-adjustment pattern**. The model's probability is used for a visual match score, not for clinically measuring concentration or productivity.

## Input vector (float32 `[1,4]`)

| Index | Feature | Valid values | Normalization |
|---:|---|---|---|
| 0 | Study duration | 10..120 min | `duration / 120.0` |
| 1 | Phone interruptions | 0..15 | `interruptions / 15.0` |
| 2 | Total break time | 0..25 min | `breaks / 25.0` |
| 3 | Self-rated energy | 1..5 | `energy / 5.0` |

## Output tensor

`[1,2]` float32 softmax where index 0 corresponds to the generated *needs-adjustment* class and index 1 to the generated *balanced* class. The Android app calls `new Interpreter(modelBuffer)` and `Interpreter.run(input, output)` to produce this result.

## Training and limitations

The source was built using a reproducible synthetic-data Python training routine, located in `ml/train_and_export.py`, and the exported weights are in `ml/model_weights.npz`. `ml/validate_export.py` confirms FlatBuffer structure and checks numerical predictions against an independent calculation.

These labels are **not derived from human productivity studies, medical diagnoses or verified user outcomes**. No accuracy claim on real student data is supported. The model can be biased by its synthetic rules, and results should never be used for grading, employment decisions or medical advice.

## Inference/data privacy

All inference occurs on-device. The app requests no internet permission and stores up to 60 local analyses in private SharedPreferences. It does not upload the session metrics.
