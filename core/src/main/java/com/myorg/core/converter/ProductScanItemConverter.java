package com.myorg.core.converter;

import com.myorg.core.ScanItemUtils;
import com.myorg.core.entity.Product;
import com.myorg.core.entity.Stock;
import com.myorg.core.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductScanItemConverter extends AbstractConverter<Product, Map<String, AttributeValue>> {

    private static final Logger LOG = LoggerFactory.getLogger(ProductScanItemConverter.class);

    private StockService stockService;

    @Override
    public Product convert(Map<String, AttributeValue> item) {
        if (item == null) return null;

        Product product = new Product();
        try {
            String id = ScanItemUtils.getString("id", item);

            product.setId(id);
            product.setName(ScanItemUtils.getString("name", item));
            product.setDescription(ScanItemUtils.getString("description", item));
            product.setTitle(ScanItemUtils.getString("title", item));
            product.setPrice(ScanItemUtils.getInteger("price", item));

            Stock stock = stockService.getStockByProductId(id);
            product.setCount(stock.getCount());
        } catch (Exception e) {
            LOG.warn("Error converting item to product");
        }

        return product;
    }

    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
}
