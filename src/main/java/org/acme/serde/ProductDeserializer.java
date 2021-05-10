package org.acme.serde;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.acme.beans.Product;

@RegisterForReflection
public class ProductDeserializer extends JsonbDeserializer<Product> {
    public ProductDeserializer() {
        super(Product.class);
    }
}
