package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.entity.Product;
import com.hwc.wms.modules.business.mapper.ProductMapper;
import com.hwc.wms.modules.business.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 商品 Service 实现
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public Page<Product> pageProducts(Page<Product> page, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Product::getName, keyword)
                    .or()
                    .like(Product::getSkuCode, keyword)
                    .or()
                    .like(Product::getBarcode, keyword));
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void saveProduct(Product product) {
        // 自动生成SKU编码: SP00001, SP00002 ...
        product.setSkuCode(getNextSkuCode());
        product.setStatus(product.getStatus() != null ? product.getStatus() : 1);
        product.setUnit(product.getUnit() != null ? product.getUnit() : "个");
        product.setSafetyStock(product.getSafetyStock() != null ? product.getSafetyStock() : 0);
        productMapper.insert(product);
    }

    @Override
    public String getNextSkuCode() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Product::getSkuCode, "SP");
        wrapper.orderByDesc(Product::getSkuCode);
        wrapper.last("LIMIT 1");
        Product lastProduct = productMapper.selectOne(wrapper);

        if (lastProduct == null || lastProduct.getSkuCode() == null) {
            return "SP00001";
        }
        try {
            int nextNum = Integer.parseInt(lastProduct.getSkuCode().substring(2)) + 1;
            return "SP" + String.format("%05d", nextNum);
        } catch (NumberFormatException e) {
            throw new BusinessException("SKU编码格式异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public void updateProduct(Product product) {
        Product exist = productMapper.selectById(product.getId());
        if (exist == null) {
            throw new BusinessException("商品不存在");
        }
        // 检查SKU编码是否与其他商品重复
        Long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getSkuCode, product.getSkuCode())
                        .ne(Product::getId, product.getId()));
        if (count > 0) {
            throw new BusinessException("SKU编码已被其他商品使用");
        }
        productMapper.updateById(product);
    }
}
