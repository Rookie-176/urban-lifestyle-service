package com.roger.urbanlifestyle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roger.urbanlifestyle.dto.Result;
import com.roger.urbanlifestyle.entity.ShopType;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IShopTypeService extends IService<ShopType> {

    Result queryTypeList();
}
