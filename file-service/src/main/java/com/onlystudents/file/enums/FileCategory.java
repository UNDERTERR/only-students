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
     * 例如：公开笔记的附件
     */
    PUBLIC,
    
    /**
     * 私有资源 - 需要认证
     * 存储路径: private/{userId}/
     * 例如：仅自己可见的笔记
     */
    PRIVATE,
    
    /**
     * 付费资源 - 需要订阅或购买
     * 存储路径: paid/{creatorId}/
     * 例如：付费笔记、订阅可见笔记
     */
    PAID;

    /**
     * 根据笔记可见性获取文件分类
     * @param visibility 笔记可见性: 0=公开, 1=订阅可见, 2=付费可见, 3=订阅+付费, 4=仅自己
     * @return 对应的FileCategory
     */
    public static FileCategory fromVisibility(Integer visibility) {
        if (visibility == null) return PRIVATE;
        switch (visibility) {
            case 0:
                return PUBLIC;
            case 1:
            case 2:
            case 3:
                return PAID;
            case 4:
                return PRIVATE;
            default:
                return PRIVATE;
        }
    }
}
