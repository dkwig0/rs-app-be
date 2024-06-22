package com.myorg.core.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aliaksei Tsvirko
 */
public class Mocks {
    public static List<Product> MOCK_PRODUCTS = List.of(
            new Product("1", "Product 1", "Product 1", "Product 1", 15),
            new Product("2", "Product 2", "Awesome Product 2", "Awesome Product 2", 25),
            new Product("3", "Product 3", "Overrated Product 3", "Overrated Product 3", 999_999_999),
            new Product("4", "Product 4", "Suspicious Product 4", "Suspicious Product 4", 1)
    );

    public static List<Product> getMockProducts() {
        return new ArrayList<>(MOCK_PRODUCTS);
    }
}
