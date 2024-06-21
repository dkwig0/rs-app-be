package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.myorg.core.entity.Mocks;
import com.myorg.core.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductByIdLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Map<String, String> HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "GET",
            "Content-Type", "application/json");
    private final ObjectWriter objectWriter = new ObjectMapper().writer();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String productId = request.getPathParameters().get("productId");

        List<Product> productsForId = Mocks.getMockProducts().stream()
                .filter(product -> product.getId().equals(productId))
                .toList();

        if (productsForId.size() > 1) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("More than one products found for id %s.".formatted(productId));
        }

        if (productsForId.isEmpty()) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(404)
                    .withBody("No product found for id %s.".formatted(productId));
        }

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(HEADERS)
                    .withBody(objectWriter.writeValueAsString(productsForId.get(0)));
        } catch (JsonProcessingException ignore) {}

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("Error finding product with ID %s.".formatted(productId));
    }
}
