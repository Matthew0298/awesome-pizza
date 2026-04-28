package it.adesso.awesomepizza.utilities.logging;

import org.slf4j.MDC;

import java.util.UUID;

public final class LogContextUtils {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String ORDER_CODE_KEY = "orderCode";

    private LogContextUtils() {
    }

    public static String resolveRequestId(String requestIdHeader) {
        if (requestIdHeader == null || requestIdHeader.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestIdHeader;
    }

    public static void putRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    public static void putOrderCode(String orderCode) {
        if (orderCode != null && !orderCode.isBlank()) {
            MDC.put(ORDER_CODE_KEY, orderCode);
        }
    }

    public static void clear() {
        MDC.clear();
    }
}
