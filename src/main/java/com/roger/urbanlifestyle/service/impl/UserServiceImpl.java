package com.roger.urbanlifestyle.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.dto.LoginFormDTO;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.dto.UserDTO;
import com.roger.urbanlifestyle.entity.User;
import com.roger.urbanlifestyle.mapper.UserMapper;
import com.roger.urbanlifestyle.service.IUserService;
import com.roger.urbanlifestyle.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.roger.urbanlifestyle.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {

        // Validate the phone number
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号校验失败！");
        }

        // TODO: 当前逻辑是有问题的，如果换手机号B，仍然采用A获得的验证码，一样可以登录

        // Generate 6 random numbers as code
        String code = RandomUtil.randomNumbers(6);

        // Set it to the session for further check
        session.setAttribute("code", code);

        // TODO:在阿里云上实现真实验证码发送
        log.debug("发送成功，验证码为：" + code);

        // Return state
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO form, HttpSession session) {
        String phone = form.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号校验失败！");
        }

        String code = form.getCode();
        String code_expected = (String)session.getAttribute("code");
        if (code_expected == null || !code_expected.equals(code)) {
            return Result.fail("验证码错误！");
        }

        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }
        
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX +RandomUtil.randomString(12));
        save(user);
        return user;
    }
}
