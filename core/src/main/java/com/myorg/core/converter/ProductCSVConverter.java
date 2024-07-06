package com.myorg.core.converter;

import com.myorg.core.entity.Product;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductCSVConverter extends AbstractConverter<Product, String[]> {
    @Override
    public Product convert(String[] item) {
        Product product = new Product();

        product.setId(item[0]);
        product.setName(item[1]);
        product.setTitle(item[2]);
        product.setDescription(item[3]);
        product.setPrice(Integer.parseInt(item[4]));
        product.setCount(Integer.parseInt(item[5]));

        return product;
    }
}
