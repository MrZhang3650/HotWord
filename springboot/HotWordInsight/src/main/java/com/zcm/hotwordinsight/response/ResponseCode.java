 package com.zcm.hotwordinsight.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周四 2026-3-19
 * @description：
 * @modifiedBy：
 * @version:
 */
@AllArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(200,"操作成功"),
    ERROR(500,"操作失败"),
    KEYWORD_EXIST(1001,"热词已存在"),
    USER_EXIST(1002,"用户已存在"),
    USERNAME_USERPWD_ERROR(1003,"用户名或密码错误"),
    USER_NOT_EXIST(1004,"用户不存在"),
    OLDPASSWORD_ERROR(1005,"旧密码错误"),
    CREATE_CAPTCHA_ERROR(2001,"获取验证码失败"),
    CAPTCHA_ERROR(2002,"验证码错误");

    /**
     * 响应状态码
     */
    private Integer Code;
    /**
     * 响应信息
     */
    private String msg;
}
