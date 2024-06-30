package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.opencsv.CSVReader;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author Aliaksei Tsvirko
 */
public class ImportFileParserLambda implements RequestHandler<S3Event, Void> {
    private S3Client s3Client = S3Client.create();

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
    ) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(responseStream))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                context.getLogger().log("CSV row: " + String.join(",", line));
            }

            removeFileAfterParsing(bucketName, objectKey, context);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV file and moving it to 'parsed/' folder", e);
        }
    }

    private void removeFileAfterParsing(String bucketName, String objectKey, Context context) {
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
}
