package com.yunyu.common;

/**
 * Redis Key 约定，便于多实例共享与统一失效。
 */
public final class CacheKeys {

    private CacheKeys() {}

    public static final String PREFIX = "yunyu:";

    public static String venueList(int page, int size, String keyword) {
        String kw = keyword == null || keyword.isBlank() ? "_" : keyword.trim();
        return PREFIX + "venue:list:" + page + ":" + size + ":" + kw;
    }

    public static String venueDetail(int venueId) {
        return PREFIX + "venue:detail:" + venueId;
    }

    public static final String VENUE_PATTERN = PREFIX + "venue:*";

    public static String spotList(int page, int size, String keyword) {
        String kw = keyword == null || keyword.isBlank() ? "_" : keyword.trim();
        return PREFIX + "spot:list:" + page + ":" + size + ":" + kw;
    }

    public static String spotDetail(int id) {
        return PREFIX + "spot:detail:" + id;
    }

    public static final String SPOT_PATTERN = PREFIX + "spot:*";

    public static String productList(Integer category, int page, int size) {
        return PREFIX + "product:list:" + (category == null ? "all" : category) + ":" + page + ":" + size;
    }

    public static String productDetail(int id) {
        return PREFIX + "product:detail:" + id;
    }

    public static final String PRODUCT_PATTERN = PREFIX + "product:*";

    public static String weather(String city) {
        return PREFIX + "weather:" + (city == null ? "" : city.trim());
    }

    public static String lockSessionOrder(int sessionId) {
        return PREFIX + "lock:session:order:" + sessionId;
    }

    public static String lockSessionSeat(int sessionId) {
        return PREFIX + "lock:session:seat:" + sessionId;
    }

    public static String lockProductExchange(int productId) {
        return PREFIX + "lock:product:ex:" + productId;
    }

    public static String lockJob(String jobName) {
        return PREFIX + "lock:job:" + jobName;
    }

    public static String rateLimit(String api, String identity) {
        return PREFIX + "rl:" + api + ":" + identity;
    }

    public static String sessionSeats(int sessionId) {
        return PREFIX + "session:seats:" + sessionId;
    }
}
