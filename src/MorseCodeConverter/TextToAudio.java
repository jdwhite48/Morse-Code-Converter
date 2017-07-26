package MorseCodeConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.*;

/**
 * Converts a Morse code String to audio signals, and outputs the Morse code (either to
 * console or to a jTextArea, depending on the parameters) as it plays.
 * 
 * 
 * @author Jacob White
 */

public class TextToAudio {
    private static int DOT_FREQ = 1000; // Dot frequency (Hz)
    private int WPM = 20; //Words per minute, at ~50 dot lengths per word
    private double volume = 1.0; //Default volume is 1.0
    private int dotLength = 1200 / WPM; // Unit of time for Morse (msec)
    private int dashLength = 3*dotLength; //Play dash for 3 dot lengths
    private int dotSpacing = dotLength; //Pause 1 dot length between dots or dashes
    private int letterSpacing = 3*dotSpacing; //Pause 3 dot lengths between letters
    private int wordSpacing = 7*dotSpacing; //Pause 7 dot lengths between words
    
    /**
     * Set the words per minute/playback speed 
     * (based on the convention of 50 dot lengths per word, e.g. PARIS or CODEX)
     * @param wpm
     */
    public void setWPM(int wpm) {
        WPM = wpm;
        
        //Update variables derived from WPM
        dotLength = 1200 / WPM;
        dashLength = 3*dotLength;
        dotSpacing = dotLength;
        letterSpacing = 3*dotSpacing;
        wordSpacing = 7*dotSpacing;
    }
    
    /***
     * Set the frequency of playback
     * @param freq 
     */
    public void setFrequency(int freq) {
        DOT_FREQ = freq;
    }
    
    /**
     * Sets the volume of playback
     * @param vol 
     */
    public void setVolume(double vol) {
        volume = vol;
    }
    
    public void playAudio(String input) throws LineUnavailableException, InterruptedException, InvalidTransmissionException {
        playAudio(input, null);
    }

    /**
     * Plays the audio from Morse code input (with starting & ending signals) and
     * outputs the morse code as it plays into a text area (via SwingWorker)
     * 
     * NOTE: Input null for the SwingWorker parameter if outputting to Terminal
     * 
     * @param input the Morse code String
     * @param vol The volume (0 is muted, 1 standard?)
     * @param ttap The SwingWorker that handles outputting text to the text area
     * @throws LineUnavailableException 
     * @throws java.lang.InterruptedException 
     * @throws MorseCodeConverter.InvalidTransmissionException 
     */
    public void playAudio(String input, TextToAudioProcessor ttap) throws LineUnavailableException, InterruptedException, InvalidTransmissionException {
        String msg = "";
        Matcher msgMatcher = Pattern.compile("(.*[^.\\-])?(-\\.-\\.- (.*?) \\.\\.\\.-\\.-)").matcher(input);
        if (msgMatcher.find())
        {
            //Takes the included Morse message in (.*?)
            msg = msgMatcher.group(2);
        }
        else {
            
            throw new InvalidTransmissionException();
        }
            
        char[] morseChars = msg.toCharArray();
        for (char c: morseChars) {
            if (ttap == null) {
                switch (c) {
                    case '.':
                        System.out.print(".");
                        PlaySoundUtils.tone(DOT_FREQ, dotLength, volume);
                        Thread.sleep(dotSpacing);
                        break;
                    case '-':
                        System.out.print("-");
                        PlaySoundUtils.tone(DOT_FREQ, dashLength, volume);
                        Thread.sleep(dotSpacing);
                        break;
                    case ' ':
                        System.out.print(" ");
                        Thread.sleep(letterSpacing - dotSpacing);
                        //Because there's a space after every dot or dash, it has to be taken baack out
                        break;
                    case '|':
                        System.out.print("|");
                        Thread.sleep(wordSpacing - dotSpacing);
                        break;
                    case '/':
                        System.out.print("/");
                        Thread.sleep(wordSpacing - dotSpacing);
                        break;
                    default:
                        break;
                }
            }
            else {
                switch (c) {
                    case '.':
                        ttap.outputText(".");
                        PlaySoundUtils.tone(DOT_FREQ, dotLength, volume);
                        Thread.sleep(dotSpacing);
                        break;
                    case '-':
                        ttap.outputText("-");
                        PlaySoundUtils.tone(DOT_FREQ, dashLength, volume);
                        Thread.sleep(dotSpacing);
                        break;
                    case ' ':
                        ttap.outputText(" ");
                        Thread.sleep(letterSpacing - dotSpacing);
                        //Because there's a space after every dot or dash, it has to be taken baack out
                        break;
                    case '|':
                        ttap.outputText("|");
                        Thread.sleep(wordSpacing - dotSpacing);
                        break;
                    case '/':
                        ttap.outputText("/");
                        Thread.sleep(wordSpacing - dotSpacing);
                        break;
                    default:
                        break;
                }
            }
            System.out.flush();
        }
    }
}
