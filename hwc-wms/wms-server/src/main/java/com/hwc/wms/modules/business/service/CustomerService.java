package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.entity.Customer;

/**
 * 客户 Service
 */
public interface CustomerService extends IService<Customer> {

    /**
     * 分页查询客户（支持按名称或编码搜索）
     */
    Page<Customer> pageCustomers(Page<Customer> page, String keyword);

    /**
     * 新增客户
     */
    void saveCustomer(Customer customer);

    /**
     * 修改客户
     */
    void updateCustomer(Customer customer);

    /**
     * 获取下一个客户编码
     */
    String getNextCode();
}
