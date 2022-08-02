package ua.alexlapada.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@ConfigurationProperties("cognito")
public class AWSCognitoProperties {

    @NotBlank
    private String region;
    @NotBlank
    private String userPoolId;
    @NotBlank
    private String clientId;
    @NotBlank
    private String ssoClientId;
    @NotBlank
    private String ssoClientSecret;
    @NotBlank
    private String tokenUrl;
}
