package it.adesso.awesomepizza.utilities.logging;

import it.adesso.awesomepizza.application.OrderStatus;
import org.slf4j.Logger;

public final class OrderLogUtils {

    private OrderLogUtils() {
    }

    public static void logOrderCreated(Logger logger, String orderCode, int pizzasCount) {
        LogContextUtils.putOrderCode(orderCode);
        logger.info("event=order.created orderCode={} pizzasCount={}", orderCode, pizzasCount);
    }

    public static void logStatusTransition(Logger logger, Long orderId, OrderStatus from, OrderStatus to) {
        logger.info("event=order.status.transition orderId={} fromStatus={} toStatus={}", orderId, from, to);
    }
}
