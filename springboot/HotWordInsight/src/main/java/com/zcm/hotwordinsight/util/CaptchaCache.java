package com.zcm.hotwordinsight.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周五 2026-4-10
 * @description：
 * @modifiedBy：
 * @version:
 */
@Component
public class CaptchaCache {
    //验证码缓存
    private static ConcurrentHashMap<String,String> captchaMap = new ConcurrentHashMap<>();
    public void storeCaptcha(String captchaId,String captcha){
        captchaMap.put(captchaId,captcha);
    }

    //移除验证码
    public void removeCaptcha(String captchaId){
        captchaMap.remove(captchaId);
    }

    //验证验证码
    public boolean validateCaptcha(String captchaId,String captcha){
        System.out.println("验证码是什么："+captcha);
        //获取验证码
        String captchaCode = captchaMap.get(captchaId);
        if(captchaCode == null){
            return false;
        }else {
            //验证码正确
            if (captchaCode.equals(captcha)){
                captchaMap.remove(captchaId);
                return true;
            }else {
                //验证码错误
                return false;
            }
        }
    }
}
