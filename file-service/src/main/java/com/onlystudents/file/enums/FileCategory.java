package com.onlystudents.file.enums;

/**
 * 文件分类枚举
 * 用于确定文件的存储路径和访问权限
 */
public enum FileCategory {
    /**
     * 用户头像 - 公开可读
     * 存储路径: avatars/{userId}/
     */
    AVATARS,
    
    /**
     * 公开资源 - 公开可读
     * 存储路径: public/
     * 例如：笔记封面、公开附件
     */
    PUBLIC,
    
    /**
     * 私有资源 - 需要认证
     * 存储路径: private/{userId}/
     * 例如：个人笔记原稿
     */
    PRIVATE,
    
    /**
     * 付费资源 - 需要订阅或购买
     * 存储路径: paid/{creatorId}/
     * 例如：付费笔记、付费课程
     */
    PAID
}
