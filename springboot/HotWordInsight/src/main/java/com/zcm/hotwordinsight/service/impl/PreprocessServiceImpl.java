package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcm.hotwordinsight.dto.*;
import com.zcm.hotwordinsight.entity.*;
import com.zcm.hotwordinsight.mapper.PreprocessLogMapper;
import com.zcm.hotwordinsight.preprocess.step.impl.*;
import com.zcm.hotwordinsight.service.*;
import com.zcm.hotwordinsight.vo.PageResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 预处理服务实现类
 */
@Slf4j
@Service
public class PreprocessServiceImpl extends ServiceImpl<PreprocessLogMapper, PreprocessLog> implements PreprocessService {

    @Resource
    private HotWordService hotWordService;

    @Resource
    private CommodityService commodityService;

    @Resource
    private HotWordCleanService hotWordCleanService;

    @Resource
    private CommodityCleanService commodityCleanService;

    @Resource
    private CleanStep cleanStep;

    @Resource
    private SegmentStep segmentStep;

    @Resource
    private StopwordsStep stopwordsStep;

    @Resource
    private OutlierStep outlierStep;

    @Resource
    private NormalizeStep normalizeStep;

    @Resource
    private VectorizeStep vectorizeStep;

    private final PreprocessConfig config = new PreprocessConfig();

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    @Override
    @Transactional
    public ExecuteResultDTO execute(ExecutePreprocessDTO dto) {
        ExecuteResultDTO result = new ExecuteResultDTO();
        int totalCount = 0;
        int cleanSuccessCount = 0;
        int cleanFailCount = 0;
        int segmentSuccessCount = 0;
        int segmentFailCount = 0;
        int stopwordsRemovedCount = 0;
        int outlierRemovedCount = 0;
        int normalizeSuccessCount = 0;
        int vectorSuccessCount = 0;

        List<String> cleanedData = new ArrayList<>();
        List<List<String>> segmentedData = new ArrayList<>();
        List<List<String>> stopwordsRemovedData = new ArrayList<>();
        List<Double> heatValues = new ArrayList<>();
        List<HotWord> hotWordList = new ArrayList<>();
        List<Commodity> commodityList = new ArrayList<>();

        String dataType = dto.getDataType();
        String[] steps = dto.getSteps();
        Set<String> stepSet = new HashSet<>(Arrays.asList(steps));

        if ("hotword".equals(dataType)) {
            hotWordList = hotWordService.list();
            totalCount = hotWordList.size();

            if (stepSet.contains("clean")) {
                for (HotWord hotWord : hotWordList) {
                    try {
                        String cleaned = cleanStep.execute(hotWord.getKeyword());
                        if (isNotBlank(cleaned)) {
                            cleanedData.add(cleaned);
                            cleanSuccessCount++;
                        } else {
                            cleanFailCount++;
                        }
                    } catch (Exception e) {
                        cleanFailCount++;
                    }
                }
            }

            if (stepSet.contains("segment")) {
                for (String data : cleanedData) {
                    try {
                        List<String> segments = segmentStep.execute(data);
                        segmentedData.add(segments);
                        segmentSuccessCount++;
                    } catch (Exception e) {
                        segmentFailCount++;
                    }
                }
            }

            if (stepSet.contains("stopwords")) {
                for (List<String> segments : segmentedData) {
                    List<String> removed = stopwordsStep.execute(segments);
                    stopwordsRemovedData.add(removed);
                    stopwordsRemovedCount += (segments.size() - removed.size());
                }
            }

            if (stepSet.contains("outlier") || stepSet.contains("normalize")) {
                for (HotWord hotWord : hotWordList) {
                    heatValues.add(hotWord.getHeat() != null ? hotWord.getHeat().doubleValue() : 0.0);
                }
                if (stepSet.contains("outlier")) {
                    List<Double> outlierRemoved = outlierStep.execute(new ArrayList<>(heatValues));
                    outlierRemovedCount = heatValues.size() - outlierRemoved.size();
                }
            }

            if (stepSet.contains("normalize")) {
                List<Double> normalized = normalizeStep.execute(new ArrayList<>(heatValues));
                normalizeSuccessCount = normalized.size();
            }

            if (stepSet.contains("vectorize")) {
                for (String data : cleanedData) {
                    try {
                        vectorizeStep.execute(data);
                        vectorSuccessCount++;
                    } catch (Exception e) {
                        log.error("向量化失败: {}", e.getMessage());
                    }
                }
            }

            saveHotWordClean(hotWordList, cleanedData, stepSet);

        } else if ("commodity".equals(dataType)) {
            commodityList = commodityService.list();
            totalCount = commodityList.size();

            if (stepSet.contains("clean")) {
                for (Commodity commodity : commodityList) {
                    try {
                        String cleaned = cleanStep.execute(commodity.getTitle());
                        if (isNotBlank(cleaned)) {
                            cleanedData.add(cleaned);
                            cleanSuccessCount++;
                        } else {
                            cleanFailCount++;
                        }
                    } catch (Exception e) {
                        cleanFailCount++;
                    }
                }
            }

            if (stepSet.contains("segment")) {
                for (String data : cleanedData) {
                    try {
                        List<String> segments = segmentStep.execute(data);
                        segmentedData.add(segments);
                        segmentSuccessCount++;
                    } catch (Exception e) {
                        segmentFailCount++;
                    }
                }
            }

            if (stepSet.contains("vectorize")) {
                for (String data : cleanedData) {
                    try {
                        vectorizeStep.execute(data);
                        vectorSuccessCount++;
                    } catch (Exception e) {
                        log.error("向量化失败: {}", e.getMessage());
                    }
                }
            }

            saveCommodityClean(commodityList, cleanedData, stepSet);
        }

        result.setTotalCount(totalCount);

        ExecuteResultDTO.StepResult cleanResult = new ExecuteResultDTO.StepResult();
        cleanResult.setSuccessCount(cleanSuccessCount);
        cleanResult.setFailCount(cleanFailCount);
        result.setCleanResult(cleanResult);

        ExecuteResultDTO.StepResult segmentResult = new ExecuteResultDTO.StepResult();
        segmentResult.setSuccessCount(segmentSuccessCount);
        segmentResult.setFailCount(segmentFailCount);
        result.setSegmentResult(segmentResult);

        ExecuteResultDTO.StopwordsResult stopwordsResult = new ExecuteResultDTO.StopwordsResult();
        stopwordsResult.setSuccessCount(segmentSuccessCount);
        stopwordsResult.setRemovedCount(stopwordsRemovedCount);
        result.setStopwordsResult(stopwordsResult);

        ExecuteResultDTO.OutlierResult outlierResult = new ExecuteResultDTO.OutlierResult();
        outlierResult.setSuccessCount(totalCount - outlierRemovedCount);
        outlierResult.setRemovedCount(outlierRemovedCount);
        result.setOutlierResult(outlierResult);

        ExecuteResultDTO.NormalizeResult normalizeResultDTO = new ExecuteResultDTO.NormalizeResult();
        normalizeResultDTO.setSuccessCount(normalizeSuccessCount);
        ExecuteResultDTO.NormalizeResult.Range range = new ExecuteResultDTO.NormalizeResult.Range();
        range.setMin(config.getNormalizeMin());
        range.setMax(config.getNormalizeMax());
        normalizeResultDTO.setRange(range);
        result.setNormalizeResult(normalizeResultDTO);

        ExecuteResultDTO.VectorizeResult vectorizeResult = new ExecuteResultDTO.VectorizeResult();
        vectorizeResult.setSuccessCount(vectorSuccessCount);
        vectorizeResult.setDimension(config.getVectorDimension());
        result.setVectorizeResult(vectorizeResult);

        PreprocessLog preprocessLog = new PreprocessLog();
        preprocessLog.setDataType(dataType);
        preprocessLog.setOperation("数据预处理");
        preprocessLog.setTotalCount(totalCount);
        preprocessLog.setCleanCount(cleanSuccessCount);
        preprocessLog.setOutlierCount(outlierRemovedCount);
        preprocessLog.setVectorCount(vectorSuccessCount);
        preprocessLog.setStatus("success");
        preprocessLog.setMessage("处理成功，已保存到" + dataType + "_clean表");
        preprocessLog.setCreateTime(new Date());
        this.save(preprocessLog);

        return result;
    }

