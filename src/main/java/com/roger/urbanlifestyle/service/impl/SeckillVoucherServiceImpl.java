package com.roger.urbanlifestyle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.entity.SeckillVoucher;
import com.roger.urbanlifestyle.mapper.SeckillVoucherMapper;
import com.roger.urbanlifestyle.service.ISeckillVoucherService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * 
 * 2022-01-04
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
