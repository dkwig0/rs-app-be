package com.myorg.cdk;

import software.amazon.awscdk.*;
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpLambdaAuthorizer;
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpLambdaAuthorizerProps;
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpLambdaResponseType;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSourceProps;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.s3.notifications.LambdaDestination;
import software.amazon.awscdk.services.sns.StringConditions;
import software.amazon.awscdk.services.sns.SubscriptionFilter;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.TopicProps;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscriptionProps;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueProps;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CdkStack extends Stack {

    public static final String ADMIN_LOGIN = "dkwig0";

    public CdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
        Map<String, String> environment = new HashMap<>();

        Topic productCreateTopic = new Topic(this, "productCreateTopic", TopicProps.builder()
                .displayName("Product Creation topic")
                .build());
        environment.put("PRODUCT_CREATE_TOPIC_ARN", productCreateTopic.getTopicArn());

        productCreateTopic.addSubscription(new EmailSubscription("forsonggggg@gmail.com"));

        productCreateTopic.addSubscription(new EmailSubscription(
                "forsongggggg@gmail.com",
                EmailSubscriptionProps.builder()
                        .filterPolicy(Map.of("name", SubscriptionFilter.stringFilter(
                                StringConditions.builder()
                                        .matchPrefixes(List.of("TEST"))
                                        .build())))
                        .build()));

        Queue catalogItemsQueue = new Queue(this, "catalogItemsQueue", QueueProps.builder()
                .visibilityTimeout(Duration.minutes(5))
                .retentionPeriod(Duration.minutes(2))
                .receiveMessageWaitTime(Duration.seconds(10))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build());
        environment.put("CATALOG_ITEMS_QUEUE_URL", catalogItemsQueue.getQueueUrl());

        SqsEventSource catalogItemsQueueEventSource = new SqsEventSource(
                catalogItemsQueue,
                SqsEventSourceProps.builder()
                        .batchSize(5)
                        .maxBatchingWindow(Duration.seconds(10))
                        .build()
        );

        Bucket importBucket = new Bucket(this, "import-bucket", BucketProps.builder()
                .removalPolicy(RemovalPolicy.DESTROY)
                .versioned(true)
                .cors(List.of(
                        CorsRule.builder()
                                .id("allow-all")
                                .allowedMethods(List.of(HttpMethods.PUT))
                                .allowedHeaders(List.of("*"))
                                .allowedOrigins(List.of("https://d1rh1cdtj16wwn.cloudfront.net"))
                                .build())
                )
                .autoDeleteObjects(true)
                .build());
        environment.put("IMPORT_BUCKET", importBucket.getBucketName());

        Table productTable = new Table(this, "product", TableProps.builder()
                .tableName("product")
                .partitionKey(Attribute.builder()
                        .name("id")
                        .type(AttributeType.STRING)
                        .build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        environment.put("PRODUCT_TABLE", productTable.getTableName());

        Table stockTable = new Table(this, "stock", TableProps.builder()
                .tableName("stock")
                .partitionKey(Attribute.builder()
                        .name("product_id")
                        .type(AttributeType.STRING)
                        .build())
                .removalPolicy(RemovalPolicy.DESTROY)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
        environment.put("STOCK_TABLE", stockTable.getTableName());

        Function productListFunction = Function.Builder.create(this, "getProductList")
                .environment(environment)
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ProductListLambda")
                .memorySize(128)
                .timeout(Duration.seconds(60))
                .functionName("getProductList")
                .code(Code.fromAsset("../assets/product.jar"))
                .build();
        productTable.grantReadWriteData(productListFunction);
        stockTable.grantReadWriteData(productListFunction);

        Function productByIdFunction = Function.Builder.create(this, "getProductById")
                .environment(environment)
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ProductByIdLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("getProductById")
                .code(Code.fromAsset("../assets/product.jar"))
                .build();
        productTable.grantReadWriteData(productByIdFunction);
        stockTable.grantReadWriteData(productByIdFunction);

        Function productCreateFunction = Function.Builder.create(this, "createProduct")
                .environment(environment)
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ProductCreateLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("createProduct")
                .code(Code.fromAsset("../assets/product.jar"))
                .build();
        productTable.grantReadWriteData(productCreateFunction);
        stockTable.grantReadWriteData(productCreateFunction);

        Function importProductsFileFunction = Function.Builder.create(this, "importProductsFile")
                .environment(environment)
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ImportProductsFileLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("importProductsFile")
                .code(Code.fromAsset("../assets/import.jar"))
                .build();
        importBucket.grantReadWrite(importProductsFileFunction);

        Function importFileParserFunction = Function.Builder.create(this, "importFileParser")
                .environment(environment)
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.ImportFileParserLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("importFileParser")
                .code(Code.fromAsset("../assets/import.jar"))
                .build();
        importBucket.grantReadWrite(importFileParserFunction);
        catalogItemsQueue.grantSendMessages(importFileParserFunction);

        Function catalogBatchProcessFunction = Function.Builder.create(this, "catalogBatchProcessFunction")
                .environment(environment)
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.CatalogBatchProcessLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("catalogBatchProcessFunction")
                .code(Code.fromAsset("../assets/product.jar"))
                .build();
        importBucket.grantReadWrite(catalogBatchProcessFunction);
        productTable.grantReadWriteData(catalogBatchProcessFunction);
        stockTable.grantReadWriteData(catalogBatchProcessFunction);
        catalogBatchProcessFunction.addEventSource(catalogItemsQueueEventSource);
        catalogItemsQueue.grantConsumeMessages(catalogBatchProcessFunction);
        productCreateTopic.grantPublish(catalogBatchProcessFunction);

        Function basicAuthorizerFunction = Function.Builder.create(this, "basicAuthorizer")
                .environment(Map.of(ADMIN_LOGIN, System.getenv(ADMIN_LOGIN)))
                .runtime(Runtime.JAVA_17)
                .handler("com.myorg.lambdas.BasicAuthorizerLambda")
                .memorySize(128)
                .timeout(Duration.seconds(20))
                .functionName("basicAuthorizer")
                .code(Code.fromAsset("../assets/authorization.jar"))
                .build();

        HttpLambdaAuthorizer authorizer = new HttpLambdaAuthorizer("authorizer", basicAuthorizerFunction,
                HttpLambdaAuthorizerProps.builder()
                        .responseTypes(List.of(HttpLambdaResponseType.IAM))
                        .build());

        importBucket.addEventNotification(EventType.OBJECT_CREATED,
                new LambdaDestination(importFileParserFunction),
                NotificationKeyFilter.builder()
                        .prefix("uploaded/")
                        .build());

        HttpApi httpApi = HttpApi.Builder.create(this, "product-service")
                .corsPreflight(CorsPreflightOptions.builder()
                        .allowMethods(List.of(CorsHttpMethod.ANY))
                        .allowHeaders(List.of("*"))
                        .exposeHeaders(List.of("*"))
                        .allowOrigins(List.of("https://d1rh1cdtj16wwn.cloudfront.net"))
                        .build())
                .apiName("product service")
                .build();

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/products")
                .methods(List.of(HttpMethod.GET))
                .integration(new HttpLambdaIntegration("products", productListFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/products/{productId}")
                .methods(List.of(HttpMethod.GET))
                .integration(new HttpLambdaIntegration("productById", productByIdFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/products")
                .methods(List.of(HttpMethod.POST))
                .integration(new HttpLambdaIntegration("productCreate", productCreateFunction))
                .build());

        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/import")
                .methods(List.of(HttpMethod.GET))
                .authorizer(authorizer)
                .integration(new HttpLambdaIntegration("importProductsFile", importProductsFileFunction))
                .build());
    }
}
