package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcm.hotwordinsight.entity.CommodityClean;
import com.zcm.hotwordinsight.mapper.CommodityCleanMapper;
import com.zcm.hotwordinsight.service.CommodityCleanService;
import org.springframework.stereotype.Service;

/**
 * 预处理后商品服务实现类
 */
@Service
public class CommodityCleanServiceImpl extends ServiceImpl<CommodityCleanMapper, CommodityClean> implements CommodityCleanService {
}
