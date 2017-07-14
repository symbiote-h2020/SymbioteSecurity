package eu.h2020.symbiote.security.exceptions.custom;

import eu.h2020.symbiote.security.constants.SecurityConstants;
import eu.h2020.symbiote.security.exceptions.SecurityException;
import org.springframework.http.HttpStatus;

/**
 * Custom exception thrown when JWT token creation fails
 *
 * @author Daniele Caldarola (CNIT)
 * @author Nemanja Ignjatov (UNIVIE)
 */
public class MalformedJWTException extends SecurityException {

    private static final long serialVersionUID = SecurityConstants.serialVersionUID;
    private final static String errorMessage = "UNABLE_MALFORMED_JWT_TOKEN";
    private final HttpStatus statusCode = HttpStatus.BAD_REQUEST;

    public MalformedJWTException() {
        super(errorMessage);
    }

    public MalformedJWTException(String message) {
        super(message);
    }

    public MalformedJWTException(Throwable cause) {
        super(cause);
    }

    public MalformedJWTException(String message, Throwable cause) {
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