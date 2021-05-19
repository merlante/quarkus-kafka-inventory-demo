package org.acme.services;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Order;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;

@ApplicationScoped
public class ShipmentMaker {
    private static final int FIXED_SHIPPING_DELAY = 10; //seconds
    private static final int VARIABLE_SHIPPING_DELAY = 10; //seconds

    private Random random = new Random(55);

    @Channel("shipments-out")
    Emitter<Record<String, Order>> emitter;

    @Incoming("new-orders")
    public void onNewOrders(Record<String, Order> orderRecord) {
        Uni.createFrom().item(orderRecord)
                .onItem()
                .delayIt().by(Duration.ofSeconds(FIXED_SHIPPING_DELAY + random.nextInt(VARIABLE_SHIPPING_DELAY)))
                .subscribe().with(item -> emitter.send(item)); // create shipments matching orders in N seconds time.
    }

}
