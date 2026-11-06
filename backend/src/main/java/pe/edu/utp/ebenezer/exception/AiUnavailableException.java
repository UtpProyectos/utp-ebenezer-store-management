package pe.edu.utp.ebenezer.exception;

/**
 * No AI provider could answer (none configured, or all of them failed). Mapped to 503.
 * The message is for the logs; the client receives a generic one.
 */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message) {
        super(message);
    }
}
