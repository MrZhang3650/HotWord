package com.zcm.hotwordinsight.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周三 2026-3-18
 * @description：
 * @modifiedBy：
 * @version:
 */
@Configuration
public class SwaggerConfig {
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HotWordInsight API")
                        .version("1.0.0")
                        .description("张宸鸣毕设项目HotWordInsightAPI文档")
                        .contact(new Contact().name("张宸鸣").email("1580273650@qq.com")));
    }
}
