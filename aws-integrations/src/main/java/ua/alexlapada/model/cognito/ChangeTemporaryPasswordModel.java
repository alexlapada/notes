package ua.alexlapada.model.cognito;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangeTemporaryPasswordModel {

    private String userName;
    private String newPassword;
    private String challengeName;
    private String sessionId;
}
