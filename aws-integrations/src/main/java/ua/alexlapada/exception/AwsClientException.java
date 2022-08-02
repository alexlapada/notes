package ua.alexlapada.exception;

public class AwsClientException extends RuntimeException {
    public AwsClientException(String msg) {
        super(msg);
    }

    public AwsClientException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
