package com.hwc.wms.modules.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hwc.wms.modules.business.entity.OutboundOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单明细 Mapper
 */
@Mapper
public interface OutboundOrderItemMapper extends BaseMapper<OutboundOrderItem> {
}
