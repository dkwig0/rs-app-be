package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.myorg.core.entity.Product;
import com.myorg.core.service.ProductService;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.Map;
import java.util.stream.Collectors;

import static com.myorg.core.Context.PRODUCT_SERVICE;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductCreateLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Map<String, String> HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "POST",
            "Content-Type", "application/json",
            "Accept", "application/json");
    private final Gson gson = new Gson();
    private ProductService productService = PRODUCT_SERVICE;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        context.getLogger().log("Handling product creation request.");

        try {
            Product product = productService.createProduct(gson.fromJson(request.getBody(), Product.class));

            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS)
                    .withStatusCode(200)
                    .withBody(gson.toJson(product, Product.class));
        } catch (TransactionCanceledException | JsonSyntaxException e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS.entrySet().stream()
                            .filter(header -> !header.getKey().equals("Content-Type"))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .withStatusCode(400)
                    .withBody("Invalid product data.");
        } catch (Exception e) {
            context.getLogger().log(e.getClass().getSimpleName() + " ERROR: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS.entrySet().stream()
                            .filter(header -> !header.getKey().equals("Content-Type"))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .withStatusCode(500)
                    .withBody("Unexpected error.");
        }
    }
}
