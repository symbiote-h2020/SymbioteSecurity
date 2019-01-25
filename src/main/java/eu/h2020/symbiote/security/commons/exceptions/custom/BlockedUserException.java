package eu.h2020.symbiote.security.commons.exceptions.custom;

import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.exceptions.SecurityException;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when user is blocked due to anomaly detection.
 *
 * @author Piotr Jakubowski (PCSS)
 */
public class BlockedUserException extends SecurityException {

    public static final String errorMessage = "User account is not active. It could've have been blocked for suspicious activity or still needs to be activated.";
    private static final long serialVersionUID = SecurityConstants.serialVersionUID;
    private final static HttpStatus statusCode = HttpStatus.FORBIDDEN;

    public BlockedUserException() {
        super(errorMessage);
    }

    public BlockedUserException(String message) {
        super(message);
    }

    public BlockedUserException(Throwable cause) {
        super(cause);
    }

    public BlockedUserException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

}