package ua.alexlapada.model.cognito;

import lombok.Data;

@Data
public class ChangePasswordModel {
    private String accessToken;
    private String oldPassword;
    private String newPassword;
}
