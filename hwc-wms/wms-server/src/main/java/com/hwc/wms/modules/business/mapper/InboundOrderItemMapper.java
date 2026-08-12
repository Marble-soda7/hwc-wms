package com.hwc.wms.modules.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hwc.wms.modules.business.entity.InboundOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库单明细 Mapper
 */
@Mapper
public interface InboundOrderItemMapper extends BaseMapper<InboundOrderItem> {
}
