package com.zcm.hotwordinsight.preprocess.step.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.zcm.hotwordinsight.preprocess.step.PreprocessStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 向量化步骤实现
 */
@Slf4j
@Component
public class VectorizeStep implements PreprocessStep<String, List<Double>> {
    private Integer dimension = 128;
    private final Random random = new Random(42);

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    @Override
    public List<Double> execute(String data) {
        List<Double> vector = new ArrayList<>(dimension);
        if (data == null || data.isEmpty()) {
            for (int i = 0; i < dimension; i++) {
                vector.add(0.0);
            }
            return vector;
        }
        int hash = data.hashCode();
        random.setSeed(hash);
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextDouble() * 2 - 1);
        }
        return normalize(vector);
    }

    private List<Double> normalize(List<Double> vector) {
        double magnitude = 0.0;
        for (Double value : vector) {
            magnitude += value * value;
        }
        magnitude = Math.sqrt(magnitude);
        if (magnitude == 0) {
            magnitude = 1.0;
        }
        List<Double> normalized = new ArrayList<>();
        for (Double value : vector) {
            normalized.add(value / magnitude);
        }
        return normalized;
    }

    @Override
    public String getName() {
        return "vectorize";
    }

    @Override
    public String getDescription() {
        return "向量化：将文本转换为向量表示";
    }
}
