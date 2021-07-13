package org.acme.services;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Product;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;

/*
 *TODO: This class is for really just for testing purposes. In a proper demo, stock-levels actually reflect
 * stock movements in the warehouse, and therefore both incoming stock and shipments -- would be better to
 * randomly generate incoming stock and feed shipments into stock-levels than to simply emmit a fixed number.
 */
@ApplicationScoped
public class StockLevelsMaker {
    private Random random = new Random(3);

    @ConfigProperty(name = "inventorydemo.generated.stock-levels.interval.seconds", defaultValue="5")
    Integer stockLevelsIntervalSeconds;

    @ConfigProperty(name = "inventorydemo.skus.number", defaultValue="10")
    Integer numberSkus;

    @ConfigProperty(name = "inventorydemo.generated.stock-levels.fixed_level.number", defaultValue="10")
    Integer fixedStockLevelForSkus;

    @Outgoing("stock-levels-out")
    public Multi<Record<Product, Integer>> generate() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(stockLevelsIntervalSeconds))
                .onOverflow().drop()
                .map(tick -> {
                    Product product = new Product("SKU-" + random.nextInt(numberSkus));
                    Integer stockLevel = fixedStockLevelForSkus;

                    return Record.of(product, stockLevel);
                });
    }
}
