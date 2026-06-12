package com.example.smartfarmapp.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteHelper {

    private Interpreter plantInterpreter;
    private List<String> plantLabels = new ArrayList<>();

    private Interpreter diseaseInterpreter;
    private List<String> diseaseLabels = new ArrayList<>();

    private static final int IMAGE_SIZE = 224; // 모델 사이즈
    private static final String TAG = "TFLiteHelper_Log";

    // 1. 모델과 라벨 불러오기 (초기화)
    public void init(Context context) {
        try {
            // 🌿 식물 모델 로드
            plantInterpreter = new Interpreter(loadModelFile(context, "plant_model.tflite"));
            plantLabels = loadLabels(context, "labels.txt");
            Log.d(TAG, "식물 모델 초기화 성공");

            // 🐛 병해충 모델 로드 (파일 이름 d.model.tflite 확인!)
            diseaseInterpreter = new Interpreter(loadModelFile(context, "d.model.tflite"));
            diseaseLabels = loadLabels(context, "label_d.txt");
            Log.d(TAG, "병해충 모델 초기화 성공");

        } catch (Exception e) {
            Log.e(TAG, "AI 초기화 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        android.content.res.AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private List<String> loadLabels(Context context, String labelPath) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    // 🌿 식물 분류
    public String classifyPlant(Bitmap bitmap) {
        if (plantInterpreter == null || plantLabels.isEmpty()) return "인식 불가";
        return runInference(plantInterpreter, plantLabels, bitmap, 0.4f, true);
    }

    // 🐛 병해충 분류
    public String classifyDisease(Bitmap bitmap) {
        if (diseaseInterpreter == null || diseaseLabels.isEmpty()) return "인식 불가";
        // 병해충은 숫자가 포함되어 있을 수 있으므로 숫자 제거 안함(false)
        return runInference(diseaseInterpreter, diseaseLabels, bitmap, 0.5f, false);
    }

    // 공용 추론 로직
    private String runInference(Interpreter interpreter, List<String> labels, Bitmap bitmap, float threshold, boolean removeNumbers) {
        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedBitmap);

            float[][] result = new float[1][labels.size()];
            interpreter.run(byteBuffer, result);

            return getBestLabel(result[0], labels, threshold, removeNumbers);
        } catch (Exception e) {
            Log.e(TAG, "추론 에러: " + e.getMessage());
            return "분석 실패";
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < IMAGE_SIZE; ++i) {
            for (int j = 0; j < IMAGE_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((val & 0xFF) / 255.0f);
            }
        }
        return byteBuffer;
    }

    private String getBestLabel(float[] probabilities, List<String> labelList, float threshold, boolean removeNumbers) {
        int maxIndex = -1;
        float maxProb = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }

        if (maxIndex == -1 || maxProb < threshold) return "알 수 없음";

        String bestLabel = labelList.get(maxIndex);
        if (removeNumbers) {
            // "0 바질" -> "바질" 처리
            return bestLabel.replaceAll("[0-9]", "").trim();
        }
        return bestLabel.trim();
    }
}