package com.onlystudents.common.constants;

public final class CommonConstants {

    private CommonConstants() {
    }

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ADMIN_ID_HEADER = "X-Admin-Id";
    public static final int MaxTextMessageBufferSize = 10 * 1024 * 1024;
    public static final int MaxBinaryMessageBufferSize =10 * 1024 * 1024;
    public static final long MaxSessionIdleTimeout=30 * 60 * 1000L;
}
