package ua.alexlapada.model.cognito;

import lombok.Data;

@Data
public class ConfirmForgotPasswordModel {
    private String email;
    private String otp;
    private String newPassword;
}
