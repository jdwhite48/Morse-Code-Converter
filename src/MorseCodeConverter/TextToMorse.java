package MorseCodeConverter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given an input string, converts to text to Morse code (or vice versa), and
 * outputs the message.
 * 
 * NOTE: Corresponding entries to the letters and Morse arrays should have the 
 * same index. Uncomment displayArrays() in toString() method to see how they pair up.
 * 
 * BUG FIXES: 
 * first .-.-.- (".") is interpreted as starting signal (-.-.-) if there is none
 * 
 * @author Jacob White
 */
public class TextToMorse {
    //These arrays MUST have the same number of indices (use displayArrays() for debug, commented in toString())
    String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", 
        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", 
        "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", ",", "?", 
        "\'", "!", "/", "(", ")", "&", ":", ";", "=", "+", "-", "_", "\"", "$", 
        "@", " ", " ", "\n"};
    String[] morse = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", 
        "..", ".---", "-.-", ".-..",  "--", "-.", "---", ".--.", "--.-", ".-.", 
        "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", "-----", 
        ".----", "..---", "...--",  "....-", ".....", "-....", "--...", "---..",
        "----.", ".-.-.-", "--..--", "..--..",".----.", "-.-.--", "-..-.", 
        "-.--.", "-.--.-", ".-...", "---...", "-.-.-.", "-...-", ".-.-.", 
        "-....-", "..--.-", ".-..-.", "...-..-", ".--.-.", "|", "/", ".-.-"};
    
    //Starting and ending transmission signals for Morse code (DO NOT CHANGE, will break msgMatcher)
    String start = "-.-.-";
    String end = "...-.-";
    
/**
 * Converts a character from text to Morse code.
 * 
 * @param str String of character
 * @return String of Morse code or error
 * @throws MorseCodeConverter.InvalidCharacterException if letters array doesn't 
 * contain character/Morse combo
 */
public String charToMorse(String str) throws InvalidCharacterException {
    int letterIndex = Arrays.asList(letters).indexOf(str);
    if (letterIndex >= 0) {
        String m = morse[letterIndex];
        return m;
    }
    else {
        throw new InvalidCharacterException(str);
    }
}

/**
 * Converts a word from Morse code to text.
 * 
 * @param word a "word" of Morse code
 * @return a word in English
 * @throws MorseCodeConverter.InvalidCharacterException if Morse array doesn't
 * contain character/Morse combination.
 */
public String morseToWord(String word) throws InvalidCharacterException {
    String[] lettersInWord = word.split(" +");
    String out = "";
    for (String letter: lettersInWord) {
        int morseIndex = Arrays.asList(morse).indexOf(letter);
        
        //If the index is valid (Morse letter is in the morse array)
        if (morseIndex != -1) {
            out += letters[morseIndex];
        }
        else {
            throw new InvalidCharacterException(letter);
        }
    }
    return out;
    
}

     /**
     * Evaluates whether the input string is text or Morse code & converts message
     * 
     * @param input the text to be converted to Morse code
     * @return Morse code message
     * @throws MorseCodeConverter.InvalidCharacterException
     * @throws MorseCodeConverter.InvalidArraysException
     * @throws MorseCodeConverter.InvalidTransmissionException if starting and ending signals aren't present and placed correctly
     */
    public String toString(String input) throws InvalidTransmissionException, InvalidCharacterException, InvalidArraysException {
        checkArrays();
        //Uncomment to show how each letter matches which morse character
//        displayArrays();
        String transmission = input.toUpperCase();
        
        //If the input doesn't just contain one or more morse characters (|/.- and whitespace)
        String textPattern = "[^\\-\\s.\\|/]";
        boolean isText = Pattern.compile(textPattern).matcher(transmission).find(); 
        if (!isText) {
            //Replace each instance of one or more whitespace
            transmission= transmission.replaceAll("\\s+", " ");
            //Pattern to find the message between starting/ending signals
            Matcher msgMatcher = Pattern.compile(".*([^.\\-]?)-\\.-\\.- (.*?) \\.\\.\\.-\\.-").matcher(transmission);
            if (msgMatcher.find()) {
                //Takes the included Morse message and converts to text
                String output = morseToText(msgMatcher.group(2));
                return output;
            }
            else {
                throw new InvalidTransmissionException();
            }
        }
        else { //input is text
            String output = textToMorse(transmission);
            return output;
        }
    }
    
    /**
     * Checks to see if the letters and Morse arrays are of the same length.
     * If not, there is an array mismatch.
     * 
     * @throws InvalidArraysException 
     */
    public void checkArrays() throws InvalidArraysException {
        if (letters.length != morse.length) {
            throw new InvalidArraysException(letters.length, morse.length);
        }
    }
    
    /**
     * Debug method that displays a list of corresponding elements in the 
     * letters and morse arrays. Returns error if unequal indices.
     * @throws InvalidArraysException 
     */
    public void displayArrays() throws InvalidArraysException {
        checkArrays();
        String list = "Text <-> Morse Code\n";
        for (int i = 0; i < letters.length; i++) {
            switch (letters[i]) {
                case "\n":
                    list += "New Line (\\n)" + " <-> " + morse[i] + "\n";
                    break;
                case " ":
                    list += "\" \"" + " <-> " + "\" \"" + "\n";
                    break;
                default:
                    list += letters[i] + " <-> " + morse[i] + "\n";
                    break;
            }
        }
        System.out.println(list);
    }
    /**
     * For each character in the array, attempt to convert text to Morse code.
     * Throw error if invalid character.
     * @param text the message string
     * @return String output (morse code)
     * @throws InvalidCharacterException 
     */
    public String textToMorse(String text) throws InvalidCharacterException {
        char[] charArray = text.toCharArray();
        String output = start + " ";
        for(char c: charArray) {
            output += charToMorse("" + c);
            output += " "; //add spaces between "letters" of Morse code
        }
        output += end; // End of Transmission Signal
        return output;
    }
    
    /**
     * For each string of Morse characters, attempt to convert Morse code to text.
     * Throw error if invalid character
     * @param s (Morse code message as String)
     * @return output (text as String)
     * @throws InvalidCharacterException 
     */
    public String morseToText(String s) throws InvalidCharacterException {
        String output = "";
        String[] morseWords = s.split("\\s*[|/]//s*");
        for (int i = 0; i < morseWords.length; i++) {
            if (i != 0) {
                output += " ";
            }
            output += morseToWord(morseWords[i]);
        }
        return output;
    }
}

