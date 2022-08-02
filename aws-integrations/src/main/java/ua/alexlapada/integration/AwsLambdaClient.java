package ua.alexlapada.integration;

import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.AddPermissionRequest;
import software.amazon.awssdk.services.lambda.model.AddPermissionResponse;
import ua.alexlapada.EnvUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;

public class AwsLambdaClient {
    private static AwsLambdaClient instance;

    private final LambdaClient lambdaClient;

    private AwsLambdaClient() {
        this.lambdaClient = LambdaClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    public static AwsLambdaClient instance() {
        if (instance == null) {
            instance = new AwsLambdaClient();
        }
        return instance;
    }

    public void syncEventRuleWithLambda(String ruleArn, String name, String lambdaArn) {
        try {
            AddPermissionResponse response = lambdaClient.addPermission(AddPermissionRequest.builder()
                    .action("lambda:InvokeFunction")
                    .principal("events.amazonaws.com")
                    .functionName(lambdaArn)
                    .sourceArn(ruleArn)
                    .statementId(name + "-statement")
                    .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Sync event rule with lambda error.");
        } catch (Exception e) {
            throw new AwsClientException("Sync event rule with lambda error.", e);
        }
    }

    private void handleAWSSdkResponse(SdkHttpResponse response, String message) {
        if (!response.isSuccessful()) {
            throw new AwsClientException(message + String.format(" Response code: %s", response.statusCode()));
        }
    }
}
