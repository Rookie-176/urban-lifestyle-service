package com.roger.urbanlifestyle.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP=1735689600;
    private static final int COUNT_BITS = 32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId (String keyPrefix) {

        LocalDateTime now = LocalDateTime.now();
        long timestamp=now.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;

        String yyyyMMdd = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));

        String idKey = "icr:" + keyPrefix + ":" + yyyyMMdd;
        long count = stringRedisTemplate.opsForValue().increment(idKey);

        if (count == 1) {
            stringRedisTemplate.expire(idKey, 1, TimeUnit.DAYS);
        }

        return timestamp << 32 | count ;
    }

}
