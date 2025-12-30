package com.blogs.service;

import com.blogs.entity.FriendLink;
import com.blogs.exception.BusinessException;
import com.blogs.repository.FriendLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 友链服务
 */
@Service
@Transactional
public class FriendLinkService {
    
    @Autowired
    private FriendLinkRepository friendLinkRepository;
    
    /**
     * 创建友链
     */
    public FriendLink createFriendLink(String name, String url, String logo, String description) {
        FriendLink link = new FriendLink();
        link.setName(name);
        link.setUrl(url);
        link.setLogo(logo);
        link.setDescription(description);
        return friendLinkRepository.save(link);
    }
    
    /**
     * 更新友链
     */
    public FriendLink updateFriendLink(Long id, String name, String url, String logo, 
                                        String description, Integer sortOrder, Boolean isVisible) {
        FriendLink link = friendLinkRepository.findById(id)
                .orElseThrow(() -> new BusinessException("友链不存在"));
        
        link.setName(name);
        link.setUrl(url);
        link.setLogo(logo);
        link.setDescription(description);
        if (sortOrder != null) {
            link.setSortOrder(sortOrder);
        }
        if (isVisible != null) {
            link.setIsVisible(isVisible);
        }
        
        return friendLinkRepository.save(link);
    }
    
    /**
     * 删除友链
     */
    public void deleteFriendLink(Long id) {
        friendLinkRepository.deleteById(id);
    }
    
    /**
     * 获取所有友链（后台）
     */
    public List<FriendLink> getAllFriendLinks() {
        return friendLinkRepository.findAllByOrderBySortOrderAsc();
    }
    
    /**
     * 获取可见友链（前台）
     */
    public List<FriendLink> getVisibleFriendLinks() {
        return friendLinkRepository.findByIsVisibleTrueOrderBySortOrderAsc();
    }
}
