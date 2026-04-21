package com.zcm.hotwordinsight.service;

import java.util.List;
import java.util.Map;

/**
 * 关联分析服务接口
 */
public interface RelationService {

    /**
     * 执行关联分析
     * @param dataType 数据类型：hotword/commodity
     * @param minSupport 最小支持度
     * @param minConfidence 最小置信度
     * @return 关联分析结果
     */
    Map<String, Object> runRelationAnalysis(String dataType, double minSupport, double minConfidence);

    /**
     * 获取关联规则列表
     * @param dataType 数据类型
     * @return 关联规则列表
     */
    List<Map<String, Object>> getRelationList(String dataType);

    /**
     * 获取可视化图表数据
     * @param dataType 数据类型
     * @return 图表数据
     */
    Map<String, Object> getRelationChartData(String dataType);

    /**
     * 清空历史规则
     * @param dataType 数据类型
     * @return 是否成功
     */
    boolean clearRelationData(String dataType);
}
