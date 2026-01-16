package com.roger.urbanlifestyle.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.util.BooleanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
        String jsonStr = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, jsonStr, time, timeUnit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        String jsonStr = JSONUtil.toJsonStr(redisData);
        stringRedisTemplate.opsForValue().set(key, jsonStr);
    }

    public <R, ID> R queryWithPassThrough (
            String keyPrefix, ID id, Class<R> clazz, Function<ID, R> dbFallback, Long time, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, clazz);
        }
        if (json != null) {
            return null;
        }

        R r = dbFallback.apply(id);
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(r), time, timeUnit);

        return r;
    }

    public <ID, R> R queryWithLogicalExpire (
            String keyPrefix, ID id, Class<R> clazz, Function<ID, R> dbFallback, Long time, TimeUnit timeUnit) {

        String key = keyPrefix + id;
        String redisDataJson = stringRedisTemplate.opsForValue().get(key);

        if (StrUtil.isBlank(redisDataJson)) {
            return null;
        }

        RedisData redisData = JSONUtil.toBean(redisDataJson, RedisData.class);
        R r = JSONUtil.toBean((JSONObject)redisData.getData(), clazz);

        LocalDateTime expireTime = redisData.getExpireTime();

        if (expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean locked = tryLock(lockKey);
        if (locked){
            redisDataJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(redisDataJson)) {
                redisData = JSONUtil.toBean(redisDataJson, RedisData.class);
                r = JSONUtil.toBean((JSONObject)redisData.getData(), clazz);
                expireTime = redisData.getExpireTime();
                if (expireTime.isAfter(LocalDateTime.now())) {
                    return r;
                }
            }
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R r1 = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, r1, time, timeUnit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    releaseLock(lockKey);
                }
            });
        }
        return r;
    }

    private boolean tryLock (String key) {
        return BooleanUtil.isTrue(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS));
    }

    private void releaseLock (String key) {
        stringRedisTemplate.delete(key);
    }

}
