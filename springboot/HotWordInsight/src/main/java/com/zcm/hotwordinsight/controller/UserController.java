package com.zcm.hotwordinsight.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zcm.hotwordinsight.entity.ChangePasswordParams;
import com.zcm.hotwordinsight.entity.User;
import com.zcm.hotwordinsight.exception.BussinessException;
import com.zcm.hotwordinsight.response.R;
import com.zcm.hotwordinsight.response.ResponseCode;
import com.zcm.hotwordinsight.service.UserService;
import com.zcm.hotwordinsight.util.CaptchaCache;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.logging.Logger;

import java.util.List;

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
@Tag(name = "热词相关信息管理")
public class UserController {
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    
    @Resource
    private UserService userService;

    @Resource
    private CaptchaCache captchaCache;

    @Operation(summary = "添加用户相关信息")
    @PostMapping("/admin/add")
    @SaCheckLogin
    @CrossOrigin
    public R add(@RequestBody User user){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        long count = userService.count(wrapper);
        if (count > 0){
            throw new BussinessException(ResponseCode.USER_EXIST);
        }
        userService.save(user);
        return R.success("添加用户成功！");
    }

    @Operation(summary = "查询用户")
    @PostMapping("/admin/list")
    @SaCheckLogin
    @CrossOrigin
    public R<PageInfo<User>> list(@RequestBody User user, @RequestParam Integer pageNum, @RequestParam Integer pageSize){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(user.getUsername()!=null, User::getUsername, user.getUsername());
        wrapper.like(user.getRole()!=null, User::getRole, user.getRole());
        wrapper.orderByDesc(User::getId);
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userService.list(wrapper);
        PageInfo<User> pageInfo = new PageInfo(list);
        return R.data(pageInfo);
    }

    @Operation(summary = "修改用户相关信息")
    @PostMapping("/admin/update")
    @SaCheckLogin
    @CrossOrigin
    public R update(@RequestBody User user){
        userService.updateById(user);
        return R.success();
    }

    @Operation(summary = "根据id删除用户相关信息")
    @PostMapping("/admin/delete")
    @SaCheckLogin
    @CrossOrigin
    public R delete(@RequestParam List<Long> ids){
        if (ids == null || ids.isEmpty()) {
            throw new BussinessException(ResponseCode.ERROR);
        }
        userService.removeByIds(ids);
        return R.success();
    }

    @Operation(summary = "登录")
    @PostMapping("/admin/login")
    @CrossOrigin
    public R<User> login(@RequestBody User user){
        /**
         * 1.验证验证码
         * 2.用户名密码验证
         * 3.satoken登录后，获取token
         */
        if (StringUtils.isBlank(user.getCaptchaId()) || StringUtils.isBlank(user.getCaptchaCode())){
            throw new BussinessException(ResponseCode.CAPTCHA_ERROR);
        }
        boolean flag = captchaCache.validateCaptcha(user.getCaptchaId(), user.getCaptchaCode());
        if (!flag){
            throw new BussinessException(ResponseCode.CAPTCHA_ERROR);
        }
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getUsername, user.getUsername());
        lambdaQueryWrapper.eq(User::getPassword, user.getPassword());
        User user1 = userService.getOne(lambdaQueryWrapper);
        if (user1 == null){
            throw new BussinessException(ResponseCode.USERNAME_USERPWD_ERROR);
        }
        // 登录，使用用户ID作为登录标识
        StpUtil.login(user1.getId());
        user1.setToken(StpUtil.getTokenValue());
        return R.data(user1);
    }

    @Operation(summary = "登出")
    @PostMapping("/admin/logout")
    @CrossOrigin
    public R logout(){
        StpUtil.logout();
        return R.success();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/user/changePassword")
    @SaCheckLogin
    @CrossOrigin
    @org.springframework.transaction.annotation.Transactional
    public R changePassword(@RequestBody ChangePasswordParams params){
        // 获取当前登录用户ID
        Object loginId = StpUtil.getLoginId();
        Integer userId;
        if (loginId instanceof String) {
            userId = Integer.parseInt((String) loginId);
        } else {
            userId = (Integer) loginId;
        }
        logger.info("修改密码 - 用户ID: " + userId);
        // 根据ID获取用户信息
        User user = userService.getById(userId);
        logger.info("修改密码 - 获取用户信息: " + user);
        if (user == null){
            throw new BussinessException(ResponseCode.USER_NOT_EXIST);
        }
        // 验证旧密码是否正确
        logger.info("修改密码 - 旧密码: " + params.getOldPassword());
        logger.info("修改密码 - 数据库密码: " + user.getPassword());
        if (!user.getPassword().equals(params.getOldPassword())){
            throw new BussinessException(ResponseCode.OLDPASSWORD_ERROR);
        }
        // 更新新密码
        user.setPassword(params.getNewPassword());
        logger.info("修改密码 - 新密码: " + params.getNewPassword());
        boolean result = userService.updateById(user);
        logger.info("修改密码 - 更新结果: " + result);
        // 强制刷新缓存
        userService.getById(userId);
        return R.success("密码修改成功！");
    }
}