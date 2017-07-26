package MorseCodeConverter;

/**
 * Thrown if the morse and letters arrays are not the same length
 * 
 * @author Jacob White
 */

public class InvalidArraysException extends Exception {
    private int lettersLength = 0;
    private int morseLength;

    public InvalidArraysException(int arr1Length, int arr2Length) {
        super("Indices in the letters and morse arrays do not match.\nletters: " + arr1Length + "\nmorse: " + arr2Length);
        lettersLength = arr1Length;
        morseLength = arr2Length;
    }
    /**
     * Returns the length of the letters array (in TextToMorse) that caused the exception to be thrown
     * @return lettersLength
     */
    public int getLettersLength() {
        return lettersLength;
    }
    
    /**
     * Returns the length of the morse array (in TextToMorse) that caused the exception to be thrown
     * @return morseLength
     */
    public int getMorseLength() {
        return morseLength;
    }
}
