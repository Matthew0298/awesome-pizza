package it.adesso.awesomepizza.utils.logging;

import it.adesso.awesomepizza.model.OrderStatus;
import org.slf4j.Logger;

public final class OrderLogUtils {

    private static final String UNKNOWN = "N/A";

    private OrderLogUtils() {
    }

    public static void logBusinessEvent(Logger logger, String event, Long orderId, String orderCode, OrderStatus status, String details) {
        String safeDetails = details == null ? "" : details;
        logger.info(
                "event={} orderId={} orderCode={} status={} {}",
                event,
                orderId == null ? UNKNOWN : orderId,
                isBlank(orderCode) ? UNKNOWN : orderCode,
                status == null ? UNKNOWN : status,
                safeDetails
        );
    }

    public static void logOrderCreated(Logger logger, Long orderId, String orderCode, OrderStatus status, int pizzasCount) {
        LogContextUtils.putOrderCode(orderCode);
        logBusinessEvent(logger, "order.created", orderId, orderCode, status, "pizzasCount=" + pizzasCount);
    }

    public static void logStatusTransition(Logger logger, Long orderId, String orderCode, OrderStatus from, OrderStatus to) {
        logBusinessEvent(logger, "order.status.transition", orderId, orderCode, to, "fromStatus=" + from + " toStatus=" + to);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
