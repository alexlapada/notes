package ua.alexlapada.exception;

public class AWSInvalidPasswordException extends AwsClientException {

    public AWSInvalidPasswordException(String message) {
        super(message);
    }

    public AWSInvalidPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
