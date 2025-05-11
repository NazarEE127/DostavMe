package com.example.dostavme.models;

import java.util.Date;

public class Order {
    private String id;
    private String clientId;
    private String courierId;
    private String fromAddress;
    private String toAddress;
    private String description;
    private double weight;
    private double price;
    private String status;
    private String createdAt;
    private Date updatedAt;

    public Order() {
        // Пустой конструктор для SQLite
    }

    public Order(String id, String clientId, String courierId, String fromAddress, String toAddress,
                String description, double weight, double price, String status, String createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.courierId = courierId;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.description = description;
        this.weight = weight;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCourierId() {
        return courierId;
    }

    public void setCourierId(String courierId) {
        this.courierId = courierId;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 