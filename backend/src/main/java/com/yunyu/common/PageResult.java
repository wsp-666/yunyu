package com.yunyu.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long totalCount;
    private long pageSize;
    private long currentPage;
    private long totalPages;
    private List<T> list;

    public static <T> PageResult<T> from(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotalCount(page.getTotal());
        result.setPageSize(page.getSize());
        result.setCurrentPage(page.getCurrent());
        result.setTotalPages(page.getPages());
        result.setList(page.getRecords());
        return result;
    }
}
