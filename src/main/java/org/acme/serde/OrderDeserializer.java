package org.acme.serde;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.acme.beans.Order;

@RegisterForReflection
public class OrderDeserializer extends JsonbDeserializer<Order> {
    public OrderDeserializer() {
        super(Order.class);
    }
}
