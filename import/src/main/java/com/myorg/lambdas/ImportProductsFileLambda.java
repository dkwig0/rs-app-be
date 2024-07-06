package com.myorg.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * @author Aliaksei Tsvirko
 */
public class ImportProductsFileLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Map<String, String> HEADERS = Map.of(
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS",
            "Access-Control-Allow-Headers", "Content-Type");

    private S3Presigner s3Presigner = S3Presigner.builder()
            .region(Region.EU_WEST_1)
            .build();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        Map<String, String> queryStringParameters = request.getQueryStringParameters();

        context.getLogger().log("Importing products file with name");

        try {
            String fileName = queryStringParameters.get("name");
            context.getLogger().log("Importing products file with name - " + fileName);

            if (fileName == null) {
                return new APIGatewayProxyResponseEvent()
                        .withHeaders(HEADERS)
                        .withStatusCode(400)
                        .withBody("No request parameter [name].");
            }

            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS)
                    .withStatusCode(200)
                    .withBody(generateSignedUrl(fileName));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(HEADERS)
                    .withStatusCode(500)
                    .withBody("Unexpected error. Message: " + e.getMessage());
        }
    }

    public String generateSignedUrl(String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(System.getenv("IMPORT_BUCKET"))
                .key("uploaded/" + fileName)
                .build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest preSignedRequest = s3Presigner.presignPutObject(putObjectPresignRequest);

        URL signedUrl = preSignedRequest.url();


        return signedUrl.toString();
    }

    public void setS3Presigner(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }
}
