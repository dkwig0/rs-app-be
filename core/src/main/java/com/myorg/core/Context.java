package com.myorg.core;

import com.google.gson.Gson;
import com.myorg.core.converter.ProductCSVConverter;
import com.myorg.core.converter.ProductScanItemConverter;
import com.myorg.core.converter.StockScanItemConverter;
import com.myorg.core.service.ProductService;
import com.myorg.core.service.StockService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * @author Aliaksei Tsvirko
 */
public class Context {
    static {
        ProductScanItemConverter productScanItemConverter = new ProductScanItemConverter();
        StockScanItemConverter stockScanItemConverter = new StockScanItemConverter();
        ProductCSVConverter productCSVConverter = new ProductCSVConverter();

        DynamoDbClient dynamoDbClient = DynamoDbClient.create();

        ProductService productService = new ProductService();
        productService.setDynamoDbClient(dynamoDbClient);
        productService.setProductConverter(productScanItemConverter);

        StockService stockService = new StockService();
        stockService.setDynamoDbClient(dynamoDbClient);
        stockService.setStockScanItemConverter(stockScanItemConverter);

        productScanItemConverter.setStockService(stockService);


        DYNAMO_DB_CLIENT = dynamoDbClient;

        PRODUCT_SERVICE = productService;
        STOCK_SERVICE = stockService;

        PRODUCT_SCAN_ITEM_CONVERTER = productScanItemConverter;
        STOCK_SCAN_ITEM_CONVERTER = stockScanItemConverter;
        PRODUCT_CSV_CONVERTER = productCSVConverter;

        GSON = new Gson();
    }

    public static DynamoDbClient DYNAMO_DB_CLIENT;

    public static ProductService PRODUCT_SERVICE;
    public static StockService STOCK_SERVICE;

    public static ProductScanItemConverter PRODUCT_SCAN_ITEM_CONVERTER;
    public static StockScanItemConverter STOCK_SCAN_ITEM_CONVERTER;
    public static ProductCSVConverter PRODUCT_CSV_CONVERTER;

    public static Gson GSON;

}
