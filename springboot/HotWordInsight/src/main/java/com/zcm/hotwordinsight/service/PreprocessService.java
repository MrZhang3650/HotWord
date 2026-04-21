package com.zcm.hotwordinsight.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zcm.hotwordinsight.dto.*;
import com.zcm.hotwordinsight.entity.PreprocessLog;
import com.zcm.hotwordinsight.entity.PreprocessResult;
import com.zcm.hotwordinsight.vo.PageResult;

import java.util.List;

/**
 * 预处理服务接口
 */
public interface PreprocessService extends IService<PreprocessLog> {
    /**
     * 执行预处理
     */
    ExecuteResultDTO execute(ExecutePreprocessDTO dto);

    /**
     * 查询预处理日志列表
     */
    PageResult<PreprocessLog> queryLogs(LogQueryDTO dto);

    /**
     * 获取预处理配置
     */
    Object getConfig();

    /**
     * 更新预处理配置
     */
    Boolean updateConfig(ConfigUpdateDTO dto);

    /**
     * 预览预处理结果
     */
    List<PreprocessResult> preview(PreviewDTO dto);

    /**
     * 导出预处理结果
     */
    byte[] export(ExportDTO dto);
}
