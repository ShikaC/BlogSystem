package com.blogs.dto;

import com.blogs.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公开的用户信息VO（用于显示在用户主页）
 */
@Data
public class PublicUserVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String bio;
    private String github;
    private String zhihu;
    private String weixin;
    private LocalDateTime createdAt;

    // 隐私设置
    private Boolean likesPublic;
    private Boolean favoritesPublic;

    public static PublicUserVO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        PublicUserVO vo = new PublicUserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setGithub(user.getGithub());
        vo.setZhihu(user.getZhihu());
        vo.setWeixin(user.getWeixin());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setLikesPublic(user.getLikesPublic() != null ? user.getLikesPublic() : true);
        vo.setFavoritesPublic(user.getFavoritesPublic() != null ? user.getFavoritesPublic() : true);

        return vo;
    }
}
