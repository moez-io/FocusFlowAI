package com.moez.focusflow;

import android.content.Context;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Real on-device inference through LiteRT's TensorFlow Lite Interpreter. */
public final class FocusPredictor implements AutoCloseable {
    public static final class Prediction {
        public final float adjustmentScore;
        public final float balancedScore;
        Prediction(float adjustmentScore, float balancedScore) {
            this.adjustmentScore = adjustmentScore;
            this.balancedScore = balancedScore;
        }
    }

    private Interpreter interpreter;

    public FocusPredictor(Context context) throws Exception {
        // 1. Load the actual .tflite FlatBuffer model from the app's assets.
        ByteBuffer modelBuffer = readAsset(context, "focusflow.tflite");
        interpreter = new Interpreter(modelBuffer, new Interpreter.Options().setNumThreads(2));
        int[] input = interpreter.getInputTensor(0).shape();
        int[] output = interpreter.getOutputTensor(0).shape();
        if (input.length != 2 || input[0] != 1 || input[1] != 4 ||
                output.length != 2 || output[0] != 1 || output[1] != 2) {
            close();
            throw new IllegalStateException("Unexpected model tensor shapes");
        }
    }

    private static ByteBuffer readAsset(Context context, String name) throws Exception {
        try (InputStream stream = context.getAssets().open(name);
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] block = new byte[4096];
            int read;
            while ((read = stream.read(block)) != -1) bytes.write(block, 0, read);
            byte[] data = bytes.toByteArray();
            if (data.length == 0) throw new IllegalStateException("Empty AI model file");
            ByteBuffer buffer = ByteBuffer.allocateDirect(data.length).order(ByteOrder.nativeOrder());
            buffer.put(data);
            buffer.rewind();
            return buffer;
        }
    }

    /**
     * Feature order/normalization MUST match ml/train_and_export.py.
     * Duration 10..120, interruptions 0..15, breaks 0..25, energy 1..5.
     */
    public Prediction predict(int duration, int interruptions, int breaks, int energy) {
        if (interpreter == null) throw new IllegalStateException("Model already closed");
        if (duration < 10 || duration > 120 || interruptions < 0 || interruptions > 15 ||
                breaks < 0 || breaks > 25 || energy < 1 || energy > 5) {
            throw new IllegalArgumentException("Input outside supported range");
        }
        // 2. Encode a [1,4] FLOAT32 input tensor.
        float[][] input = {{duration / 120f, interruptions / 15f, breaks / 25f, energy / 5f}};
        // 3. Obtain the real [1,2] SOFTMAX output tensor.
        float[][] output = new float[1][2];
        interpreter.run(input, output);
        if (!Float.isFinite(output[0][0]) || !Float.isFinite(output[0][1])) {
            throw new IllegalStateException("Model returned an invalid prediction");
        }
        return new Prediction(output[0][0], output[0][1]);
    }

    @Override public void close() {
        if (interpreter != null) { interpreter.close(); interpreter = null; }
    }
}
