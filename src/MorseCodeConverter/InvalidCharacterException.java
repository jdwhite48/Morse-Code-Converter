package MorseCodeConverter;

/**
 * @author Jacob White
 * Thrown if the array does not contain character input.
 */
public class InvalidCharacterException extends Exception {
    String character;
    public InvalidCharacterException(String str) {
        super("Invalid Character: " + str);
        character = str;
    }
    
    /**
     * Returns the character (Morse or English) that TextToMorse threw when it couldn't find it in its array
     * @return character
     */
    public String getCharacter() {
        return character;
    }
    
}
