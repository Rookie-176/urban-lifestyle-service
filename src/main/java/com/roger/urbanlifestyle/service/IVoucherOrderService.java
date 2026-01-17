package com.roger.urbanlifestyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.entity.SeckillVoucher;
import com.roger.urbanlifestyle.entity.VoucherOrder;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId, SeckillVoucher voucher);
}
