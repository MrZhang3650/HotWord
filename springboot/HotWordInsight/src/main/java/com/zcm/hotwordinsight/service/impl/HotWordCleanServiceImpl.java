package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcm.hotwordinsight.entity.HotWordClean;
import com.zcm.hotwordinsight.mapper.HotWordCleanMapper;
import com.zcm.hotwordinsight.service.HotWordCleanService;
import org.springframework.stereotype.Service;

/**
 * 预处理后热词服务实现类
 */
@Service
public class HotWordCleanServiceImpl extends ServiceImpl<HotWordCleanMapper, HotWordClean> implements HotWordCleanService {
}
