package com.blogs.common;

/**
 * 通知类型常量（全站统一：博客 + 论坛）
 */
public final class NotificationTypes {

    private NotificationTypes() {}

    // 博客
    public static final String ARTICLE_LIKE = "ARTICLE_LIKE";
    public static final String ARTICLE_COMMENT = "ARTICLE_COMMENT";
    public static final String ARTICLE_COMMENT_REPLY = "ARTICLE_COMMENT_REPLY";

    // 论坛
    public static final String FORUM_POST_LIKE = "FORUM_POST_LIKE";
    public static final String FORUM_POST_COMMENT = "FORUM_POST_COMMENT";
    public static final String FORUM_POST_COMMENT_REPLY = "FORUM_POST_COMMENT_REPLY";
}


