package org.acme.services;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Product;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;

@ApplicationScoped
public class StockLevelsMaker {
    private Random random = new Random(3);

    @Outgoing("stock-levels-out")
    public Multi<Record<Product, Integer>> generate() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
                .onOverflow().drop()
                .map(tick -> {
                    Product product = new Product("SKU-" + random.nextInt(10));
                    Integer stockLevel = 10;// + random.nextInt(4);

                    return Record.of(product, stockLevel);
                });
    }
}
