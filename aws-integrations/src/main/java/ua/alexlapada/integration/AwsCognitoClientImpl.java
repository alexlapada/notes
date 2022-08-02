package ua.alexlapada.integration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUserGlobalSignOutResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateIdentityProviderRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateIdentityProviderResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DescribeUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.IdentityProviderTypeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.LimitExceededException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UpdateUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;
import software.amazon.awssdk.utils.ImmutableMap;
import ua.alexlapada.AwsCognitoUtil;
import ua.alexlapada.configuration.AWSCognitoProperties;
import ua.alexlapada.exception.AWSConfirmForgotPasswordException;
import ua.alexlapada.exception.AWSInvalidPasswordException;
import ua.alexlapada.exception.AwsClientException;
import ua.alexlapada.model.cognito.ChangePasswordModel;
import ua.alexlapada.model.cognito.ChangeTemporaryPasswordModel;
import ua.alexlapada.model.cognito.ConfirmForgotPasswordModel;
import ua.alexlapada.model.cognito.InitAuthModel;
import ua.alexlapada.model.cognito.ModifiableIdpModel;
import ua.alexlapada.model.cognito.RegisterUserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AwsCognitoClientImpl implements AwsCognitoClient {

    private static final String EMAIL_ATTR_NAME = "email";
    private static final String SYNC_ATTR_NAME = "custom:userSync";
    private static final String PASSWORD_OR_USERNAME_INCORRECT = "Incorrect username or password";
    private static final String TO_MANY_ATTEMPTS = "Password attempts exceeded";

    private final CognitoIdentityProviderClient cognitoClient;
    private final AWSCognitoProperties properties;

    @Override
    public String registerUser(RegisterUserModel registerUserModel) {
        try {
            SignUpResponse response = cognitoClient.signUp(SignUpRequest.builder()
                                                                        .clientId(properties.getClientId())
                                                                        .username(registerUserModel.getEmail())
                                                                        .password(registerUserModel.getPassword())
                                                                        .userAttributes(AttributeType.builder()
                                                                                                     .name(EMAIL_ATTR_NAME)
                                                                                                     .value(registerUserModel.getEmail())
                                                                                                     .build(),
                                                                                        AttributeType.builder()
                                                                                                     .name(SYNC_ATTR_NAME)
                                                                                                     .value("true")
                                                                                                     .build())
                                                                        .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Registration user on cognito is not successfully.");
            log.info("User {} registered on cognito successfully", registerUserModel.getUserName());
            return response.userSub();
        } catch (InvalidPasswordException e) {
            log.error("Password does not conform to cognito policy.");
            throw new AWSInvalidPasswordException("Password does not conform to cognito policy: " +
                            "Min length 8, allowed uppercase letters, lowercase letters, special characters, numbers");
        } catch (UsernameExistsException e) {
            log.error("Registration user on cognito error. Reason {}", e.getMessage());
            throw new IllegalArgumentException("The given email cannot be accepted, please try a different one", e);
        } catch (Exception e) {
            log.error("Registration user on cognito error. Reason {}", e.getMessage());
            throw new AwsClientException(String.format("Registration user on cognito error. %s", e.getMessage()), e);
        }
    }

    @Override
    public void changeTemporaryPassword(ChangeTemporaryPasswordModel changeTemporaryPasswordModel) {
        try {
            RespondToAuthChallengeResponse challengeResponse =
                            cognitoClient.respondToAuthChallenge(RespondToAuthChallengeRequest.builder()
                                                                                              .challengeName(changeTemporaryPasswordModel.getChallengeName())
                                                                                              .challengeResponses(
                                                                                                              ImmutableMap.of("USERNAME",
                                                                                                                              changeTemporaryPasswordModel.getUserName(),
                                                                                                                              "NEW_PASSWORD",
                                                                                                                              changeTemporaryPasswordModel.getNewPassword()))
                                                                                              .clientId(properties.getClientId())
                                                                                              .session(changeTemporaryPasswordModel.getSessionId())
                                                                                              .build());
            handleAWSSdkResponse(challengeResponse.sdkHttpResponse(), "Change temporary password on cognito error");
            log.info("Password for username {} successfully changed.", changeTemporaryPasswordModel.getUserName());
        } catch (Exception e) {
            log.error("Change temporary password on cognito error. Reason {}", e.getMessage());
            throw new AwsClientException(String
                            .format("Registration user on cognito error. %s", e.getMessage()
                                                                               .substring(0, 50)), e);
        }
    }

    @Override
    public void changePassword(ChangePasswordModel changePasswordModel) {
        try {
            ChangePasswordResponse response = cognitoClient.changePassword(ChangePasswordRequest.builder()
                                                                                                .accessToken(changePasswordModel.getAccessToken())
                                                                                                .previousPassword(changePasswordModel.getOldPassword())
                                                                                                .proposedPassword(changePasswordModel.getNewPassword())
                                                                                                .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Change password on cognito error");
            log.info("Password successfully changed.");
        } catch (NotAuthorizedException e) {
            log.error("User not authorized on cognito. {}", e.getMessage());
            throw new BadCredentialsException("Incorrect old password.");
        } catch (LimitExceededException e) {
            log.error("User not authorized on cognito. {}", e.getMessage());
            throw new BadCredentialsException("Attempt limit exceeded.");
        } catch(Exception e) {
            log.error("Change password on cognito error. Reason {}", e.getMessage());
            throw new AwsClientException(String.format("Change password  on cognito error. %s", e.getMessage()), e);
        }
    }

    @Override
    public void forgotPassword(String email) {
        try {
            ForgotPasswordResponse response = cognitoClient.forgotPassword(ForgotPasswordRequest.builder()
                                                                                                .clientId(properties.getClientId())
                                                                                                .username(email)
                                                                                                .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Forgot password on cognito error");
            log.info("Forgot password initiated. OTP was sent to {}." , email);
        } catch (Exception e) {
            log.error("Initiate forgot password on cognito error. Reason {}", e.getMessage());
            throw new AwsClientException(String.format("Initiate forgot password on cognito error. %s", e.getMessage()), e);
        }
    }

    @Override
    public void confirmForgotPassword(ConfirmForgotPasswordModel model) {
        try {
            ConfirmForgotPasswordResponse response = cognitoClient.confirmForgotPassword(ConfirmForgotPasswordRequest.builder()
                                                                                                                     .clientId(properties.getClientId())
                                                                                                                     .username(model.getEmail())
                                                                                                                     .password(model.getNewPassword())
                                                                                                                     .confirmationCode(model.getOtp())
                                                                                                                     .build());
            handleAWSSdkResponse(response.sdkHttpResponse(), "Forgot password on cognito error");
            log.info("Forgot password confirmed successfully. Password was changed for {}.", model.getEmail());
        } catch (CodeMismatchException e) {
            log.error("Confirm forgot password on cognito error. Invalid confirmation code.");
            throw new AWSConfirmForgotPasswordException("Confirm forgot password on cognito error. Invalid confirmation code provided.", e);
        } catch (Exception e) {
            log.error("Forgot password confirmation on cognito error. Reason {}", e.getMessage());
            throw new AwsClientException(String.format("Forgot password confirmation on cognito error. %s", e.getMessage()), e);
        }
    }

    @Override
    public String registerUserWithTempPassword(RegisterUserModel registerUserModel) {
        try {
            AdminCreateUserResponse response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                                                                                                   .username(registerUserModel.getEmail())
                                                                                                   .temporaryPassword("P@ssw0rd!")
                                                                                                   .userPoolId(properties.getUserPoolId())
                                                                                                   .userAttributes(AttributeType.builder()
                                                                                                                                .name(EMAIL_ATTR_NAME)
                                                                                                                                .value(registerUserModel.getEmail())
                                                                                                                                .build())
                                                                                                   .build());

            handleAWSSdkResponse(response.sdkHttpResponse(), "Register with temporary password on cognito error.");

            return Optional.ofNullable(response)
                           .map(AdminCreateUserResponse::user)
                           .map(UserType::attributes)
                           .orElse(Collections.emptyList())
                           .stream()
                           .filter(attr -> "sub".equals(attr.name()))
                           .map(AttributeType::value)
                           .findAny()
                           .orElseThrow(() -> new AwsClientException("Registration admin user on cognito error. " +
                                           "Cognito didn't return cognitoId"));
        } catch (Exception e) {
            throw new AwsClientException("Registration admin user on cognito error.", e);
        }

    }

    @Override
    public InitAuthModel initAuth(String username, String password) {
        try {
            return login(username, password);
        } catch (NotAuthorizedException e) {
            log.error("User not authorized on cognito. {}", e.getMessage());
            String errorMessage = e.getMessage()
                                   .contains(TO_MANY_ATTEMPTS) ? TO_MANY_ATTEMPTS : PASSWORD_OR_USERNAME_INCORRECT;
            throw new BadCredentialsException(errorMessage);
        } catch (Exception e) {
            log.error("Init Auth on cognito error. Reason: {}", e.getMessage(), e);
            throw new AwsClientException("Init Auth on cognito error. Reason: " + e.getMessage());
        }
    }

    @Override
    public InitAuthModel initAuthWithRefreshToken(String refreshToken) {
        try {
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(InitiateAuthRequest.builder()
                                                                                              .authFlow("REFRESH_TOKEN_AUTH")
                                                                                              .clientId(properties.getClientId())
                                                                                              .authParameters(ImmutableMap.of(
                                                                                                              "REFRESH_TOKEN",
                                                                                                              refreshToken))
                                                                                              .build());
            handleAWSSdkResponse(authResponse.sdkHttpResponse(), "Retrieve token with refresh token on cognito error.");
            AuthenticationResultType resultType = authResponse.authenticationResult();
            return InitAuthModel.builder()
                                .accessToken(resultType.accessToken())
                                .expiresIn(resultType.expiresIn())
                                .idToken(resultType.idToken())
                                .refreshToken(resultType.refreshToken())
                                .tokenType(resultType.tokenType())
                                .build();
        } catch (Exception e) {
            log.error("Retrieve token with refresh token on cognito error. Reason: {}", e.getMessage(), e);
            throw new AwsClientException(
                            "Retrieve token with refresh token on cognito error. Reason: " + e.getMessage());
        }
    }

    @Override
    public Optional<InitAuthModel> initAuthOptional(String username, String password) {
        try {
            return Optional.of(login(username, password));
        } catch (NotAuthorizedException | UserNotFoundException e) {
            log.debug("User not authorized on cognito. {}", e.getMessage());
            if (e.getMessage()
                 .contains(TO_MANY_ATTEMPTS)) {
                throw new BadCredentialsException(TO_MANY_ATTEMPTS);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Init Auth on cognito error. Reason: {}", e.getMessage(), e);
            throw new AwsClientException("Init Auth on cognito error. Reason: " + e.getMessage());
        }
    }

    private InitAuthModel login(String username, String password) {
        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(InitiateAuthRequest.builder()
                                                                                          .authFlow("USER_PASSWORD_AUTH")
                                                                                          .clientId(properties.getClientId())
                                                                                          .authParameters(ImmutableMap.of(
                                                                                                          "USERNAME",
                                                                                                          username,
                                                                                                          "PASSWORD",
                                                                                                          password))
                                                                                          .build());
        handleAWSSdkResponse(authResponse.sdkHttpResponse(), "Init Auth on cognito error.");
        AuthenticationResultType resultType = authResponse.authenticationResult();
        return InitAuthModel.builder()
                            .accessToken(resultType.accessToken())
                            .expiresIn(resultType.expiresIn())
                            .idToken(resultType.idToken())
                            .refreshToken(resultType.refreshToken())
                            .tokenType(resultType.tokenType())
                            .build();
    }

    @Override
    public boolean existsUserByEmail(String email) {
        try {
            AdminGetUserResponse userResponse = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                                                                                              .username(email)
                                                                                              .userPoolId(properties.getUserPoolId())
                                                                                              .build());
            handleAWSSdkResponse(userResponse.sdkHttpResponse(),
                            String.format("Find user by email %s error on cognito.", email));
            return AwsCognitoUtil.isUserEmailVerified(userResponse.userAttributes());
        } catch (UserNotFoundException e) {
            log.debug("Get user by email {} not found", email);
            return false;
        } catch (Exception e) {
            log.error("Get user by email {} error.", email, e);
            throw new AwsClientException(String.format("Get user by email %s error.", email));
        }
    }

    @Override
    public void adminSetPassword(String username, String password) {
        try {
            AdminSetUserPasswordResponse setUserPasswordResponse =
                            cognitoClient.adminSetUserPassword(AdminSetUserPasswordRequest.builder()
                                                                                          .username(username)
                                                                                          .password(password)
                                                                                          .permanent(true)
                                                                                          .userPoolId(properties.getUserPoolId())
                                                                                          .build());
            handleAWSSdkResponse(setUserPasswordResponse.sdkHttpResponse(),
                            String.format("Change password for %s on cognito side error.", username));
            log.info("Password for {} on cognito changed successfully.", username);
        } catch (InvalidPasswordException e) {
            log.error("Password does not conform to cognito policy.");
            throw new AWSInvalidPasswordException("Password does not conform to cognito policy: " +
                            "Min length 8, allowed uppercase letters, lowercase letters, special characters, numbers");
        } catch (Exception e) {
            throw new AwsClientException(String.format("Change password for %s on cognito side error.", username));
        }
    }

    @Override
    public void globalSignOut(String username) {
        try {
            AdminUserGlobalSignOutRequest request = AdminUserGlobalSignOutRequest.builder()
                                                                                 .username(username)
                                                                                 .userPoolId(properties.getUserPoolId())
                                                                                 .build();
            AdminUserGlobalSignOutResponse response = cognitoClient.adminUserGlobalSignOut(request);
            handleAWSSdkResponse(response.sdkHttpResponse(), String.format("Logout for user %s error.", username));
            log.info("Logout for {} on cognito successfully.", username);
        } catch (Exception e) {
            throw new AwsClientException(String.format("Logout for user %s error.", username), e);
        }
    }

    @Override
    public void createIdentityProvider(String idpName, ModifiableIdpModel modifiableIdp) {
        try {
            Map<String, String> providerDetails = ImmutableMap.of("MetadataURL", modifiableIdp.getMetadataUrl());
            Map<String, String> attributeMapping = ImmutableMap.of(EMAIL_ATTR_NAME, modifiableIdp.getEmailAttribute());
            CreateIdentityProviderRequest request = CreateIdentityProviderRequest.builder()
                    .attributeMapping(attributeMapping)
                    .userPoolId(properties.getUserPoolId())
                    .providerType(IdentityProviderTypeType.SAML)
                    .providerName(idpName)
                    .idpIdentifiers(idpName)
                    .providerDetails(providerDetails)
                    .build();
            CreateIdentityProviderResponse response = cognitoClient.createIdentityProvider(request);
            handleAWSSdkResponse(response.sdkHttpResponse(), "SSO Identity Provider creation error.");
            log.info("New SSO Identity Provider {} created successfully.", idpName);
        } catch (Exception e) {
            log.error("SSO Identity Provider {} creation error. {}", idpName, e.getMessage(), e);
            throw new AwsClientException(
                    String.format("SSO Identity Provider creation error. Reason: %s", e.getMessage()));
        }

    }

    public void assignIdpToClient(String idpName) {
        try {
            DescribeUserPoolClientResponse clientInfoResponse =
                    cognitoClient.describeUserPoolClient(DescribeUserPoolClientRequest.builder()
                            .clientId(properties.getSsoClientId())
                            .userPoolId(properties.getUserPoolId())
                            .build());
            UserPoolClientType userPoolInfo = clientInfoResponse.userPoolClient();
            List<String> supportedIdps = new ArrayList<>(userPoolInfo.supportedIdentityProviders());
            supportedIdps.add(idpName);
            UpdateUserPoolClientResponse updatedResponse =
                    cognitoClient.updateUserPoolClient(UpdateUserPoolClientRequest.builder()
                            .clientId(userPoolInfo.clientId())
                            .userPoolId(userPoolInfo.userPoolId())
                            .clientName(userPoolInfo.clientName())
                            .supportedIdentityProviders(supportedIdps)
                            .accessTokenValidity(userPoolInfo.accessTokenValidity())
                            .allowedOAuthFlows(userPoolInfo.allowedOAuthFlows())
                            .allowedOAuthFlowsUserPoolClient(userPoolInfo.allowedOAuthFlowsUserPoolClient())
                            .refreshTokenValidity(userPoolInfo.refreshTokenValidity())
                            .idTokenValidity(userPoolInfo.idTokenValidity())
                            .tokenValidityUnits(userPoolInfo.tokenValidityUnits())
                            .readAttributes(userPoolInfo.readAttributes())
                            .writeAttributes(userPoolInfo.writeAttributes())
                            .explicitAuthFlows(userPoolInfo.explicitAuthFlows())
                            .callbackURLs(userPoolInfo.callbackURLs())
                            .logoutURLs(userPoolInfo.logoutURLs())
                            .defaultRedirectURI(userPoolInfo.defaultRedirectURI())
                            .allowedOAuthScopes(userPoolInfo.allowedOAuthScopes())
                            .analyticsConfiguration(userPoolInfo.analyticsConfiguration())
                            .preventUserExistenceErrors(userPoolInfo.preventUserExistenceErrors())
                            .build());
            handleAWSSdkResponse(updatedResponse.sdkHttpResponse(), String.format("Assign Idp %s error.", idpName));
            log.info("Idp {} assigned to client successfully.", idpName);
        } catch (Exception e) {
            log.error("Assign Idp {} error.", idpName, e);
            throw new AwsClientException(
                    String.format("Assign Idp %s error.. Reason: %s", idpName, e.getMessage()));
        }

    }

    private void handleAWSSdkResponse(SdkHttpResponse response, String message) {
        if (!response.isSuccessful()) {
            throw new AwsClientException(message + String.format(" Response code: %s", response.statusCode()));
        }
    }
}
