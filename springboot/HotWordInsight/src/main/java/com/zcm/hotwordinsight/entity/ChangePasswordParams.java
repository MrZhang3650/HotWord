package com.zcm.hotwordinsight.entity;

import lombok.Data;

/**
 * 修改密码参数类
 */
@Data
public class ChangePasswordParams {
    private String oldPassword;
    private String newPassword;
}
