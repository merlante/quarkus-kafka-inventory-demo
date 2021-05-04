package org.acme.beans;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;

@RegisterForReflection
public class Order {
    private String orderCode;
    private OrderEntry[] orderEntries;

    public Order() {

    }

    public Order(String orderCode, OrderEntry[] orderEntries) {
        this.orderCode = orderCode;
        this.orderEntries = orderEntries;
    }

    public String getOrderCode() {
        return orderCode;
    }
    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }
    public OrderEntry[] getOrderEntries() {
        return orderEntries;
    }
    public void setOrderEntries(OrderEntry[] orderEntries) {
        this.orderEntries = orderEntries;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderCode == null) ? 0 : orderCode.hashCode());
        result = prime * result + Arrays.hashCode(orderEntries);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Order other = (Order) obj;
        if (orderCode == null) {
            if (other.orderCode != null)
                return false;
        } else if (!orderCode.equals(other.orderCode))
            return false;
        if (!Arrays.equals(orderEntries, other.orderEntries))
            return false;
        return true;
    }

    
}
