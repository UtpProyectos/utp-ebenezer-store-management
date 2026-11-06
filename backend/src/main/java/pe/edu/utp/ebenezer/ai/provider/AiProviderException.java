package pe.edu.utp.ebenezer.ai.provider;

/**
 * A provider could not answer. The message is safe to log: it never contains prompts, answers or keys.
 */
public class AiProviderException extends RuntimeException {

    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
