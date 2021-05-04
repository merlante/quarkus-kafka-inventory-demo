package org.acme.serde;

import io.quarkus.reactivemessaging.http.runtime.serializers.Serializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.acme.beans.Order;

//@RegisterForReflection
public class OrderReactiveSerializer implements Serializer<Order> {

    @Override
    public boolean handles(Object order) {
        return order instanceof Order;
    }

    @Override
    public Buffer serialize(Order order) {
        return JsonObject.mapFrom(order).toBuffer();
    }
}
