package com.myorg.core.service;

import com.myorg.core.converter.StockScanItemConverter;
import com.myorg.core.entity.Stock;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aliaksei Tsvirko
 */
public class StockService {
    private DynamoDbClient dynamoDbClient;
    private StockScanItemConverter stockScanItemConverter;

    public List<Stock> getAllStocks() {
        String tableName = System.getenv("PRODUCT_TABLE");
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        return stockScanItemConverter.convertAll(scanResponse.items());
    }

    public Stock getStockByProductId(String productId) {
        String tableName = System.getenv("STOCK_TABLE");
        Map<String, AttributeValue> tableKey = new HashMap<>();
        tableKey.put("product_id", AttributeValue.builder().s(productId).build());
        GetItemRequest getItemRequest= GetItemRequest.builder()
                .key(tableKey)
                .tableName(tableName)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(getItemRequest);
        return stockScanItemConverter.convert(response.item());
    }

    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }

    public void setDynamoDbClient(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public StockScanItemConverter getStockScanItemConverter() {
        return stockScanItemConverter;
    }

    public void setStockScanItemConverter(StockScanItemConverter stockScanItemConverter) {
        this.stockScanItemConverter = stockScanItemConverter;
    }
}
