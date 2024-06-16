package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.core.entity.Mocks;
import com.myorg.core.entity.Product;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Aliaksei Tsvirko (aliaksei.tsvirko@hycom.pl)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductListLambdaUnitTest {

    @InjectMocks
    private ProductListLambda productListLambda;

    @Before
    public void setUp() {
        Mocks.MOCK_PRODUCTS = List.of(
                new Product("1", "1", "1", 1),
                new Product("2", "2", "2", 2),
                new Product("3", "3", "3", 3));
    }

    @Test
    public void testHandleRequest() throws JsonProcessingException {
        APIGatewayProxyResponseEvent result = productListLambda.handleRequest(null, null);

        JsonNode jsonNode = new ObjectMapper().reader().readTree(result.getBody());
        Assert.assertTrue(jsonNode.isArray());
        Assert.assertEquals(3, jsonNode.size());
    }
}