package ua.alexlapada.integration;

import ua.alexlapada.model.cognito.ChangePasswordModel;
import ua.alexlapada.model.cognito.ChangeTemporaryPasswordModel;
import ua.alexlapada.model.cognito.ConfirmForgotPasswordModel;
import ua.alexlapada.model.cognito.InitAuthModel;
import ua.alexlapada.model.cognito.ModifiableIdpModel;
import ua.alexlapada.model.cognito.RegisterUserModel;

import java.util.Optional;

public interface AwsCognitoClient {

    String registerUser(RegisterUserModel registerUserModel);

    void changeTemporaryPassword(ChangeTemporaryPasswordModel changeTemporaryPasswordModel);

    void changePassword(ChangePasswordModel changePasswordModel);

    void forgotPassword(String email);

    void confirmForgotPassword(ConfirmForgotPasswordModel model);

    String registerUserWithTempPassword(RegisterUserModel registerUserModel);

    InitAuthModel initAuth(String username, String password);

    InitAuthModel initAuthWithRefreshToken(String refreshToken);

    Optional<InitAuthModel> initAuthOptional(String username, String password);

    boolean existsUserByEmail(String email);

    void adminSetPassword(String username, String paswword);

    void globalSignOut(String username);

    void createIdentityProvider(String idpName, ModifiableIdpModel modifiableIdp);

    void assignIdpToClient(String idpName);
}
