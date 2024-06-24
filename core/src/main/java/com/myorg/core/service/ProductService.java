package com.myorg.core.service;

import com.myorg.core.converter.ProductScanItemConverter;
import com.myorg.core.entity.Product;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductService {

    private DynamoDbClient dynamoDbClient;

    private ProductScanItemConverter productConverter;

    public List<Product> getAllProducts() {
        String tableName = System.getenv("PRODUCT_TABLE");
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();
        ScanResponse response = dynamoDbClient.scan(scanRequest);
        return productConverter.convertAll(response.items());
    }

    public Product getProductById(String id) {
        String tableName = System.getenv("PRODUCT_TABLE");

        Map<String, AttributeValue> tableKey = new HashMap<>();
        tableKey.put("id", AttributeValue.builder().s(id).build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .key(tableKey)
                .tableName(tableName)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(getItemRequest);

        return productConverter.convert(response.item());
    }

    public Product createProduct(Product product) {
        validateProduct(product);

        String productId = String.valueOf(UUID.randomUUID());

        String productTableName = System.getenv("PRODUCT_TABLE");

        Map<String, AttributeValue> productAttributes = new HashMap<>();
        productAttributes.put("id", AttributeValue.builder().s(productId).build());
        productAttributes.put("name", AttributeValue.builder().s(product.getName()).build());
        productAttributes.put("title", AttributeValue.builder().s(product.getTitle()).build());
        productAttributes.put("description", AttributeValue.builder().s(product.getDescription()).build());
        productAttributes.put("price", AttributeValue.builder().n(product.getPrice().toString()).build());

        Put productPut = Put.builder()
                .item(productAttributes)
                .tableName(productTableName)
                .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .conditionExpression("attribute_not_exists(id)")
                .build();

        String stockTableName = System.getenv("STOCK_TABLE");

        Map<String, AttributeValue> stockAttributes = new HashMap<>();
        stockAttributes.put("product_id", AttributeValue.builder().s(productId).build());
        stockAttributes.put("count", AttributeValue.builder().n(product.getCount().toString()).build());

        Put stockPut = Put.builder()
                .item(stockAttributes)
                .tableName(stockTableName)
                .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .conditionExpression("attribute_not_exists(product_id)")
                .build();

        List<TransactWriteItem> transactionItems = Arrays.asList(
                TransactWriteItem.builder()
                        .put(productPut)
                        .build(),
                TransactWriteItem.builder()
                        .put(stockPut)
                        .build());

        TransactWriteItemsRequest transactionRequest = TransactWriteItemsRequest.builder()
                .transactItems(transactionItems)
                .returnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
                .build();

        dynamoDbClient.transactWriteItems(transactionRequest);

        return product;
    }

    private void validateProduct(Product product) {
        if (product.getCount() < 0) throw new RuntimeException("Stock cannot be less than 0.");
    }

    public void setDynamoDbClient(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void setProductConverter(ProductScanItemConverter productConverter) {
        this.productConverter = productConverter;
    }
}
