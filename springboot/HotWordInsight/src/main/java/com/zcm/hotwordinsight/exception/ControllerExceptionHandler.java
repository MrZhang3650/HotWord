package com.zcm.hotwordinsight.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.zcm.hotwordinsight.response.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周四 2026-3-19
 * @description：
 * @modifiedBy：
 * @version:
 */
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e){
        return R.fall(e.getMessage());
    }

    @ExceptionHandler(BussinessException.class)
    public R handleBussinessException(BussinessException b){
        return R.fall(b.getCode(),b.getMsg());
    }

    @ExceptionHandler(NotLoginException.class)
    public R handleNotLoginException(NotLoginException b){
        return R.fall(b.getCode(),b.getMessage());
    }
}