    private void saveHotWordClean(List<HotWord> hotWordList, List<String> cleanedData, Set<String> stepSet) {
        List<HotWordClean> cleanList = new ArrayList<>();
        Set<String> existingKeys = new HashSet<>();

        List<HotWordClean> existingList = hotWordCleanService.list();
        for (HotWordClean existing : existingList) {
            existingKeys.add(existing.getKeyword() + "_" + existing.getSource());
        }

        int index = 0;
        for (HotWord hotWord : hotWordList) {
            String keyword = index < cleanedData.size() ? cleanedData.get(index) : hotWord.getKeyword();
            String source = normalizeSource(hotWord.getSource());
            String uniqueKey = keyword + "_" + source;

            if (existingKeys.contains(uniqueKey)) {
                index++;
                continue;
            }

            HotWordClean clean = new HotWordClean();
            clean.setKeyword(keyword);
            clean.setHeat(hotWord.getHeat() != null ? hotWord.getHeat() : 0L);
            clean.setSearchCount(hotWord.getSearchCount() != null ? hotWord.getSearchCount() : 0);
            clean.setRelatedCount(hotWord.getRelatedCount() != null ? hotWord.getRelatedCount() : 0L);
            clean.setSource(source);
            clean.setCollectTime(hotWord.getCollectTime());
            clean.setCleanTime(new Date());
            cleanList.add(clean);
            existingKeys.add(uniqueKey);
            index++;
        }
        if (!cleanList.isEmpty()) {
            hotWordCleanService.saveBatch(cleanList);
        }
    }

