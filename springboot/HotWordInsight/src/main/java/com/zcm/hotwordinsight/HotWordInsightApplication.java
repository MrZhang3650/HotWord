package com.zcm.hotwordinsight;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zcm.hotwordinsight.mapper")
public class HotWordInsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotWordInsightApplication.class, args);
    }

}
