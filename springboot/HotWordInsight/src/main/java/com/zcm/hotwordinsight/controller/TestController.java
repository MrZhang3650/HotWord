package com.zcm.hotwordinsight.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周三 2026-3-18
 * @description：
 * @modifiedBy：
 * @version:
 */
@RestController
public class TestController {
    @RequestMapping("/test")
    public String test(@RequestParam String name){
        return "Hello "+name;
    }
}
