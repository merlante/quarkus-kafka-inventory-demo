package org.acme.services;

import java.time.Duration;
import java.util.Random;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.acme.beans.Order;
import org.acme.beans.OrderEntry;
import org.acme.beans.Product;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;

@ApplicationScoped
public class OrderMaker {
    private Random random = new Random();

    @Outgoing("new-orders")
    @Broadcast
    public Multi<Record<String, Order>> generate() {                  
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onOverflow().drop()
                .map(tick -> {
                    if(tick.shortValue() == 0) {
                        // Send a dummy order on tick 0. This avoids a bug where the first broadcast order is lost
                        // to the slower of the 2 consumers to start up, resulting in lost stock reservations and an
                        // incorrect stock count. Dummy order "wakes up" both consumers of new-orders, but makes no
                        // reservations because the order is empty.

                        return Record.of("ORDER-0", new Order("ORDER-0", new OrderEntry[]{}));
                    } else {
                        int numEntries = random.nextInt(4) + 1;
                        OrderEntry[] entries = IntStream.range(0, numEntries)
                                .mapToObj(i -> new OrderEntry(
                                        new Product("SKU-" + random.nextInt(10)),
                                        random.nextInt(3) + 1))
                                .toArray(OrderEntry[]::new);

                        Order order = new Order("ORDER-" + tick.shortValue(), entries);

                        return Record.of(order.getOrderCode(), order);
                    }

                });
    }

    @Incoming("new-orders")
    @Outgoing("orders-out")
    public Multi<Record<String, Order>> generate(Multi<Record<String, Order>> orders) {
        return orders;
    }

}
