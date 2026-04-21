package com.zcm.hotwordinsight.util;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周五 2026-4-10
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class Captcha {
    /**
     * 验证码id
     */
    private String captchaId;
    /**
     * 验证码图片
     */
    private String captchaImage;
}
