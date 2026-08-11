package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.entity.Product;

/**
 * 商品 Service
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品（支持按名称、SKU编码或条码搜索）
     */
    Page<Product> pageProducts(Page<Product> page, String keyword);

    /**
     * 新增商品（自动生成SKU编码）
     */
    void saveProduct(Product product);

    /**
     * 修改商品
     */
    void updateProduct(Product product);

    /**
     * 获取下一个SKU编码
     */
    String getNextSkuCode();
}
