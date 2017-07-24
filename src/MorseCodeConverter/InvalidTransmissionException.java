package MorseCodeConverter;

/**
 * Thrown if the Morse code input does not contain a starting and ending signal.
 * 
 * @author Jacob White
 */
public class InvalidTransmissionException extends Exception {
    public InvalidTransmissionException() {
        super("Incorrect placement of starting and ending signals");
    }
}
