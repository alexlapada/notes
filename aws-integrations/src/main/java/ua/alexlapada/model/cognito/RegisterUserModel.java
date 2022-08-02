package ua.alexlapada.model.cognito;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserModel {
    public static final String DEFAULT_PASSWORD = "P@ssw0rd!";

    private String userName;
    private String password;
    private String email;

}
