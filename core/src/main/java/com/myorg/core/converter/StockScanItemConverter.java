package com.myorg.core.converter;

import com.myorg.core.ScanItemUtils;
import com.myorg.core.entity.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * @author Aliaksei Tsvirko
 */
public class StockScanItemConverter extends AbstractConverter<Stock, Map<String, AttributeValue>> {

    private static final Logger LOG = LoggerFactory.getLogger(StockScanItemConverter.class);

    @Override
    public Stock convert(Map<String, AttributeValue> item) {
        if (item == null) return null;

        Stock stock = new Stock();
        try {
            stock.setProductId(ScanItemUtils.getString("product_id", item));
            stock.setCount(ScanItemUtils.getInteger("count", item));
        } catch (Exception e) {
            LOG.warn("Error converting item to product");
        }

        return stock;
    }
}