    private void saveCommodityClean(List<Commodity> commodityList, List<String> cleanedData, Set<String> stepSet) {
        List<CommodityClean> cleanList = new ArrayList<>();
        Set<String> existingItemIds = new HashSet<>();

        List<CommodityClean> existingList = commodityCleanService.list();
        for (CommodityClean existing : existingList) {
            existingItemIds.add(existing.getItemId());
        }

        int index = 0;
        for (Commodity commodity : commodityList) {
            if (existingItemIds.contains(commodity.getItemId())) {
                index++;
                continue;
            }

            CommodityClean clean = new CommodityClean();
            clean.setItemId(commodity.getItemId());
            clean.setTitle(index < cleanedData.size() ? cleanedData.get(index) : commodity.getTitle());
            clean.setPrice(commodity.getPrice() != null ? commodity.getPrice() : java.math.BigDecimal.ZERO);
            clean.setSales(commodity.getSales() != null ? commodity.getSales() : 0);
            clean.setReviewCount(commodity.getReviewCount() != null ? commodity.getReviewCount() : 0);
            clean.setSeller(commodity.getSeller() != null ? commodity.getSeller() : "");
            clean.setCategory(commodity.getCategory() != null ? commodity.getCategory() : "");
            clean.setBrand(commodity.getBrand() != null ? commodity.getBrand() : "");
            clean.setLink(commodity.getLink() != null ? commodity.getLink() : "");
            clean.setKeyword(commodity.getKeyword() != null ? commodity.getKeyword() : "");
            clean.setSource(normalizeSource(commodity.getSource()));
            clean.setCollectTime(commodity.getCollectTime());
            clean.setCleanTime(new Date());
            cleanList.add(clean);
            existingItemIds.add(commodity.getItemId());
            index++;
        }
        if (!cleanList.isEmpty()) {
            commodityCleanService.saveBatch(cleanList);
        }
    }

