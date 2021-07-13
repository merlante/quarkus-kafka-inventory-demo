package org.acme.services;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Order;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;

@ApplicationScoped
public class ShipmentMaker {
    private Random random = new Random(55);

    @ConfigProperty(name = "inventorydemo.generated.shipments.fixed_shipping_delay.seconds", defaultValue="10")
    Integer fixedShippingDelay;

    @ConfigProperty(name = "inventorydemo.generated.shipments.variable_shipping_delay.upperbound.seconds", defaultValue="10")
    Integer variableShippingDelay;

    @Channel("shipments-out")
    Emitter<Record<String, Order>> emitter;

    @Incoming("new-orders")
    public void onNewOrders(Record<String, Order> orderRecord) {
        Uni.createFrom().item(orderRecord)
                .onItem()
                .delayIt().by(Duration.ofSeconds(fixedShippingDelay + random.nextInt(variableShippingDelay)))
                .subscribe().with(item -> emitter.send(item)); // create shipments matching orders in N seconds time.
    }

}
