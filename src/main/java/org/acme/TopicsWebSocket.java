package org.acme;

import io.smallrye.reactive.messaging.kafka.Record;
import org.acme.beans.Order;
import org.acme.beans.Product;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.json.bind.JsonbBuilder;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ServerEndpoint("/topic/messages")
@ApplicationScoped
public class TopicsWebSocket {

    private List<Session> sessions = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @Incoming("orders_broadcast")
    void ordersToSocket(Order order){
        final String orderText = "orders:" + JsonbBuilder.create().toJson(order);

        textToSocket(orderText);
    }

    @Incoming("shipments_broadcast")
    void shipmentsToSocket(Order shipment){
        final String shipmentText = "shipments:" + JsonbBuilder.create().toJson(shipment);

        textToSocket(shipmentText);
    }

    @Incoming("reserved-stock_broadcast")
    void reservedStockToSocket(Record<Product, Integer> record){
        final String stockText = "reserved-stock:" +
                record.key().getProductSku() + ":" +
                Integer.valueOf(record.value());

        textToSocket(stockText);
    }

    @Incoming("stock-levels_broadcast")
    void stockLevelsToSocket(Record<Product, Integer> record){
        final String stockText = "stock-levels:" +
                record.key().getProductSku() + ":" +
                Integer.valueOf(record.value());

        textToSocket(stockText);
    }

    @Incoming("available-stock_broadcast")
    void availableStockToSocket(Record<Product, Integer> record){
        final String stockText = "available-stock:" +
                record.key().getProductSku() + ":" +
                Integer.valueOf(record.value());

        textToSocket(stockText);
    }

    void textToSocket(String text) {
        sessions.forEach(s -> s.getAsyncRemote().sendObject(text, result ->  {
            if (result.getException() != null) {
                System.out.println("Unable to send message: " + result.getException());
            }
        }));
    }

}
