package com.hwc.wms.modules.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hwc.wms.modules.business.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库单 Mapper
 */
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {
}
