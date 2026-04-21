package com.zcm.hotwordinsight.preprocess.step.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.zcm.hotwordinsight.preprocess.step.PreprocessStep;

import java.util.ArrayList;
import java.util.List;

/**
 * 标准化步骤实现
 */
@Slf4j
@Component
public class NormalizeStep implements PreprocessStep<List<Double>, List<Double>> {
    private Double minValue = 0.0;
    private Double maxValue = 1.0;

    public void setRange(Double min, Double max) {
        this.minValue = min;
        this.maxValue = max;
    }

    @Override
    public List<Double> execute(List<Double> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        double dataMin = data.stream().min(Double::compareTo).orElse(0.0);
        double dataMax = data.stream().max(Double::compareTo).orElse(1.0);
        double range = dataMax - dataMin;
        if (range == 0) {
            range = 1.0;
        }
        List<Double> result = new ArrayList<>();
        for (Double value : data) {
            double normalized = (value - dataMin) / range * (maxValue - minValue) + minValue;
            result.add(normalized);
        }
        return result;
    }

    @Override
    public String getName() {
        return "normalize";
    }

    @Override
    public String getDescription() {
        return "标准化：将数值缩放到指定范围";
    }
}
