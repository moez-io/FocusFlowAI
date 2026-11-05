#!/usr/bin/env python3
"""Reproducible educational synthetic-data training and FLOAT32 TFLite export.

This is not a scientifically validated study-performance prediction model.
Requires numpy and torch; uses the bundled educational FlatBuffer schema writer
for the very small Dense->ReLU->Dense->Softmax graph.
"""
from pathlib import Path
import json
import numpy as np
import torch
from torch import nn
from flatbuffer_writer import export_network

BASE = Path(__file__).resolve().parents[1]
RNG = np.random.default_rng(20261105)
torch.manual_seed(20261105)
torch.set_num_threads(2)


def generate(n):
    # Realistic field ranges reflect the Android form's accepted input values.
    minutes = RNG.integers(10, 121, n)
    interruptions = RNG.integers(0, 16, n)
    breaks = RNG.integers(0, 26, n)
    energy = RNG.integers(1, 6, n)
    features = np.column_stack((minutes / 120., interruptions / 15.,
                                breaks / 25., energy / 5.)).astype(np.float32)

    # Educational target definition, not a measurement of learning ability:
    # More focused study time, some breaks and higher reported energy help;
    # repeated interruptions and excessive breaks reduce the synthetic score.
    latent = (2.25 * np.minimum(minutes, 90) / 90.
              + 0.65 * np.minimum(breaks, 15) / 15.
              + 1.20 * (energy - 1) / 4.
              - 2.30 * interruptions / 10.
              - 1.60
              - 0.30 * np.maximum(breaks - 20, 0) / 5.
              + RNG.normal(0, 0.16, n))
    labels = (latent > 0).astype(np.int64)
    return features, labels


def main():
    xtrain, ytrain = generate(8000)
    xtest, ytest = generate(1600)
    model = nn.Sequential(nn.Linear(4, 16), nn.ReLU(), nn.Linear(16, 2))
    optim = torch.optim.Adam(model.parameters(), lr=0.018)
    lossfn = nn.CrossEntropyLoss()
    a = torch.from_numpy(xtrain)
    b = torch.from_numpy(ytrain)
    for epoch in range(180):
        model.train()
        optim.zero_grad()
        loss = lossfn(model(a), b)
        loss.backward()
        optim.step()
    model.eval()
    with torch.no_grad():
        preds = model(torch.from_numpy(xtest)).argmax(dim=1).numpy()
    acc = float(np.mean(preds == ytest))
    sd = model.state_dict()
    w0 = sd['0.weight'].cpu().numpy().astype(np.float32)
    b0 = sd['0.bias'].cpu().numpy().astype(np.float32)
    # Apply a fixed temperature of 3 to temper overconfident demonstration scores.
    # This does NOT calibrate the model against real student outcomes.
    w1 = sd['2.weight'].cpu().numpy().astype(np.float32) / 3.0
    b1 = sd['2.bias'].cpu().numpy().astype(np.float32) / 3.0
    blob = export_network(w0, b0, w1, b1,
                          'FocusFlowAI demo: educational focus-pattern classifier; '
                          'synthetic training labels; not empirically validated')
    out = BASE / 'app/src/main/assets/focusflow.tflite'
    out.write_bytes(blob)
    np.savez_compressed(BASE / 'ml/model_weights.npz', w0=w0, b0=b0, w1=w1, b1=b1)
    with torch.no_grad():
        def infer(minutes, interruptions, breaks, energy):
            xx = torch.tensor([[minutes/120, interruptions/15, breaks/25, energy/5]],
                              dtype=torch.float32)
            # Inference using EXACTLY the exported, temperature-scaled weights.
            v = xx.numpy().reshape(-1)
            h = np.maximum(w0 @ v + b0, 0.0)
            logits = w1 @ h + b1
            z = np.exp(logits - np.max(logits))
            return float(z[1] / z.sum())
        examples = {
            'balanced_sample':[60,2,10,4],
            'distracted_sample':[40,9,0,2],
            'neutral_sample':[45,4,8,3],
        }
        scores = {k: round(infer(*v), 5) for k,v in examples.items()}
    summary = {
        'model_name':'FocusFlowAI Synthetic Focus Pattern Neural Network',
        'runtime':'TensorFlow Lite / LiteRT Interpreter',
        'architecture':'FLOAT32 FullyConnected(4->16, RELU) + FullyConnected(16->2) + SOFTMAX',
        'model_file':str(out.relative_to(BASE)),
        'training_samples':len(xtrain), 'synthetic_holdout_samples':len(xtest),
        'synthetic_holdout_accuracy':round(acc, 4),
        'limitation':'Synthetic pedagogical labels; no measured human performance ground truth; not a validated predictor.',
        'input_normalization':['minutes/120','interruptions/15','break_minutes/25','energy/5'],
        'input_shape':[1,4], 'output_shape':[1,2],
        'output_classes':['needs adjustment','balanced pattern'],
        'example_balanced_class_scores':scores,
        'model_size_bytes':len(blob),
        'training_seed':20261105,
        'display_temperature':3.0,
    }
    (BASE / 'ml/training_summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
    print(json.dumps(summary,indent=2))


if __name__=='__main__':
    main()
