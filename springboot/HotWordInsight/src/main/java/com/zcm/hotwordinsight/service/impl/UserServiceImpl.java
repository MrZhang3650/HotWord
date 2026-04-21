package com.zcm.hotwordinsight.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcm.hotwordinsight.entity.HotWord;
import com.zcm.hotwordinsight.entity.User;
import com.zcm.hotwordinsight.mapper.HotWordMapper;
import com.zcm.hotwordinsight.mapper.UserMapper;
import com.zcm.hotwordinsight.service.HotWordService;
import com.zcm.hotwordinsight.service.UserService;
import org.springframework.stereotype.Service;

/**
 * Created with IntelliJ IDEA.
 *
 * @author： ZhangChenMing
 * @date： 周三 2026-3-18
 * @description：
 * @modifiedBy：
 * @version:
 */
@Service
@org.springframework.transaction.annotation.Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