    private String normalizeSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return "未知";
        }
        source = source.trim();
        if (source.contains("京东") || source.contains("JD") || source.contains("jd")) {
            return "京东";
        } else if (source.contains("淘宝") || source.contains("天猫") || source.contains("TB") || source.contains("tb")) {
            return "淘宝";
        } else if (source.contains("拼多多") || source.contains("PDD") || source.contains("pdd")) {
            return "拼多多";
        }
        return source;
    }

    @Override
    public PageResult<PreprocessLog> queryLogs(LogQueryDTO dto) {
        LambdaQueryWrapper<PreprocessLog> wrapper = new LambdaQueryWrapper<>();
        if (isNotBlank(dto.getDataType())) {
            wrapper.eq(PreprocessLog::getDataType, dto.getDataType());
        }
        if (isNotBlank(dto.getOperation())) {
            wrapper.eq(PreprocessLog::getOperation, dto.getOperation());
        }
        if (isNotBlank(dto.getStatus())) {
            wrapper.eq(PreprocessLog::getStatus, dto.getStatus());
        }
        if (isNotBlank(dto.getStartTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                wrapper.ge(PreprocessLog::getCreateTime, sdf.parse(dto.getStartTime()));
            } catch (Exception e) {
                log.error("解析开始时间失败: {}", e.getMessage());
            }
        }
        if (isNotBlank(dto.getEndTime())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                wrapper.le(PreprocessLog::getCreateTime, sdf.parse(dto.getEndTime()));
            } catch (Exception e) {
                log.error("解析结束时间失败: {}", e.getMessage());
            }
        }
        wrapper.orderByDesc(PreprocessLog::getCreateTime);

        Page<PreprocessLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<PreprocessLog> resultPage = this.page(page, wrapper);

        return PageResult.build(
                resultPage.getRecords(),
                resultPage.getTotal(),
                dto.getPageNum(),
                dto.getPageSize()
        );
    }

    @Override
    public Object getConfig() {
        return config;
    }

    @Override
    public Boolean updateConfig(ConfigUpdateDTO dto) {
        if (dto.getOutlierThreshold() != null) {
            config.setOutlierThreshold(dto.getOutlierThreshold());
            outlierStep.setThreshold(dto.getOutlierThreshold());
        }
        if (dto.getNormalizeMin() != null) {
            config.setNormalizeMin(dto.getNormalizeMin());
        }
        if (dto.getNormalizeMax() != null) {
            config.setNormalizeMax(dto.getNormalizeMax());
            normalizeStep.setRange(config.getNormalizeMin(), dto.getNormalizeMax());
        }
        return true;
    }

    @Override
    public List<PreprocessResult> preview(PreviewDTO dto) {
        List<PreprocessResult> results = new ArrayList<>();
        String dataType = dto.getDataType();

        if ("hotword".equals(dataType)) {
            List<HotWordClean> hotWordCleanList = hotWordCleanService.list();
            int count = 0;
            for (HotWordClean hotWordClean : hotWordCleanList) {
                if (count >= 10) {
                    break;
                }
                PreprocessResult result = new PreprocessResult();
                result.setId(hotWordClean.getId());
                result.setKeyword(hotWordClean.getKeyword());
                result.setHeat(hotWordClean.getHeat() != null ? java.math.BigDecimal.valueOf(hotWordClean.getHeat()) : java.math.BigDecimal.ZERO);
                result.setSearchCount(hotWordClean.getSearchCount());
                result.setRelatedCount(hotWordClean.getRelatedCount());
                result.setSource(hotWordClean.getSource());
                result.setCollectTime(hotWordClean.getCollectTime());
                results.add(result);
                count++;
            }
        } else if ("commodity".equals(dataType)) {
            List<CommodityClean> commodityCleanList = commodityCleanService.list();
            int count = 0;
            for (CommodityClean commodityClean : commodityCleanList) {
                if (count >= 10) {
                    break;
                }
                PreprocessResult result = new PreprocessResult();
                result.setId(commodityClean.getId());
                result.setItemId(commodityClean.getItemId());
                result.setTitle(commodityClean.getTitle());
                result.setPrice(commodityClean.getPrice());
                result.setSales(commodityClean.getSales());
                result.setReviewCount(commodityClean.getReviewCount());
                result.setSeller(commodityClean.getSeller());
                result.setCategory(commodityClean.getCategory());
                result.setSource(commodityClean.getSource());
                result.setCollectTime(commodityClean.getCollectTime());
                results.add(result);
                count++;
            }
        }
        return results;
    }

    @Override
    public byte[] export(ExportDTO dto) {
        String format = dto.getFormat();

        if ("excel".equals(format) || "xlsx".equals(format)) {
            return exportExcel(dto);
        } else {
            return exportCsv(dto);
        }
    }

    private byte[] exportExcel(ExportDTO dto) {
        String dataType = dto.getDataType();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("预处理结果");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if ("hotword".equals(dataType)) {
                List<HotWordClean> hotWordCleanList = hotWordCleanService.list();

                String[] headers = {"ID", "关键词", "热度", "搜索人数", "相关商品数", "来源平台", "采集时间", "清洗时间"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                for (HotWordClean hotWordClean : hotWordCleanList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(hotWordClean.getId());
                    row.createCell(1).setCellValue(hotWordClean.getKeyword());
                    row.createCell(2).setCellValue(hotWordClean.getHeat() != null ? hotWordClean.getHeat() : 0);
                    row.createCell(3).setCellValue(hotWordClean.getSearchCount() != null ? hotWordClean.getSearchCount() : 0);
                    row.createCell(4).setCellValue(hotWordClean.getRelatedCount() != null ? hotWordClean.getRelatedCount() : 0);
                    row.createCell(5).setCellValue(hotWordClean.getSource());
                    row.createCell(6).setCellValue(hotWordClean.getCollectTime() != null ? dateFormat.format(hotWordClean.getCollectTime()) : "");
                    row.createCell(7).setCellValue(hotWordClean.getCleanTime() != null ? dateFormat.format(hotWordClean.getCleanTime()) : "");
                }
            } else if ("commodity".equals(dataType)) {
                List<CommodityClean> commodityCleanList = commodityCleanService.list();

                String[] headers = {"ID", "商品ID", "标题", "价格", "销量", "评论数", "卖家", "分类", "品牌", "链接", "关联热词", "来源平台", "采集时间", "清洗时间"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                for (CommodityClean commodityClean : commodityCleanList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(commodityClean.getId());
                    row.createCell(1).setCellValue(commodityClean.getItemId());
                    row.createCell(2).setCellValue(commodityClean.getTitle());
                    row.createCell(3).setCellValue(commodityClean.getPrice() != null ? commodityClean.getPrice().doubleValue() : 0.0);
                    row.createCell(4).setCellValue(commodityClean.getSales() != null ? commodityClean.getSales() : 0);
                    row.createCell(5).setCellValue(commodityClean.getReviewCount() != null ? commodityClean.getReviewCount() : 0);
                    row.createCell(6).setCellValue(commodityClean.getSeller());
                    row.createCell(7).setCellValue(commodityClean.getCategory());
                    row.createCell(8).setCellValue(commodityClean.getBrand());
                    row.createCell(9).setCellValue(commodityClean.getLink());
                    row.createCell(10).setCellValue(commodityClean.getKeyword());
                    row.createCell(11).setCellValue(commodityClean.getSource());
                    row.createCell(12).setCellValue(commodityClean.getCollectTime() != null ? dateFormat.format(commodityClean.getCollectTime()) : "");
                    row.createCell(13).setCellValue(commodityClean.getCleanTime() != null ? dateFormat.format(commodityClean.getCleanTime()) : "");
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出Excel失败: {}", e.getMessage());
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    private byte[] exportCsv(ExportDTO dto) {
        String dataType = dto.getDataType();
        StringBuilder csv = new StringBuilder();

        if ("hotword".equals(dataType)) {
            List<HotWordClean> hotWordCleanList = hotWordCleanService.list();
            csv.append("ID,关键词,热度,搜索人数,相关商品数,来源平台,采集时间,清洗时间\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (HotWordClean hotWordClean : hotWordCleanList) {
                csv.append(hotWordClean.getId()).append(",");
                csv.append(hotWordClean.getKeyword()).append(",");
                csv.append(hotWordClean.getHeat()).append(",");
                csv.append(hotWordClean.getSearchCount()).append(",");
                csv.append(hotWordClean.getRelatedCount()).append(",");
                csv.append(hotWordClean.getSource()).append(",");
                csv.append(hotWordClean.getCollectTime() != null ? dateFormat.format(hotWordClean.getCollectTime()) : "").append(",");
                csv.append(hotWordClean.getCleanTime() != null ? dateFormat.format(hotWordClean.getCleanTime()) : "").append("\n");
            }
        } else if ("commodity".equals(dataType)) {
            List<CommodityClean> commodityCleanList = commodityCleanService.list();
            csv.append("ID,商品ID,标题,价格,销量,评论数,卖家,分类,品牌,链接,关联热词,来源平台,采集时间,清洗时间\n");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (CommodityClean commodityClean : commodityCleanList) {
                csv.append(commodityClean.getId()).append(",");
                csv.append(commodityClean.getItemId()).append(",");
                csv.append(commodityClean.getTitle()).append(",");
                csv.append(commodityClean.getPrice()).append(",");
                csv.append(commodityClean.getSales()).append(",");
                csv.append(commodityClean.getReviewCount()).append(",");
                csv.append(commodityClean.getSeller()).append(",");
                csv.append(commodityClean.getCategory()).append(",");
                csv.append(commodityClean.getBrand()).append(",");
                csv.append(commodityClean.getLink()).append(",");
                csv.append(commodityClean.getKeyword()).append(",");
                csv.append(commodityClean.getSource()).append(",");
                csv.append(commodityClean.getCollectTime() != null ? dateFormat.format(commodityClean.getCollectTime()) : "").append(",");
                csv.append(commodityClean.getCleanTime() != null ? dateFormat.format(commodityClean.getCleanTime()) : "").append("\n");
            }
        }

        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] content = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(content, 0, result, bom.length, content.length);
        return result;
    }
}
