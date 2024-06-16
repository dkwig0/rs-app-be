package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.myorg.core.entity.Mocks;
import com.myorg.core.entity.Product;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Aliaksei Tsvirko (aliaksei.tsvirko@hycom.pl)
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductByIdLambdaUnitTest {

    @InjectMocks
    private ProductByIdLambda productByIdLambda;

    @Mock
    private APIGatewayProxyRequestEvent request;

    private final ObjectReader objectReader = new ObjectMapper().reader();

    @Before
    public void setUp() {
        Mocks.MOCK_PRODUCTS = List.of(
                new Product("1", "p1", "p1", 1),
                new Product("2", "p2", "p2", 2),
                new Product("3", "p3", "p3", 3));

        when(request.getPathParameters()).thenReturn(Map.of("productId", "1"));
    }

    @Test
    public void testHandleRequest200() throws JsonProcessingException {
        APIGatewayProxyResponseEvent result = productByIdLambda.handleRequest(request, null);

        JsonNode jsonNode = objectReader.readTree(result.getBody());

        Assert.assertEquals("1", jsonNode.get("id").asText());
        Assert.assertEquals("p1", jsonNode.get("name").asText());
        Assert.assertEquals("p1", jsonNode.get("title").asText());
        Assert.assertEquals(1, jsonNode.get("price").asInt());
    }

    @Test
    public void testHandleRequest400MoreThanOne() {
        Mocks.MOCK_PRODUCTS = List.of(
                new Product("1", "p1", "p1", 1),
                new Product("2", "p2", "p2", 2),
                new Product("1", "p3", "p3", 3));

        APIGatewayProxyResponseEvent result = productByIdLambda.handleRequest(request, null);

        Assert.assertEquals(500, result.getStatusCode().longValue());
    }

    @Test
    public void testHandleRequest400NoneFound() {
        Mocks.MOCK_PRODUCTS = List.of(
                new Product("4", "p1", "p1", 1),
                new Product("2", "p2", "p2", 2),
                new Product("3", "p3", "p3", 3));

        APIGatewayProxyResponseEvent result = productByIdLambda.handleRequest(request, null);

        Assert.assertEquals(404, result.getStatusCode().longValue());
    }
}