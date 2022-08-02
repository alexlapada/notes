package ua.alexlapada.integration;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.DeleteRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest;
import software.amazon.awssdk.services.eventbridge.model.PutRuleResponse;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutTargetsResponse;
import software.amazon.awssdk.services.eventbridge.model.RuleState;
import software.amazon.awssdk.services.eventbridge.model.Target;
import ua.alexlapada.EnvUtil;
import ua.alexlapada.JacksonUtil;
import ua.alexlapada.constant.EnvKeys;
import ua.alexlapada.exception.AwsClientException;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class AwsEventBridgeClient {
    public static final String DEFAULT_EVENT_BUS_NAME = "default";
    private static AwsEventBridgeClient instance;

    private final EventBridgeClient eventBridgeClient;


    private AwsEventBridgeClient() {
        this.eventBridgeClient = EventBridgeClient.builder()
                .region(Region.of(EnvUtil.getEnv(EnvKeys.AWS_REGION, EnvUtil.DEFAULT_REGION)))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    public static AwsEventBridgeClient instance() {
        if (instance == null) {
            instance = new AwsEventBridgeClient();
        }
        return instance;
    }

    public String createRule(String ruleName, String cron) {
        try {
            PutRuleRequest ruleRequest = PutRuleRequest.builder()
                    .name(ruleName)
                    .eventBusName(DEFAULT_EVENT_BUS_NAME)
                    .scheduleExpression(cron)
                    .state(RuleState.ENABLED)
                    .build();

            PutRuleResponse response = eventBridgeClient.putRule(ruleRequest);
            handleAWSSdkResponse(response.sdkHttpResponse(), "Create rule error.");
            return response.ruleArn();
        } catch (Exception e) {
            throw new AwsClientException("Create rule error.", e);
        }
    }

    public void deleteRule(String ruleName) {
        try {
            DeleteRuleResponse response = eventBridgeClient.deleteRule(DeleteRuleRequest.builder()
                    .eventBusName(DEFAULT_EVENT_BUS_NAME)
                    .name(ruleName)
                    .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Delete rule error.");
        } catch (Exception e) {
            throw new AwsClientException("Delete rule error.", e);
        }

    }

    public void putTargetToRule(String ruleName, String targetArn, Map<String, Object> inputParams) {
        try {
            PutTargetsResponse response = eventBridgeClient.putTargets(PutTargetsRequest.builder()
                    .rule(ruleName)
                    .targets(Target.builder()
                            .id(UUID.randomUUID().toString())
                            .arn(targetArn)
                            .input(toJson(inputParams))
                            .build())
                    .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Put target to rule error.");
        } catch (Exception e) {
            throw new AwsClientException("Put target to rule error.", e);
        }
    }

    private void handleAWSSdkResponse(SdkHttpResponse response, String message) {
        if (!response.isSuccessful()) {
            throw new AwsClientException(message + String.format(" Response code: %s", response.statusCode()));
        }
    }

    private String toJson(Map<String, Object> input) {
        try {
            return JacksonUtil.writeJson(input);
        } catch (Exception e) {
            log.warn("Parsing target params error.", e);
            return "";
        }
    }
}
