package com.zcm.hotwordinsight.preprocess.step.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.zcm.hotwordinsight.preprocess.step.PreprocessStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 去停用词步骤实现
 */
@Slf4j
@Component
public class StopwordsStep implements PreprocessStep<List<String>, List<String>> {
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "的", "了", "和", "是", "在", "我", "有", "个", "人", "这",
            "不", "也", "就", "都", "要", "会", "对", "与", "到", "说",
            "而", "为", "之", "于", "从", "到", "以", "及", "或", "但",
            "其", "被", "由", "所", "该", "并", "已", "只", "将", "可",
            "能", "如", "等", "他", "她", "它", "们", "你", "我", "您",
            "啊", "吧", "呢", "哦", "呀", "哪", "吗", "的", "得", "地",
            "着", "过", "来", "去", "里", "还", "再", "把", "让", "给"
    ));

    @Override
    public List<String> execute(List<String> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String word : data) {
            if (!STOPWORDS.contains(word.trim().toLowerCase())) {
                result.add(word);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "stopwords";
    }

    @Override
    public String getDescription() {
        return "去停用词：移除常见无意义词汇";
    }
}
