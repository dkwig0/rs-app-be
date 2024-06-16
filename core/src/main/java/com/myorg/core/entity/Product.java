package com.myorg.core.entity;

/**
 * @author Aliaksei Tsvirko
 */
public class Product {
    private String id;
    private String name;
    private String title;
    private Integer price;

    public Product(String id, String name, String title, Integer price) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
