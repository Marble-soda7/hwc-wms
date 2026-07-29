package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.entity.Customer;
import com.hwc.wms.modules.business.mapper.CustomerMapper;
import com.hwc.wms.modules.business.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 客户 Service 实现
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Resource
    private CustomerMapper customerMapper;

    @Override
    public Page<Customer> pageCustomers(Page<Customer> page, String keyword) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Customer::getName, keyword)
                    .or()
                    .like(Customer::getCode, keyword));
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        return customerMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void saveCustomer(Customer customer) {
        // 自动生成客户编码: KH00001, KH00002 ...
        customer.setCode(getNextCode());
        customer.setStatus(customer.getStatus() != null ? customer.getStatus() : 1);
        customerMapper.insert(customer);
    }

    @Override
    public String getNextCode() {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Customer::getCode, "KH");
        wrapper.orderByDesc(Customer::getCode);
        wrapper.last("LIMIT 1");
        Customer lastCustomer = customerMapper.selectOne(wrapper);

        if (lastCustomer == null || lastCustomer.getCode() == null) {
            return "KH00001";
        }
        try {
            int nextNum = Integer.parseInt(lastCustomer.getCode().substring(2)) + 1;
            return "KH" + String.format("%05d", nextNum);
        } catch (NumberFormatException e) {
            throw new BusinessException("客户编码格式异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public void updateCustomer(Customer customer) {
        Customer exist = customerMapper.selectById(customer.getId());
        if (exist == null) {
            throw new BusinessException("客户不存在");
        }
        // 检查编码是否与其他客户重复
        Long count = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getCode, customer.getCode())
                        .ne(Customer::getId, customer.getId()));
        if (count > 0) {
            throw new BusinessException("客户编码已被其他客户使用");
        }
        customerMapper.updateById(customer);
    }
}
