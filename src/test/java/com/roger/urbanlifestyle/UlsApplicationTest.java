package com.roger.urbanlifestyle;

import com.roger.urbanlifestyle.entity.Shop;
import com.roger.urbanlifestyle.service.impl.ShopServiceImpl;
import com.roger.urbanlifestyle.utils.CacheClient;
import com.roger.urbanlifestyle.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class UlsApplicationTest {

    @Resource
    private CacheClient cacheClient;

    @Resource
    private ShopServiceImpl shopService;

    @Test
    void testSaveShop () throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY+1L, shop, 10L, TimeUnit.SECONDS);
    }

}
