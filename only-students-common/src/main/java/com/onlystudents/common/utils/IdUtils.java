package com.onlystudents.common.utils;

import cn.hutool.core.util.IdUtil;

public class IdUtils {
    
    public static String randomUUID() {
        return IdUtil.randomUUID();
    }
    
    public static String simpleUUID() {
        return IdUtil.simpleUUID();
    }
    
    public static long snowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }
    
    public static String snowflakeIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }
}
