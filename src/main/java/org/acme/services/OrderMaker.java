package org.acme.services;

import java.time.Duration;
import java.util.Random;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.acme.beans.Order;
import org.acme.beans.OrderEntry;
import org.acme.beans.Product;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;

@ApplicationScoped
public class OrderMaker {
    private Random random = new Random();

    @ConfigProperty(name = "inventorydemo.generated.orders.interval.seconds", defaultValue="5")
    Integer ordersIntervalSeconds;

    @ConfigProperty(name = "inventorydemo.generated.orders.orderentries.upperbound.number", defaultValue="5")
    Integer orderEntriesNumberUpperbound;

    @ConfigProperty(name = "inventorydemo.skus.number", defaultValue="10")
    Integer numberSkus;

    @ConfigProperty(name = "inventorydemo.generated.orders.product.quantity.upperbound.number", defaultValue="4")
    Integer productQuantityUpperbound;


    @Outgoing("new-orders")
    @Broadcast(3)
    public Multi<Record<String, Order>> generate() {                  
        return Multi.createFrom().ticks().every(Duration.ofSeconds(ordersIntervalSeconds))
                .onOverflow().drop()
                .map(tick -> {
                    int numEntries = random.nextInt(orderEntriesNumberUpperbound - 1) + 1;
                    OrderEntry[] entries = IntStream.range(0, numEntries)
                            .mapToObj(i -> new OrderEntry(
                                    new Product("SKU-" + random.nextInt(numberSkus)),
                                    random.nextInt(productQuantityUpperbound - 1) + 1))
                            .toArray(OrderEntry[]::new);

                    Order order = new Order("ORDER-" + tick.shortValue(), entries);

                    return Record.of(order.getOrderCode(), order);
                });
    }

    @Incoming("new-orders")
    @Outgoing("orders-out")
    public Multi<Record<String, Order>> generate(Multi<Record<String, Order>> orders) {
        return orders;
    }

}
