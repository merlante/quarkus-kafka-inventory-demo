package org.acme.services;

import java.time.Duration;
import java.util.Random;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;

import org.acme.beans.Order;
import org.acme.beans.OrderEntry;
import org.acme.beans.Product;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;

@ApplicationScoped
public class OrderMaker {
    private Random random = new Random();

    @Outgoing("orders-out")
    public Multi<Record<String, Order>> generate() {                  
        return Multi.createFrom().ticks().every(Duration.ofSeconds(2))
                .onOverflow().drop()
                .map(tick -> {
                    int numEntries = random.nextInt(4) + 1;
                    OrderEntry[] entries = IntStream.range(0, numEntries)
                        .mapToObj(i -> new OrderEntry(
                            new Product("SKU-" + random.nextInt(10)),
                            random.nextInt(3) + 1))
                        .toArray(OrderEntry[]::new);

                    Order order = new Order("ORDER-" + tick.shortValue(), entries);
                    
                    return Record.of(order.getOrderCode(), order);
                });
    }

}
