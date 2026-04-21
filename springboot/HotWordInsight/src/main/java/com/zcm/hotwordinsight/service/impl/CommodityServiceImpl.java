package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcm.hotwordinsight.entity.Commodity;
import com.zcm.hotwordinsight.mapper.CommodityMapper;
import com.zcm.hotwordinsight.service.CommodityService;
import org.springframework.stereotype.Service;

/**
 * 商品服务实现类
 */
@Service
public class CommodityServiceImpl extends ServiceImpl<CommodityMapper, Commodity> implements CommodityService {
}
