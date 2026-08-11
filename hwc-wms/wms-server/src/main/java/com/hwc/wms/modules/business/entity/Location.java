package com.hwc.wms.modules.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hwc.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库位实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("location")
public class Location extends BaseEntity {

    /** 仓库ID */
    private Long warehouseId;

    /** 库位编码 */
    private String code;

    /** 区域 */
    private String zone;

    /** 货道 */
    private String aisle;

    /** 货架 */
    private String shelf;

    /** 层 */
    private String level;

    /** 状态: 1-空闲 2-占用 0-禁用 */
    private Integer status;
}
