package com.hwc.wms.modules.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.modules.business.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存 Mapper
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 库存分页查询（关联商品表，支持商品关键字搜索与低库存预警筛选）
     */
    @Select("<script>" +
            "SELECT i.* FROM inventory i" +
            " LEFT JOIN product p ON i.product_id = p.id" +
            "<where>" +
            "  <if test='warehouseId != null'> AND i.warehouse_id = #{warehouseId}</if>" +
            "  <if test='customerId != null'> AND i.customer_id = #{customerId}</if>" +
            "  <if test='locationId != null'> AND i.location_id = #{locationId}</if>" +
            "  <if test='batchNo != null and batchNo != \"\"'> AND i.batch_no LIKE CONCAT('%', #{batchNo}, '%')</if>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (p.name LIKE CONCAT('%', #{keyword}, '%')" +
            "      OR p.sku_code LIKE CONCAT('%', #{keyword}, '%')" +
            "      OR p.barcode LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "  <if test='warnOnly != null and warnOnly'>" +
            "    AND p.safety_stock IS NOT NULL AND i.quantity &lt; p.safety_stock</if>" +
            "</where>" +
            " ORDER BY i.update_time DESC, i.id DESC" +
            "</script>")
    Page<Inventory> selectInventoryPage(Page<Inventory> page,
                                         @Param("warehouseId") Long warehouseId,
                                         @Param("customerId") Long customerId,
                                         @Param("locationId") Long locationId,
                                         @Param("batchNo") String batchNo,
                                         @Param("keyword") String keyword,
                                         @Param("warnOnly") Boolean warnOnly);
}
