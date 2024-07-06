package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.myorg.core.service.ProductService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Aliaksei Tsvirko
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogBatchProcessLambdaUnitTest {

    @InjectMocks
    private CatalogBatchProcessLambda lambda;

    @Spy
    private SnsClient snsClient;

    @Spy
    private ProductService productService;

    @Mock
    private SQSEvent event;

    @Mock
    private SQSEvent.SQSMessage sqsMessage;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @Before
    public void setUp() throws Exception {
        when(event.getRecords()).thenReturn(List.of(sqsMessage));
        when(sqsMessage.getBody()).thenReturn("""
                {
                    id:"1"
                }
                """);
        when(context.getLogger()).thenReturn(logger);

        doReturn(null).when(productService).createProduct(Mockito.any());
        doReturn(null).when(snsClient).publish(Mockito.any(PublishRequest.class));
    }

    @Test
    public void testHandleRequest() {
        lambda.handleRequest(event, context);

        verify(snsClient).publish(Mockito.any(PublishRequest.class));
        verify(productService).createProduct(Mockito.any());
    }
}