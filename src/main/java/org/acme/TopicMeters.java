package org.acme;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Order;
import org.acme.beans.Product;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;

@ApplicationScoped
public class TopicMetrics {

    @Inject
    Logger log;

    @Inject
    MeterRegistry registry;

    private Timer ordersRoundTrip;

    @PostConstruct
    void registerCustomMeters() {
        registry.timer("inventorydemo.orders.roundtrip.millis");
    }

    /* Simple counts of topic messages */

    @Incoming("orders_broadcast")
    @Counted("inventorydemo.orders.count")
    void ordersToSocket(Order order) {
    }

    @Incoming("shipments_broadcast")
    @Counted("inventorydemo.shipments.count")
    void shipmentsToSocket(Order shipment) {
    }

    @Incoming("reserved-stock_broadcast")
    @Counted("inventorydemo.reserved-stock.count")
    void reservedStockToSocket(Record<Product, Integer> record) {
    }

    @Incoming("stock-levels_broadcast")
    @Counted("inventorydemo.stock-levels.count")
    void stockLevelsToSocket(Record<Product, Integer> record) {
    }

    @Incoming("available-stock_broadcast")
    @Counted("inventorydemo.available-stock.count")
    void availableStockToSocket(Record<Product, Integer> record) {
    }

    /* Derived metrics */

    private Map<String, ZonedDateTime> newOrderTimeStamps = new HashMap<>();

    @Incoming("new-orders")
    void clockNewOrder(Record<String, Order> order) {
        newOrderTimeStamps.put(order.value().getOrderCode(), ZonedDateTime.now());
        log.log(Logger.Level.DEBUG, "new-orders: order.value().getOrderCode(): " + order.value().getOrderCode());
    }

    @Incoming("orders_broadcast")
    void clockNewOrderReceived(Order order) {
        if(newOrderTimeStamps.containsKey(order.getOrderCode())) {
            var now = ZonedDateTime.now();
            var then = newOrderTimeStamps.get(order.getOrderCode());
            newOrderTimeStamps.remove(order.getOrderCode());

            var duration = Duration.between(then, now);
            ordersRoundTrip.record(duration);

            log.log(Logger.Level.DEBUG, "orders_broadcast: duration (millis):" + duration.toMillis());
        }
        log.log(Logger.Level.DEBUG, "orders_broadcast: order.getOrderCode(): " + order.getOrderCode());
    }
}
