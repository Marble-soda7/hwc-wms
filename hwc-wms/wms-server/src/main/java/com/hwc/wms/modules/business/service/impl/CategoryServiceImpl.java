package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.entity.Category;
import com.hwc.wms.modules.business.mapper.CategoryMapper;
import com.hwc.wms.modules.business.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类 Service 实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public List<Map<String, Object>> getCategoryTree() {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
        // 一级分类
        List<Category> parents = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Category parent : parents) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", parent.getId());
            node.put("name", parent.getName());
            node.put("level", parent.getLevel());
            node.put("sort", parent.getSort());
            // 子分类
            List<Category> children = all.stream()
                    .filter(c -> parent.getId().equals(c.getParentId()))
                    .collect(Collectors.toList());
            List<Map<String, Object>> childNodes = new ArrayList<>();
            for (Category child : children) {
                Map<String, Object> childNode = new HashMap<>();
                childNode.put("id", child.getId());
                childNode.put("name", child.getName());
                childNode.put("parentId", child.getParentId());
                childNode.put("level", child.getLevel());
                childNode.put("sort", child.getSort());
                childNodes.add(childNode);
            }
            node.put("children", childNodes);
            tree.add(node);
        }
        return tree;
    }

    @Override
    @Transactional
    public void saveCategory(Category category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("分类名称不能为空");
        }
        // 自动计算层级
        if (category.getParentId() != null && category.getParentId() > 0) {
            category.setLevel(2);
        } else {
            category.setLevel(1);
            category.setParentId(0L);
        }
        category.setSort(category.getSort() != null ? category.getSort() : 0);
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(Category category) {
        Category exist = categoryMapper.selectById(category.getId());
        if (exist == null) {
            throw new BusinessException("分类不存在");
        }
        if (category.getParentId() != null && category.getParentId() > 0) {
            category.setLevel(2);
        } else {
            category.setLevel(1);
            category.setParentId(0L);
        }
        categoryMapper.updateById(category);
    }
}
