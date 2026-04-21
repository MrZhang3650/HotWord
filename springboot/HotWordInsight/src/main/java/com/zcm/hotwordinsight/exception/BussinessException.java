package com.zcm.hotwordinsight.exception;

import com.zcm.hotwordinsight.response.ResponseCode;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周四 2026-3-19
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
public class BussinessException extends RuntimeException{
    /**
     * 业务异常码
     */
    private Integer code;
    /**
     * 业务异常信息
     */
    private String msg;

    public BussinessException(Integer code,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BussinessException(String msg) {
        this.msg = msg;
    }

    public BussinessException(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMsg();
    }
}
