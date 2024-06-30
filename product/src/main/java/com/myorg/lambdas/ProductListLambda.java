package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.myorg.core.entity.ErrorResponse;
import com.myorg.core.entity.Product;
import com.myorg.core.service.ProductService;

import java.util.List;
import java.util.Map;

import static com.myorg.core.Context.GSON;
import static com.myorg.core.Context.PRODUCT_SERVICE;

/**
 * @author Aliaksei Tsvirko
 */
public class ProductListLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Map<String, String> HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "GET",
            "Content-Type", "application/json");
    public Gson gson = GSON;
    private ProductService productService = PRODUCT_SERVICE;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            context.getLogger().log("Handling product list request.");

            List<Product> products = productService.getAllProducts();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(HEADERS)
                    .withBody(GSON.toJson(products));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS)
                    .withStatusCode(500)
                    .withBody(gson.toJson(new ErrorResponse("Unexpected error.")));
        }
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
