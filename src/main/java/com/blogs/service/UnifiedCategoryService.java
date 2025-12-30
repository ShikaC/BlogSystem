package com.blogs.service;

import com.blogs.dto.UnifiedCategoryVO;
import com.blogs.entity.Category;
import com.blogs.entity.ForumSection;
import com.blogs.repository.CategoryRepository;
import com.blogs.repository.ForumSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 全站统一分类/版块服务（低侵入式聚合，不破坏原有 Category/ForumSection 业务）
 */
@Service
public class UnifiedCategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ForumSectionRepository forumSectionRepository;

    /**
     * 获取全站导航分类（博客分类 + 论坛版块）合并输出
     *
     * @param type 可选：CATEGORY / SECTION；为空则返回全部
     */
    public List<UnifiedCategoryVO> getAll(String type) {
        List<UnifiedCategoryVO> result = new ArrayList<>();

        if (type == null || type.isBlank() || "CATEGORY".equalsIgnoreCase(type)) {
            List<Category> categories = categoryRepository.findAll();
            for (Category c : categories) {
                // 兼容历史：type 为空视为 CATEGORY
                if (c.getType() != null && !c.getType().isBlank() && !"CATEGORY".equalsIgnoreCase(c.getType())) {
                    continue;
                }
                UnifiedCategoryVO vo = new UnifiedCategoryVO();
                vo.setId(c.getId());
                vo.setName(c.getName());
                vo.setDescription(c.getDescription());
                vo.setParentId(c.getParentId());
                vo.setSortOrder(c.getSortOrder());
                vo.setType("CATEGORY");
                vo.setStatus(1);
                vo.setCreatedAt(c.getCreatedAt());
                result.add(vo);
            }
        }

        if (type == null || type.isBlank() || "SECTION".equalsIgnoreCase(type)) {
            List<ForumSection> sections = forumSectionRepository.findAllByOrderBySortOrderAsc();
            for (ForumSection s : sections) {
                UnifiedCategoryVO vo = new UnifiedCategoryVO();
                vo.setId(s.getId());
                vo.setName(s.getName());
                vo.setDescription(s.getDescription());
                vo.setParentId(s.getParentId());
                vo.setSortOrder(s.getSortOrder());
                vo.setType("SECTION");
                vo.setStatus(s.getStatus());
                vo.setIcon(s.getIcon());
                vo.setCreatedAt(s.getCreatedAt());
                result.add(vo);
            }
        }

        result.sort(Comparator
                .comparing(UnifiedCategoryVO::getType)
                .thenComparing(vo -> vo.getSortOrder() != null ? vo.getSortOrder() : 0)
                .thenComparing(UnifiedCategoryVO::getId));
        return result;
    }
}


