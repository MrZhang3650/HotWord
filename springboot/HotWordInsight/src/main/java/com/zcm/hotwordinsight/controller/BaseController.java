package com.zcm.hotwordinsight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础控制器类，提供公共的注解和配置
 */
@RestController
@SaCheckLogin
@CrossOrigin
public abstract class BaseController {
}
