package com.zcm.hotwordinsight.preprocess.step;

/**
 * 预处理步骤接口
 */
public interface PreprocessStep<T, R> {
    /**
     * 执行预处理
     */
    R execute(T data);

    /**
     * 获取步骤名称
     */
    String getName();

    /**
     * 获取步骤描述
     */
    String getDescription();
}
