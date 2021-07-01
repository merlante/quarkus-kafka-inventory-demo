package org.acme.services;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Order;
import org.acme.beans.Product;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TopicsListener {

    @Incoming("orders")
    @Broadcast(3)
    @Outgoing("orders_broadcast")
    Multi<Order> orders(Multi<Order> orders) {
        return orders;
    }

    @Incoming("shipments")
    @Broadcast(2)
    @Outgoing("shipments_broadcast")
    Multi<Order> shipments(Multi<Order> shipments) {
        return shipments;
    }

    @Incoming("reserved-stock")
    @Broadcast(2)
    @Outgoing("reserved-stock_broadcast")
    Multi<Record<Product, Integer>> reservedStock(Multi<Record<Product, Integer>> reservedStock) {
        return reservedStock;
    }

    @Incoming("stock-levels")
    @Broadcast(2)
    @Outgoing("stock-levels_broadcast")
    Multi<Record<Product, Integer>> stockLevelsToSocket(Multi<Record<Product, Integer>> stockLevels) {
        return stockLevels;
    }

    @Incoming("available-stock")
    @Broadcast(2)
    @Outgoing("available-stock_broadcast")
    Multi<Record<Product, Integer>> availableStockToSocket(Multi<Record<Product, Integer>> availableStock) {
        return availableStock;
    }
}
