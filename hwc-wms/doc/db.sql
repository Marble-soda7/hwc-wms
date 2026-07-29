-- ==========================================
-- 海外仓管理系统 (HWC-WMS) 数据库初始化脚本
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hwc_wms
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE hwc_wms;

-- ==========================================
-- 基础数据模块
-- ==========================================

-- 1. 仓库表
DROP TABLE IF EXISTS `warehouse`;
CREATE TABLE `warehouse` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '仓库名称',
    `code` VARCHAR(50) NOT NULL COMMENT '仓库编码',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '仓库地址',
    `contact` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 2. 客户表（货主）
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '客户名称',
    `code` VARCHAR(50) NOT NULL COMMENT '客户编码',
    `contact` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表（货主）';

-- 3. 商品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID, 0表示一级分类',
    `level` TINYINT NOT NULL DEFAULT 1 COMMENT '层级: 1-一级 2-二级',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 4. 商品表（SKU）
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sku_code` VARCHAR(100) NOT NULL COMMENT 'SKU编码',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户（货主）ID',
    `unit` VARCHAR(20) DEFAULT '个' COMMENT '计量单位',
    `weight` DECIMAL(10,3) DEFAULT NULL COMMENT '重量(kg)',
    `length` DECIMAL(10,2) DEFAULT NULL COMMENT '长(cm)',
    `width` DECIMAL(10,2) DEFAULT NULL COMMENT '宽(cm)',
    `height` DECIMAL(10,2) DEFAULT NULL COMMENT '高(cm)',
    `unit_price` DECIMAL(12,2) DEFAULT NULL COMMENT '申报单价(元)',
    `barcode` VARCHAR(100) DEFAULT NULL COMMENT '条码',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片地址',
    `safety_stock` INT NOT NULL DEFAULT 0 COMMENT '安全库存阈值',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表（SKU）';

-- 5. 库位表
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `code` VARCHAR(50) NOT NULL COMMENT '库位编码',
    `zone` VARCHAR(50) DEFAULT NULL COMMENT '区域',
    `aisle` VARCHAR(50) DEFAULT NULL COMMENT '货道',
    `shelf` VARCHAR(50) DEFAULT NULL COMMENT '货架',
    `level` VARCHAR(50) DEFAULT NULL COMMENT '层',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-空闲 2-占用 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code` (`warehouse_id`, `code`),
    KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位表';

-- ==========================================
-- 入库模块
-- ==========================================

-- 6. 入库单主表
DROP TABLE IF EXISTS `inbound_order`;
CREATE TABLE `inbound_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
    `customer_id` BIGINT NOT NULL COMMENT '客户（货主）ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `order_type` VARCHAR(20) NOT NULL DEFAULT 'PURCHASE' COMMENT '入库类型: PURCHASE-采购入库 RETURN-退货入库 TRANSFER-调拨入库',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待收货 RECEIVED-已收货 COMPLETED-已完成 CANCELLED-已取消',
    `expect_arrive_time` DATETIME DEFAULT NULL COMMENT '预计到达时间',
    `actual_arrive_time` DATETIME DEFAULT NULL COMMENT '实际到达时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_user` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单主表';

