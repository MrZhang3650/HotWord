package com.zcm.hotwordinsight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.service.RelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 关联分析控制器
 */
@Tag(name = "关联分析管理")
@RestController
@RequestMapping("/relation")
public class RelationController extends BaseController {

    @Resource
    private RelationService relationService;

    @Operation(summary = "执行关联分析")
    @PostMapping("/run")
    public R<Map<String, Object>> runRelationAnalysis(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        double minSupport = params.get("minSupport") != null ? 
            Double.parseDouble(params.get("minSupport").toString()) : 0.1;
        double minConfidence = params.get("minConfidence") != null ? 
            Double.parseDouble(params.get("minConfidence").toString()) : 0.5;
        Map<String, Object> result = relationService.runRelationAnalysis(dataType, minSupport, minConfidence);
        return R.data(result);
    }

    @Operation(summary = "获取关联规则列表")
    @PostMapping("/list")
    public R<List<Map<String, Object>>> getRelationList(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        List<Map<String, Object>> result = relationService.getRelationList(dataType);
        return R.data(result);
    }

    @Operation(summary = "获取可视化图表数据")
    @PostMapping("/chart")
    public R<Map<String, Object>> getRelationChartData(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        Map<String, Object> result = relationService.getRelationChartData(dataType);
        return R.data(result);
    }

    @Operation(summary = "清空历史规则")
    @PostMapping("/clear")
    public R<Boolean> clearRelationData(@RequestBody Map<String, Object> params) {
        String dataType = (String) params.get("dataType");
        boolean result = relationService.clearRelationData(dataType);
        return R.data(result);
    }
}
