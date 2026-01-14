package com.roger.urbanlifestyle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.entity.User;
import com.roger.urbanlifestyle.mapper.UserMapper;
import com.roger.urbanlifestyle.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * 
 * 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
