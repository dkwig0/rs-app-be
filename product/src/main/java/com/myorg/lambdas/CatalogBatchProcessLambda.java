package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.myorg.core.entity.Product;
import com.myorg.core.service.ProductService;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

import static com.myorg.core.Context.GSON;
import static com.myorg.core.Context.PRODUCT_SERVICE;

/**
 * @author Aliaksei Tsvirko
 */
public class CatalogBatchProcessLambda implements RequestHandler<SQSEvent, Void> {
    private SnsClient snsClient = SnsClient.create();
    private Gson gson = GSON;
    private ProductService productService = PRODUCT_SERVICE;

    @Override
    public Void handleRequest(SQSEvent request, Context context) {
        try {
            for (SQSEvent.SQSMessage sqsMessage : request.getRecords()) {
                Product product = gson.fromJson(sqsMessage.getBody(), Product.class);
                productService.createProduct(product);

                PublishResponse publishResponse = snsClient.publish(PublishRequest.builder()
                                .topicArn(System.getenv("PRODUCT_CREATE_TOPIC_ARN"))
                                .subject("New product")
                                .messageAttributes(toMessageAttributes(product))
                                .message(sqsMessage.getBody())
                                .build());
                context.getLogger().log("SNS message published, id - [%s]".formatted(publishResponse.messageId()));

            }
        } catch (Exception e) {
            context.getLogger().log("Error handling SQS event: " + e.getMessage());
        }

        return null;
    }

    Map<String, MessageAttributeValue> toMessageAttributes(Product product) {
        return Map.of(
                "id", toMessageAttributeValue("String", product.getId()),
                "name", toMessageAttributeValue("String", product.getName()),
                "title", toMessageAttributeValue("String", product.getTitle()),
                "description", toMessageAttributeValue("String", product.getDescription()),
                "price", toMessageAttributeValue("Number", String.valueOf(product.getPrice())),
                "count", toMessageAttributeValue("Number", String.valueOf(product.getCount()))
        );
    }

    MessageAttributeValue toMessageAttributeValue(String type, String value) {
        return MessageAttributeValue.builder().stringValue(value).dataType(type).build();
    }

    public void setSnsClient(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
