package com.zcm.hotwordinsight.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.zcm.hotwordinsight.exception.BussinessException;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.response.ResponseCode;
import com.zcm.hotwordinsight.util.Captcha;
import com.zcm.hotwordinsight.util.CaptchaCache;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周日 2026-3-22
 * @description：
 * @modifiedBy：
 * @version:
 */
@RestController
public class CommonController {

    @Resource
    private DefaultKaptcha defaultKaptcha;

    @Resource
    private CaptchaCache captchaCache;

    /**
     * 获取验证码
     * @return
     */
    @CrossOrigin
    @GetMapping("/common/GetCaptcha")
    public R<Captcha> getCaptcha() {
        String captchaCode = defaultKaptcha.createText();
        BufferedImage captchaImage = defaultKaptcha.createImage(captchaCode);
        String base64Code = "";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(captchaImage, "jpg", os);
            base64Code = Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException e) {
            throw new BussinessException(ResponseCode.CREATE_CAPTCHA_ERROR);
        }
        Captcha captcha = new Captcha();
        captcha.setCaptchaImage("data:image/png;base64," + base64Code);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        captcha.setCaptchaId(captchaId);
        captchaCache.storeCaptcha(captchaId,captchaCode);
        System.out.println("验证码：" + captchaCode);
        return R.data(captcha);
    }
}