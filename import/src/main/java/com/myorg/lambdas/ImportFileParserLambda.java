package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.google.gson.Gson;
import com.myorg.core.converter.ProductCSVConverter;
import com.myorg.core.entity.Product;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import static com.myorg.core.Context.GSON;
import static com.myorg.core.Context.PRODUCT_CSV_CONVERTER;

/**
 * @author Aliaksei Tsvirko
 */
public class ImportFileParserLambda implements RequestHandler<S3Event, Void> {
    private S3Client s3Client = S3Client.create();
    private SqsClient sqsClient = SqsClient.create();
    private Gson gson = GSON;
    private ProductCSVConverter productCSVConverter = PRODUCT_CSV_CONVERTER;

    @Override
    public Void handleRequest(S3Event event, Context context) {
        try {
            for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
                String bucketName = record.getS3().getBucket().getName();
                String objectKey = record.getS3().getObject().getKey();

                ResponseInputStream<GetObjectResponse> responseStream = downloadObject(bucketName, objectKey);
                parseCsvFile(responseStream, bucketName, objectKey, context);
            }
        } catch (Exception e) {
            context.getLogger().log("Error handling S3 event: " + e.getMessage());
        }

        return null;
    }

    private ResponseInputStream<GetObjectResponse> downloadObject(String bucketName, String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    private void parseCsvFile(
            ResponseInputStream<GetObjectResponse> responseStream, String bucketName, String objectKey, Context context
    ) throws CsvValidationException, IOException {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(responseStream))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                sendProductToSQS(productCSVConverter.convert(line));
            }

            moveFileAfterParsing(bucketName, objectKey, context);

        }
    }

    private void sendProductToSQS(Product product) {
        var productMessageAttributeValue = MessageAttributeValue.builder()
                .stringValue(product.getId())
                .dataType("String")
                .build();

        var sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(System.getenv("CATALOG_ITEMS_QUEUE_URL"))
                .messageBody(gson.toJson(product))
                .messageAttributes(Map.of("Product", productMessageAttributeValue))
                .build();
        sqsClient.sendMessage(sendMessageRequest);
    }

    private void moveFileAfterParsing(String bucketName, String objectKey, Context context) {
        String parsedObjectKey = "parsed/" + objectKey.substring(objectKey.lastIndexOf('/') + 1);
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(objectKey)
                .destinationBucket(bucketName)
                .destinationKey(parsedObjectKey)
                .build();

        s3Client.copyObject(copyRequest);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        s3Client.deleteObject(deleteRequest);

        context.getLogger().log("File moved from 'uploaded/' to 'parsed/': " + objectKey);
    }

    public void setS3Client(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void setSqsClient(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public void setProductCSVConverter(ProductCSVConverter productCSVConverter) {
        this.productCSVConverter = productCSVConverter;
    }
}
