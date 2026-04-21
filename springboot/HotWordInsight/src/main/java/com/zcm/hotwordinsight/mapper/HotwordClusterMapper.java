package com.zcm.hotwordinsight.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcm.hotwordinsight.entity.HotwordCluster;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

/**
 * 热词聚类结果Mapper接口
 */
@Mapper
public interface HotwordClusterMapper extends BaseMapper<HotwordCluster> {

}
