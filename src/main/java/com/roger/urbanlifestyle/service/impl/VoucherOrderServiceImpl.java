package com.roger.urbanlifestyle.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.dto.UserDTO;
import com.roger.urbanlifestyle.entity.SeckillVoucher;
import com.roger.urbanlifestyle.entity.Voucher;
import com.roger.urbanlifestyle.entity.VoucherOrder;
import com.roger.urbanlifestyle.mapper.VoucherOrderMapper;
import com.roger.urbanlifestyle.service.ISeckillVoucherService;
import com.roger.urbanlifestyle.service.IVoucherOrderService;
import com.roger.urbanlifestyle.service.IVoucherService;
import com.roger.urbanlifestyle.utils.RedisConstants;
import com.roger.urbanlifestyle.utils.RedisIdWorker;
import com.roger.urbanlifestyle.utils.SimpleRedisLock;
import com.roger.urbanlifestyle.utils.UserHolder;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result seckillVoucher(Long voucherId) {

        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

        LocalDateTime beginTime = voucher.getBeginTime();
        LocalDateTime endTime = voucher.getEndTime();
        LocalDateTime now = LocalDateTime.now();

        if (beginTime.isAfter(now)) {
            return Result.fail("the flash sale has not started yet");
        }

        if (endTime.isBefore(now)) {
            return Result.fail("the flash sale has already ended");
        }

        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        SimpleRedisLock simpleRedisLock = new SimpleRedisLock("order" + userId, stringRedisTemplate);

        boolean trylock = simpleRedisLock.trylock(1200);
        if (!trylock) {
            return Result.fail("one user can only place one order");
        }

        try {
            IVoucherOrderService proxy = (IVoucherOrderService)AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId, voucher);
        } finally {
            simpleRedisLock.unlock();
        }
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId, SeckillVoucher voucher) {
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.fail("current user has already bought one");
        }

        Integer stock = voucher.getStock();
        if (stock < 1) {
            return Result.fail("the flash sale does not have enough stock");
        }

        boolean success = seckillVoucherService.update().setSql("stock = stock-1").eq("voucher_id", voucherId).gt("stock", 0).update();
        if (!success) {
            return Result.fail("the flash sale does not have enough stock");
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setVoucherId(voucherId);
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        save(voucherOrder);

        return Result.ok(orderId + "");
    }
}
