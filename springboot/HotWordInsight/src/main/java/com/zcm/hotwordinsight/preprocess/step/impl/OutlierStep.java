package com.zcm.hotwordinsight.preprocess.step.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.zcm.hotwordinsight.preprocess.step.PreprocessStep;

import java.util.ArrayList;
import java.util.List;

/**
 * 异常剔除步骤实现
 */
@Slf4j
@Component
public class OutlierStep implements PreprocessStep<List<Double>, List<Double>> {
    private Double threshold = 3.0;

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<Double> execute(List<Double> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        if (data.size() < 3) {
            return new ArrayList<>(data);
        }
        double mean = calculateMean(data);
        double stdDev = calculateStdDev(data, mean);
        List<Double> result = new ArrayList<>();
        for (Double value : data) {
            if (Math.abs(value - mean) <= threshold * stdDev) {
                result.add(value);
            }
        }
        return result;
    }

    private double calculateMean(List<Double> data) {
        double sum = 0.0;
        for (Double value : data) {
            sum += value;
        }
        return sum / data.size();
    }

    private double calculateStdDev(List<Double> data, double mean) {
        double sumSquaredDiff = 0.0;
        for (Double value : data) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sumSquaredDiff / data.size());
    }

    @Override
    public String getName() {
        return "outlier";
    }

    @Override
    public String getDescription() {
        return "异常剔除：基于标准差方法剔除异常值";
    }
}
