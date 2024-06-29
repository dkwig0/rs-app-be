package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.myorg.core.entity.Mocks;
import com.myorg.core.entity.Product;
import com.myorg.core.service.ProductService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Aliaksei Tsvirko (aliaksei.tsvirko@hycom.pl)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductListLambdaUnitTest {

    @InjectMocks
    private ProductListLambda productListLambda;

    @Mock
    private ProductService productService;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    private Gson gson = new Gson();

    @Before
    public void setUp() throws JsonProcessingException {
        Mocks.MOCK_PRODUCTS = List.of(
                new Product("1", "1", "1", "1", 1),
                new Product("2", "2", "2", "2", 2),
                new Product("3", "3", "3", "3", 3));

        when(productService.getAllProducts()).thenReturn(Mocks.MOCK_PRODUCTS);
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    public void testHandleRequest() throws JsonProcessingException {
        APIGatewayProxyResponseEvent result = productListLambda.handleRequest(null, context);

        List<Product> products = gson.fromJson(result.getBody(), List.class);

        Assert.assertEquals(products.size(), 3);
    }
}