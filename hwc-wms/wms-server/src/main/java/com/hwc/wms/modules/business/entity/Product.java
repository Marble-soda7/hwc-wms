package com.hwc.wms.modules.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hwc.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品（SKU）实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    /** SKU编码 */
    private String skuCode;

    /** 商品名称 */
    private String name;

    /** 分类ID */
    private Long categoryId;

    /** 客户（货主）ID */
    private Long customerId;

    /** 计量单位 */
    private String unit;

    /** 重量(kg) */
    private BigDecimal weight;

    /** 长(cm) */
    private BigDecimal length;

    /** 宽(cm) */
    private BigDecimal width;

    /** 高(cm) */
    private BigDecimal height;

    /** 申报单价(元) */
    private BigDecimal unitPrice;

    /** 条码 */
    private String barcode;

    /** 图片地址 */
    private String imageUrl;

    /** 安全库存阈值 */
    private Integer safetyStock;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;
}
