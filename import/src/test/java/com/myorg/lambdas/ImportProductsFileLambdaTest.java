package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Aliaksei Tsvirko
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportProductsFileLambdaTest {

    @InjectMocks
    private ImportProductsFileLambda lambda;

    @Mock
    private S3Presigner s3Presigner;

    private URL url;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    @Mock
    private APIGatewayProxyRequestEvent requestEvent;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger lambdaLogger;

    @Before
    public void setUp() throws Exception {
        url = new URL("http", "aaa", 18, "aaa");
        when(requestEvent.getQueryStringParameters()).thenReturn(Map.of("name", "1"));
        when(s3Presigner.presignPutObject(Mockito.any(PutObjectPresignRequest.class)))
                .thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(url);
        when(context.getLogger()).thenReturn(lambdaLogger);
    }

    @Test
    public void testHandleRequest() {
        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(requestEvent, context);

        Assert.assertEquals(url.toString(), responseEvent.getBody());
        Assert.assertEquals(200, responseEvent.getStatusCode().intValue());
    }

    @Test
    public void testHandleRequestNoFileName() {
        when(requestEvent.getQueryStringParameters()).thenReturn(Map.of("name2", "1"));

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(requestEvent, context);

        Assert.assertNotEquals(url.toString(), responseEvent.getBody());
        Assert.assertEquals(400, responseEvent.getStatusCode().intValue());
    }

    @Test
    public void testHandleRequestPreSignError() {
        when(s3Presigner.presignPutObject(Mockito.any(PutObjectPresignRequest.class)))
                .thenThrow(RuntimeException.class);

        APIGatewayProxyResponseEvent responseEvent = lambda.handleRequest(requestEvent, context);

        Assert.assertNotEquals(url.toString(), responseEvent.getBody());
        Assert.assertEquals(500, responseEvent.getStatusCode().intValue());
    }
}