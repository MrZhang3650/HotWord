package com.zcm.hotwordinsight.response;

import lombok.Getter;
/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周四 2026-3-19
 * @description：统一的返回结果
 * @modifiedBy：
 * @version:
 */


@Getter
public class R<T> {
    /**
     * 状态码
     */
    private Integer code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 数据封装
     */
    private T data;

    /**
     * 成功返回结果
     * @param code
     */
    private R(Integer code){
        this.code= code;
    }

    /**
     * 成功返回结果
     * @param code
     */
    private R(Integer code,String msg){
        this.code= code;
        this.msg= msg;
    }

    private R(Integer code, String msg, T data){
        this.code= code;
        this.msg= msg;
        this.data= data;
    }

    public static <T> R<T> success(){
        return new R<>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> R<T> success(String msg){
        return new R<>(ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <T> R<T> data(T data){
        return new R<>(ResponseCode.SUCCESS.getCode(),ResponseCode.SUCCESS.getMsg(),data);
    }

    public static <T> R<T> fall(){
        return new R<>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getMsg());
    }

    public static <T> R<T> fall(String msg){
        return new R<>(ResponseCode.ERROR.getCode(),msg);
    }

    public static <T> R<T> fall(ResponseCode responseCode){
        return new R<>(responseCode.getCode(),responseCode.getMsg());
    }

    public static <T> R<T> fall(Integer code,String msg){
        return new R<>(code,msg);
    }
}
