package com.zcm.hotwordinsight.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zcm.hotwordinsight.entity.ClusterInfo;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

/**
 * 聚类中心信息Mapper接口
 */
@Mapper
public interface ClusterInfoMapper extends BaseMapper<ClusterInfo> {

}
