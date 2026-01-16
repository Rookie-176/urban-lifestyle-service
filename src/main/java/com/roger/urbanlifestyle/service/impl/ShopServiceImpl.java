package com.roger.urbanlifestyle.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.entity.Shop;
import com.roger.urbanlifestyle.mapper.ShopMapper;
import com.roger.urbanlifestyle.service.IShopService;
import com.roger.urbanlifestyle.utils.CacheClient;
import com.roger.urbanlifestyle.utils.RedisConstants;
import com.roger.urbanlifestyle.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    CacheClient cacheClient;

    @Resource
    StringRedisTemplate stringRedisTemplate;
//    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @Override
    public Result queryShopById(Long id) {
        // cache penetration prevention
//         Shop shop = cacheClient.queryWithPassThrough(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // prevent cache breakdown with mutex lock
//        Shop shop = queryWithMutex(id);

        // prevent cache breakdown with logical expire
        Shop shop = cacheClient.queryWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.fail("shop doesn't exist!");
        }
        return Result.ok(shop);
    }

//    public Shop queryWithMutex (Long id) {
//        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
//        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
//
//        if (StrUtil.isNotBlank(shopJson)) {
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//        if (shopJson != null) {
//            return null;
//        }
//
//        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//        Shop shop = null;
//        boolean locked = false;
//        try {
//            locked = tryLock(lockKey);
//            if (!locked){
//                Thread.sleep(50);
//                return queryWithMutex(id);
//            }
//
//            shopJson = stringRedisTemplate.opsForValue().get(shopKey);
//            if (StrUtil.isNotBlank(shopJson)) {
//                return JSONUtil.toBean(shopJson, Shop.class);
//            }
//            if (shopJson != null) {
//                return null;
//            }
//
//            shop = query().eq("id", id).one();
//            // for test: simulate complex process
//            Thread.sleep(200);
//
//            if (shop == null) {
//                stringRedisTemplate.opsForValue().set(shopKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//            stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            if (locked) {
//                releaseLock(lockKey);
//            }
//        }
//        return shop;
//    }

//    public Shop queryWithLogicalExpire (Long id) {
//        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
//        String redisDataJson = stringRedisTemplate.opsForValue().get(shopKey);
//
//        if (StrUtil.isBlank(redisDataJson)) {
//            return null;
//        }
//
//        RedisData redisData = JSONUtil.toBean(redisDataJson, RedisData.class);
//        Shop shop = JSONUtil.toBean((JSONObject)redisData.getData(), Shop.class);
//
//        LocalDateTime expireTime = redisData.getExpireTime();
//        if (expireTime.isAfter(LocalDateTime.now())) {
//            return shop;
//        }
//
//        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//        boolean locked = false;
//        locked = tryLock(lockKey);
//        if (locked){
//
//            redisDataJson = stringRedisTemplate.opsForValue().get(shopKey);
//            if (StrUtil.isNotBlank(redisDataJson)) {
//                redisData = JSONUtil.toBean(redisDataJson, RedisData.class);
//                shop = JSONUtil.toBean((JSONObject)redisData.getData(), Shop.class);
//                expireTime = redisData.getExpireTime();
//                if (expireTime.isAfter(LocalDateTime.now())) {
//                    return shop;
//                }
//            }
//
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    saveShop2Redis(id, 20L);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    releaseLock(lockKey);
//                }
//            });
//        }
//
//        return shop;
//    }

//    public Shop queryWithPassThrough (Long id) {
//        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
//        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
//
//        if (StrUtil.isNotBlank(shopJson)) {
//            return JSONUtil.toBean(shopJson, Shop.class);
//        }
//        if (shopJson != null) {
//            return null;
//        }
//
//        Shop shop = getById(id);
//        if (shop == null) {
//            stringRedisTemplate.opsForValue().set(shopKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null;
//        }
//        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//
//        return shop;
//    }

    @Override
    @Transactional
    public Result updateShop(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("id is null");
        }

        updateById(shop);

        String shopKey = RedisConstants.CACHE_SHOP_KEY + shop.getId();
        stringRedisTemplate.delete(shopKey);
        return Result.ok();
    }

//    private boolean tryLock (String key) {
//        return BooleanUtil.isTrue(stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS));
//    }
//
//    private void releaseLock (String key) {
//        stringRedisTemplate.delete(key);
//    }

//    public void saveShop2Redis(Long id, Long expireSecond) throws InterruptedException {
//        Shop shop = getById(id);
//
//        RedisData redisData = new RedisData();
//        Thread.sleep(200);
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSecond));
//        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(redisData));
//    }
}
