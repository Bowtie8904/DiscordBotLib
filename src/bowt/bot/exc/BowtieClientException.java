package bowt.bot.exc;


/**
 * Thrown when the client was not built before discord methods were called.
 * 
 * @author &#8904
 */
public class BowtieClientException extends Exception{
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with a detail message.
     * 
     * @param message The message describing the cause of this exception.
     */
    public BowtieClientException(String message) {
        super(message);
    }

    /**
     * Create a new exception with a detail message.
     * 
     * @param message The message describing the cause of this exception.
     * @param e The exception causing this exception to be thrown.
     */
    public BowtieClientException(String message, Throwable e) {
        super(message, e);
    }
}