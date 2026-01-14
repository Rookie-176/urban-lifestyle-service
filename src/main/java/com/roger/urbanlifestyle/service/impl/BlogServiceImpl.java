package com.roger.urbanlifestyle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roger.urbanlifestyle.entity.Blog;
import com.roger.urbanlifestyle.mapper.BlogMapper;
import com.roger.urbanlifestyle.service.IBlogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * 
 * 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
