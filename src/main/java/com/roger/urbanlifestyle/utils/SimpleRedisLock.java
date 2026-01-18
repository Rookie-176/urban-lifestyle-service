package com.roger.urbanlifestyle.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements Ilock {

    private String name;
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>();
    static {
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    public SimpleRedisLock(String name, StringRedisTemplate template) {
        this.name = name;
        this.stringRedisTemplate = template;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    @Override
    public boolean trylock(long timeoutSec) {
        String value = ID_PREFIX + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + name, value, timeoutSec, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(success);
    }

//    @Override
//    public void unlock() {
//        String id = ID_PREFIX + Thread.currentThread().getId();
//        String lock_id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
//        if (lock_id != null && lock_id.equals(id)) {
//            stringRedisTemplate.delete(KEY_PREFIX +name);
//        }
//    }

    @Override
    public void unlock() {

        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId()
                );
    }
}
