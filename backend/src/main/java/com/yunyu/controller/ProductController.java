package com.yunyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yunyu.common.CacheKeys;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.dao.ProductDao;
import com.yunyu.entity.Product;
import com.yunyu.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private RedisCacheService redisCacheService;

    @GetMapping("/list")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(required = false) Integer category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String key = CacheKeys.productList(category, page, size);
        PageResult<Product> result = redisCacheService.getOrLoad(
                key,
                Duration.ofMinutes(3),
                new TypeReference<PageResult<Product>>() {},
                () -> {
                    Page<Product> p = new Page<>(page, size);
                    LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                            .eq(Product::getStatus, 1);
                    if (category != null) {
                        wrapper.eq(Product::getCategory, category);
                    }
                    wrapper.orderByDesc(Product::getCreateTime);
                    return PageResult.from(productDao.selectPage(p, wrapper));
                }
        );
        return Result.success(result);
    }

    @GetMapping("/detail/{productId}")
    public Result<Product> getProductDetail(@PathVariable int productId) {
        Product product = redisCacheService.getOrLoad(
                CacheKeys.productDetail(productId),
                Duration.ofMinutes(5),
                Product.class,
                () -> productDao.selectById(productId)
        );
        return Result.success(product);
    }
}
