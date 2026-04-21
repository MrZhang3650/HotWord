package com.zcm.hotwordinsight.preprocess.step.impl;

import com.zcm.hotwordinsight.preprocess.step.PreprocessStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据清洗步骤实现
 */
@Slf4j
@Component
public class CleanStep implements PreprocessStep<String, String> {

    @Override
    public String execute(String data) {
        if (data == null || data.trim().isEmpty()) {
            return null;
        }
        String cleaned = data.trim();
        cleaned = cleaned.replaceAll("[\\s\\p{Z}]+", " ");
        cleaned = cleaned.replaceAll("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】《》「」『』；：\"\"''.，、？]", "");
        cleaned = cleaned.replaceAll("[0-9a-zA-Z]", "");
        cleaned = cleaned.replaceAll("\\s+", "");
        return cleaned.isEmpty() ? null : cleaned;
    }

    @Override
    public String getName() {
        return "clean";
    }

    @Override
    public String getDescription() {
        return "数据清洗：去除特殊字符、空白字符、英文数字等";
    }
}
