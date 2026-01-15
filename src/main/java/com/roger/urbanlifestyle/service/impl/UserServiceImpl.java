package com.roger.urbanlifestyle.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.dto.LoginFormDTO;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.dto.UserDTO;
import com.roger.urbanlifestyle.entity.User;
import com.roger.urbanlifestyle.mapper.UserMapper;
import com.roger.urbanlifestyle.service.IUserService;
import com.roger.urbanlifestyle.utils.RedisConstants;
import com.roger.urbanlifestyle.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.roger.urbanlifestyle.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {

        // Validate the phone number
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号校验失败！");
        }

        // Generate 6 random numbers as code
        String code = RandomUtil.randomNumbers(6);

        // Set it to Redis for further check
//        session.setAttribute("code", code);
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

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
//        String code_expected = (String)session.getAttribute("code");
        String code_expected = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        if (code_expected == null || !code_expected.equals(code)) {
            return Result.fail("验证码错误！");
        }

        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(userDTO,
                new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, stringObjectMap);
        stringRedisTemplate.expire(tokenKey,RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX +RandomUtil.randomString(12));
        save(user);
        return user;
    }
}
