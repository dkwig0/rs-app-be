package com.myorg.cdk;

import software.amazon.awscdk.*;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegrationProps;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import java.util.List;

public class CdkStack extends Stack {
    public CdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function productListFunction = Function.Builder.create(this, "getProductList")
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ProductListLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("getProductList")
                .code(Code.fromAsset("../assets/lambdas.jar"))
                .build();

        Function productByIdFunction = Function.Builder.create(this, "getProductById")
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ProductByIdLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("getProductById")
                .code(Code.fromAsset("../assets/lambdas.jar"))
                .build();

        HttpApi httpApi = HttpApi.Builder.create(this, "product-service")
                .apiName("product service")
                .build();

        httpApi.addRoutes(AddRoutesOptions.builder()
                        .path("/products")
                        .integration(new HttpLambdaIntegration("products", productListFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/products/{productId}")
                .integration(new HttpLambdaIntegration("productById", productByIdFunction))
                .build());
    }
}
