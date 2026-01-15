package com.roger.urbanlifestyle.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.entity.ShopType;
import com.roger.urbanlifestyle.mapper.ShopTypeMapper;
import com.roger.urbanlifestyle.service.IShopTypeService;
import com.roger.urbanlifestyle.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        List<String> jsonTypeList = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);

        List<ShopType> typeList = null;
        if (jsonTypeList != null && !jsonTypeList.isEmpty()) {
            typeList = jsonTypeList.stream()
                    .map(jsonString -> JSONUtil.toBean(jsonString, ShopType.class))
                    .collect(Collectors.toList());
        } else {
            typeList = query().orderByAsc("sort").list();
            if (typeList == null || typeList.isEmpty()) {
                return Result.fail("Shop type not found");
            }

            stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, typeList.stream()
                    .map(JSONUtil::toJsonStr)
                    .collect(Collectors.toList()));
            stringRedisTemplate.expire(RedisConstants.CACHE_SHOP_TYPE_KEY,
                    RedisConstants.CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        }

        return Result.ok(typeList);
    }
}
