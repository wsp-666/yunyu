package com.yunyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.dao.ProductDao;
import com.yunyu.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductDao productDao;

    @GetMapping("/list")
    public Result<PageResult<Product>> getProductList(
            @RequestParam(required = false) Integer category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> p = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);
        if (category != null) {
            wrapper.eq(Product::getCategory, category);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return Result.success(PageResult.from(productDao.selectPage(p, wrapper)));
    }

    @GetMapping("/detail/{productId}")
    public Result<Product> getProductDetail(@PathVariable int productId) {
        return Result.success(productDao.selectById(productId));
    }
}
