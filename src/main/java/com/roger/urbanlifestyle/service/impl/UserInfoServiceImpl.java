package com.roger.urbanlifestyle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.entity.UserInfo;
import com.roger.urbanlifestyle.mapper.UserInfoMapper;
import com.roger.urbanlifestyle.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 
 * 2021-12-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
