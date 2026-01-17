package com.roger.urbanlifestyle;

import com.roger.urbanlifestyle.entity.Shop;
import com.roger.urbanlifestyle.service.impl.ShopServiceImpl;
import com.roger.urbanlifestyle.utils.CacheClient;
import com.roger.urbanlifestyle.utils.RedisConstants;
import com.roger.urbanlifestyle.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class UlsApplicationTest {

    @Resource
    private CacheClient cacheClient;
    @Resource
    private ShopServiceImpl shopService;
    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    void testSaveShop () throws InterruptedException {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY+1L, shop, 10L, TimeUnit.SECONDS);
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(500);
    @Test
    void testRedisId () throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(300);

        Runnable runnable = () -> {
            for (int i = 0; i < 100; i++) {
                long order = redisIdWorker.nextId("order");
                System.out.println(order);
            }
            countDownLatch.countDown();
        };

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            executorService.execute(runnable);
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        System.out.println("time=" + (end - begin));
    }

}
