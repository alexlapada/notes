package ua.alexlapada.model.cognito;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForceChangePasswordModel {

    private String confirmPassword;
    private String newPassword;
    private String currentPassword;
    private String username;

}
