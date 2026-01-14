package com.roger.urbanlifestyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roger.urbanlifestyle.dto.LoginFormDTO;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO form, HttpSession session);
}
