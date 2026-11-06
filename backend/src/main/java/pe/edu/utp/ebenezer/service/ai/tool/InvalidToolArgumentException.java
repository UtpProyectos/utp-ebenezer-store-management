package pe.edu.utp.ebenezer.service.ai.tool;

/** Invalid arguments sent by the model. Returned to the model as an error, never to the client. */
public class InvalidToolArgumentException extends RuntimeException {

    public InvalidToolArgumentException(String message) {
        super(message);
    }
}
