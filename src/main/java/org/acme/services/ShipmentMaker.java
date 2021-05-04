package org.acme.services;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Order;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ShipmentMaker {

    @Incoming("orders")
    @Outgoing("shipments-out")
    public Multi<Record<String, Order>> generate(Multi<Record<String, Order>> orders) {
        return orders;
    }
}