-- 7. 入库单明细表
DROP TABLE IF EXISTS `inbound_order_item`;
CREATE TABLE `inbound_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `inbound_order_id` BIGINT NOT NULL COMMENT '入库单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `expect_quantity` INT NOT NULL DEFAULT 0 COMMENT '预计数量',
    `actual_quantity` INT NOT NULL DEFAULT 0 COMMENT '实际到货数量',
    `location_id` BIGINT DEFAULT NULL COMMENT '上架库位ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_inbound_order_id` (`inbound_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

-- ==========================================
-- 出库模块
-- ==========================================

-- 8. 出库单主表
DROP TABLE IF EXISTS `outbound_order`;
CREATE TABLE `outbound_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
    `customer_id` BIGINT NOT NULL COMMENT '客户（货主）ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `order_type` VARCHAR(20) NOT NULL DEFAULT 'SALE' COMMENT '出库类型: SALE-销售出库 TRANSFER-调拨出库 RETURN-退货出库',
    `status` VARCHAR(20) NOT NULL DEFAULT 'WAIT_PICK' COMMENT '状态: WAIT_PICK-待拣货 PICKING-拣货中 PICKED-已拣货 SHIPPED-已发货 CANCELLED-已取消',
    `receiver_name` VARCHAR(50) DEFAULT NULL COMMENT '收件人姓名',
    `receiver_phone` VARCHAR(20) DEFAULT NULL COMMENT '收件人电话',
    `receiver_address` VARCHAR(500) DEFAULT NULL COMMENT '收件人地址',
    `express_company` VARCHAR(50) DEFAULT NULL COMMENT '快递公司',
    `express_no` VARCHAR(100) DEFAULT NULL COMMENT '快递单号',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_user` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单主表';

-- 9. 出库单明细表
DROP TABLE IF EXISTS `outbound_order_item`;
CREATE TABLE `outbound_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `outbound_order_id` BIGINT NOT NULL COMMENT '出库单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '出库数量',
    `picked_quantity` INT NOT NULL DEFAULT 0 COMMENT '实际拣货数量',
    `location_id` BIGINT DEFAULT NULL COMMENT '拣货库位ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_outbound_order_id` (`outbound_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- ==========================================
-- 库存模块
-- ==========================================

-- 10. 库存表
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户（货主）ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `location_id` BIGINT DEFAULT NULL COMMENT '库位ID',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '库存总量',
    `available_quantity` INT NOT NULL DEFAULT 0 COMMENT '可用库存',
    `locked_quantity` INT NOT NULL DEFAULT 0 COMMENT '锁定库存（已下单未发货）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_warehouse_id` (`warehouse_id`),
    UNIQUE KEY `uk_product_location_batch` (`product_id`, `customer_id`, `warehouse_id`, `location_id`, `batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 11. 库存流水表
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
    `change_type` VARCHAR(20) NOT NULL COMMENT '变动类型: INBOUND-入库 OUTBOUND-出库 ADJUST-盘点调整 LOCK-锁定 UNLOCK-解锁',
    `change_quantity` INT NOT NULL COMMENT '变动数量（正数为增加，负数为减少）',
    `before_quantity` INT NOT NULL COMMENT '变动前数量',
    `after_quantity` INT NOT NULL COMMENT '变动后数量',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '关联单号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ==========================================
-- 系统管理模块
-- ==========================================

-- 12. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 13. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 1-已删除 0-未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 14. 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 15. 菜单权限表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID, 0表示一级菜单',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-菜单 2-按钮',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `permission` VARCHAR(200) DEFAULT NULL COMMENT '权限标识',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见: 1-可见 0-隐藏',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 16. 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 默认管理员用户 (密码: admin123)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EhsM8lE2tV3Fkp7wRpCOfm', '超级管理员', 1);

-- 默认角色
INSERT INTO `sys_role` (`name`, `code`, `description`, `status`) VALUES
('超级管理员', 'ADMIN', '拥有所有权限', 1),
('仓库管理员', 'WAREHOUSE_ADMIN', '管理仓库日常操作', 1),
('操作员', 'OPERATOR', '执行入库/出库操作', 1);

-- 分配管理员角色给admin用户
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 默认菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `type`, `sort`, `permission`, `visible`) VALUES
(1, 0, '首页', '/dashboard', 'dashboard/index', 'DataBoard', 1, 1, NULL, 1),
(2, 0, '基础数据', '', NULL, 'Setting', 1, 2, NULL, 1),
(3, 2, '仓库管理', '/warehouse', 'warehouse/index', 'Odometer', 1, 1, 'warehouse:list', 1),
(4, 2, '客户管理', '/customer', 'customer/index', 'User', 1, 2, 'customer:list', 1),
(5, 2, '商品分类', '/category', 'category/index', 'Menu', 1, 3, 'category:list', 1),
(6, 2, '商品管理', '/product', 'product/index', 'Goods', 1, 4, 'product:list', 1),
(7, 2, '库位管理', '/location', 'location/index', 'MapLocation', 1, 5, 'location:list', 1),
(8, 0, '入库管理', '/inbound', 'inbound/index', 'Download', 1, 3, 'inbound:list', 1),
(9, 0, '出库管理', '/outbound', 'outbound/index', 'Upload', 1, 4, 'outbound:list', 1),
(10, 0, '库存管理', '/inventory', 'inventory/index', 'Box', 1, 5, 'inventory:list', 1),
(11, 0, '系统管理', '', NULL, 'Setting', 1, 6, NULL, 1),
(12, 11, '用户管理', '/system/user', 'system/user/index', 'UserFilled', 1, 1, 'sys:user:list', 1),
(13, 11, '角色管理', '/system/role', 'system/role/index', 'Avatar', 1, 2, 'sys:role:list', 1),
(14, 11, '菜单管理', '/system/menu', 'system/menu/index', 'Menu', 1, 3, 'sys:menu:list', 1);
