package ua.alexlapada.exception;

public class AWSConfirmForgotPasswordException extends AwsClientException {

    public AWSConfirmForgotPasswordException(String message) {
        super(message);
    }

    public AWSConfirmForgotPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
