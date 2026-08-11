package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.entity.Category;

import java.util.List;
import java.util.Map;

/**
 * 商品分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树（一级分类包含children二级分类）
     */
    List<Map<String, Object>> getCategoryTree();

    /**
     * 新增分类
     */
    void saveCategory(Category category);

    /**
     * 修改分类
     */
    void updateCategory(Category category);
}
