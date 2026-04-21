package com.zcm.hotwordinsight.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周五 2026-3-20
 * @description：
 * @modifiedBy：
 * @version:
 */
@Data
@TableName("user")
@Schema(description = "用户类")
public class User {
    /*
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /*
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;
    /*
     * 密码
     */
    @Schema(description = "密码")
    private String password;
    /*
     * 角色
     */
    @Schema(description = "角色")
    private String role;
    /**
     * email
     */
    @Schema(description = "email")
    private String email;
    /*
     * 创建时间
     */
    @Schema(description = "创建时间")
    @TableField("create_time")
    private String createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @TableField("update_time")
    private String updateTime;

    @TableField(exist = false)
    private String captchaId;
    @TableField(exist = false)
    private String captchaCode;
    @TableField(exist = false)
    private String token;
}
