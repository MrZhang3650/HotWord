package com.zcm.hotwordinsight.preprocess.step.impl;

import com.zcm.hotwordinsight.preprocess.step.PreprocessStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 分词步骤实现
 */
@Slf4j
@Component
public class SegmentStep implements PreprocessStep<String, List<String>> {

    @Override
    public List<String> execute(String data) {
        if (StrUtil.isBlank(data)) {
            return new ArrayList<>();
        }
        List<String> segments = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (isChinese(c)) {
                if (currentWord.length() > 0) {
                    segments.add(currentWord.toString());
                    currentWord = new StringBuilder();
                }
                if (i + 1 < data.length() && isChinese(data.charAt(i + 1))) {
                    currentWord.append(c);
                } else {
                    segments.add(String.valueOf(c));
                }
            } else {
                currentWord.append(c);
            }
        }
        if (currentWord.length() > 0) {
            segments.add(currentWord.toString());
        }
        return segments;
    }

    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }

    @Override
    public String getName() {
        return "segment";
    }

    @Override
    public String getDescription() {
        return "分词：将文本切分为单词或字符序列";
    }
}
