package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.myorg.core.entity.Product;
import com.myorg.core.service.ProductService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Aliaksei Tsvirko (aliaksei.tsvirko@hycom.pl)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductByIdLambdaUnitTest {

    @InjectMocks
    private ProductByIdLambda productByIdLambda;

    @Mock
    private ProductService productService;

    @Mock
    private APIGatewayProxyRequestEvent request;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    private Gson gson = new Gson();

    @Before
    public void setUp() {

        when(request.getPathParameters()).thenReturn(Map.of("productId", "1"));
        when(productService.getProductById("1")).thenReturn(
                new Product("1", "p1", "p1", "p1", 1));

        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    public void testHandleRequest200() throws JsonProcessingException {
        APIGatewayProxyResponseEvent result = productByIdLambda.handleRequest(request, context);

        Product product = gson.fromJson(result.getBody(), Product.class);

        Assert.assertEquals("1", product.getId());
        Assert.assertEquals("p1", product.getName());
        Assert.assertEquals("p1", product.getTitle());
        Assert.assertEquals(1, product.getPrice().intValue());
    }

    @Test
    public void testHandleRequest400NoneFound() {
        when(productService.getProductById("1")).thenReturn(null);

        APIGatewayProxyResponseEvent result = productByIdLambda.handleRequest(request, context);

        Assert.assertEquals(404, result.getStatusCode().longValue());
    }
}