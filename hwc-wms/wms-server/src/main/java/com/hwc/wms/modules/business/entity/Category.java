package com.hwc.wms.modules.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hwc.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    /** 分类名称 */
    private String name;

    /** 父分类ID, 0表示一级分类 */
    private Long parentId;

    /** 层级: 1-一级 2-二级 */
    private Integer level;

    /** 排序号 */
    private Integer sort;
}
