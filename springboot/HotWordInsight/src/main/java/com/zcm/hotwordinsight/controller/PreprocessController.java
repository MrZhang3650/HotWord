package com.zcm.hotwordinsight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zcm.hotwordinsight.dto.*;
import com.zcm.hotwordinsight.entity.PreprocessResult;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.service.PreprocessService;
import com.zcm.hotwordinsight.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 预处理控制器
 */
@RestController
@Tag(name = "数据预处理管理")
@SaCheckLogin
@CrossOrigin
public class PreprocessController {

    @Resource
    private PreprocessService preprocessService;

    @Operation(summary = "执行预处理")
    @PostMapping("/preprocess/execute")
    public R<ExecuteResultDTO> execute(@RequestBody ExecutePreprocessDTO dto) {
        ExecuteResultDTO result = preprocessService.execute(dto);
        return R.data(result);
    }

    @Operation(summary = "预处理日志列表")
    @PostMapping("/preprocess/log/list")
    public R<PageResult> queryLogs(@RequestBody LogQueryDTO dto) {
        PageResult result = preprocessService.queryLogs(dto);
        return R.data(result);
    }

    @Operation(summary = "获取预处理配置")
    @PostMapping("/preprocess/config")
    public R<Object> getConfig() {
        Object config = preprocessService.getConfig();
        return R.data(config);
    }

    @Operation(summary = "更新预处理配置")
    @PostMapping("/preprocess/config/update")
    public R<Boolean> updateConfig(@RequestBody ConfigUpdateDTO dto) {
        Boolean result = preprocessService.updateConfig(dto);
        return R.data(result);
    }

    @Operation(summary = "预处理结果预览")
    @PostMapping("/preprocess/preview")
    public R<List<PreprocessResult>> preview(@RequestBody PreviewDTO dto) {
        List<PreprocessResult> results = preprocessService.preview(dto);
        return R.data(results);
    }

    @Operation(summary = "导出预处理结果")
    @PostMapping(value = "/preprocess/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> export(@RequestBody ExportDTO dto) {
        byte[] data = preprocessService.export(dto);
        String format = dto.getFormat();
        String filename;
        String contentType;

        if ("excel".equals(format) || "xlsx".equals(format)) {
            filename = "preprocess_result.xlsx";
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else {
            filename = "preprocess_result.csv";
            contentType = "text/csv";
        }

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", encodedFilename);
        headers.setContentLength(data.length);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
