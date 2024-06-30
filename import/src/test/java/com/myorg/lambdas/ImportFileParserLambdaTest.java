package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Aliaksei Tsvirko
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportFileParserLambdaTest {

    @InjectMocks
    private ImportFileParserLambda lambda;

    @Mock
    private S3Event requestEvent;

    @Mock
    private Context context;

    @Mock
    private LambdaLogger lambdaLogger;

    @Spy
    private S3Client s3Client;

    @Mock
    private S3EventNotification.S3EventNotificationRecord notificationRecord;

    @Mock
    private S3EventNotification.S3Entity s3Entity;

    @Mock
    private S3EventNotification.S3BucketEntity bucket;

    @Mock
    private S3EventNotification.S3ObjectEntity object;

    private ResponseInputStream<GetObjectResponse> responseInputStream;

    @Mock
    private GetObjectResponse getObjectResponse;

    @Before
    public void setUp() throws Exception {
        when(context.getLogger()).thenReturn(lambdaLogger);
        when(requestEvent.getRecords()).thenReturn(List.of(notificationRecord));
        when(notificationRecord.getS3()).thenReturn(s3Entity);
        when(s3Entity.getBucket()).thenReturn(bucket);
        when(bucket.getName()).thenReturn("bucket");
        when(s3Entity.getObject()).thenReturn(object);
        when(object.getKey()).thenReturn("object");

        responseInputStream = new ResponseInputStream<>(
                getObjectResponse, new ByteArrayInputStream("123,123,123,123".getBytes()));

        doReturn(responseInputStream).when(s3Client).getObject(Mockito.any(GetObjectRequest.class));
        doReturn(null).when(s3Client).copyObject(Mockito.any(CopyObjectRequest.class));
        doReturn(null).when(s3Client).deleteObject(Mockito.any(DeleteObjectRequest.class));
    }

    @Test
    public void testHandleRequest() {

        lambda.handleRequest(requestEvent, context);

        verify(s3Client).copyObject(Mockito.any(CopyObjectRequest.class));
        verify(s3Client).deleteObject(Mockito.any(DeleteObjectRequest.class));
    }

    @Test
    public void testHandleRequestErrorGettingObject() {
        doThrow(RuntimeException.class).when(s3Client).getObject(Mockito.any(GetObjectRequest.class));

        lambda.handleRequest(requestEvent, context);

        verify(s3Client, times(0)).copyObject(Mockito.any(CopyObjectRequest.class));
        verify(s3Client, times(0)).deleteObject(Mockito.any(DeleteObjectRequest.class));
    }
}