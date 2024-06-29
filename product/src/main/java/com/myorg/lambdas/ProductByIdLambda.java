package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.myorg.core.entity.Product;
import com.myorg.core.service.ProductService;

import java.util.Map;
import java.util.stream.Collectors;

import static com.myorg.core.Context.PRODUCT_SERVICE;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductByIdLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Map<String, String> HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "GET",
            "Content-Type", "application/json");
    private ProductService productService = PRODUCT_SERVICE;
    private final Gson gson = new Gson();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String productId = request.getPathParameters().get("productId");

            context.getLogger().log("Handling product request: id - %s.".formatted(productId));

            Product product = productService.getProductById(productId);

            if (product == null) {
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(HEADERS.entrySet().stream()
                                .filter(header -> !header.getKey().equals("Content-Type"))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .withStatusCode(404)
                        .withBody("No product found for id %s.".formatted(productId));
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(HEADERS)
                    .withBody(gson.toJson(product));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS.entrySet().stream()
                            .filter(header -> !header.getKey().equals("Content-Type"))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .withStatusCode(500)
                    .withBody("Unexpected error.");
        }
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
