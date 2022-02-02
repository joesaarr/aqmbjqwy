package bank.exception;

import lombok.Data;

import java.util.List;

/**
 * Error response for http requests
 */
@Data
public class ErrorResponse {

    private String message;
    private List<String> details;

    /**
     * Error response constructor
     * @param message error message
     * @param details error details
     */
    public ErrorResponse(String message, List<String> details) {
        this.message = message;
        this.details = details;
    }

}
