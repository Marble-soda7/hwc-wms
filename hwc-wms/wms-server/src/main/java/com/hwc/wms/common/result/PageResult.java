package com.hwc.wms.common.result;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {

    private List<T> records;
    private long total;
    private long page;
    private long pageSize;

    private PageResult() {
    }

    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records != null ? records : Collections.emptyList());
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        return result;
    }

    public static <T> PageResult<T> empty(long page, long pageSize) {
        return of(Collections.emptyList(), 0, page, pageSize);
    }
}
