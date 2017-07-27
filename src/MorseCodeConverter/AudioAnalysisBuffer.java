package MorseCodeConverter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Stores the buffer for AudioToText that tells whether there is noise in a group
 * of audio samples (each represented by 1 boolean). Throws out old data if 
 * buffer size exceeded.
 * 
 * TODO: 
 * Throw out outliers before using toneAvg
 * Ignore the occasional noises/spaces before the message
 * 
 * 
 * 
 * @author Jacob White
 */
public class AudioAnalysisBuffer {
    private final int MAX_SIZE;
    //Standard Morse code estimation
    private final int dotsPerWord = 50;
    
    private final double secondsPerNoise;
    ArrayList<Boolean> isNoiseList = new ArrayList<Boolean>();
    
    public AudioAnalysisBuffer(double secondsPerNoise, int maxElements) {
        MAX_SIZE = maxElements;
        this.secondsPerNoise = secondsPerNoise;
    }
    
    /**
     * Checks to see if the list contains the maximum number of elements. 
     * If it does, it throws out the oldest data element (preventing it from
     * running out of memory)
     */
    public void checkSize() {
        if (isNoiseList.size() >= MAX_SIZE) { 
            isNoiseList.remove(0);
        }
    }
    
    public int size() {
        return isNoiseList.size();
    }
    
    /**
     * Calculates how fast the data was input into the buffer and performs unit
     * conversions to obtain the words per minute (using Morse code standards)
     * @return 
     */
    public int getWPM() {
        String noiseList = getNoiseList();
        double noisesPerDot = getNoisesPerDot(noiseList);
        double dotsPerSec = (1 / secondsPerNoise) * (1 / noisesPerDot);
        int wpm = (int)Math.round((dotsPerSec * 60) / dotsPerWord);
        return wpm;
    }
    
    /**
     * Checks to see that the buffer isn't too large, then adds the given 
     * element into the ArrayList.
     * @param b 
     */
    public void add(boolean b) {
        checkSize();
        isNoiseList.add(b);
        
    }
    

    /**
     * Returns the contents of the buffer as a "." if true and " " if false
     * after it patched out minor inconsistencies in sound.
     * @return output: the string representing the buffer's contents.
     */
    private String getNoiseList() {
        String output = "";
        for (int i = 0; i < isNoiseList.size(); i++) {
            boolean b = isNoiseList.get(i);
            if (b == true) {
                output += ".";
            }
            else {
                output += " ";
            }
        }
        //Fills in short silences with noise & vice versa (filtering out crackles & such)
        Matcher noiseMatcher1 = Pattern.compile("\\. \\.").matcher(output);
        output = noiseMatcher1.replaceAll("...");
        Matcher noiseMatcher2 = Pattern.compile("\\. {2}\\.").matcher(output);
        output = noiseMatcher2.replaceAll("....");
        Matcher silenceMatcher1 = Pattern.compile(" \\. ").matcher(output);
        output = silenceMatcher1.replaceAll("   ");
        Matcher silenceMatcher2 = Pattern.compile(" \\.{2} ").matcher(output);
        output = silenceMatcher2.replaceAll("    ");
        //Remove empty space before/after message.
        output = output.trim();
        return output;
    }
    
    /**
     * Determines which 
     * @param noiseList
     * @return 
     */
    private double getNoisesPerDot(String noiseList) {
        String[] tones = noiseList.split(" +");
        ArrayList<Integer> toneLengths = new ArrayList<Integer>();
        for (String tone : tones) {
            toneLengths.add(tone.length());
        }
        //Assuming no outliers
        double toneAvg = getAverage(toneLengths);
        
        ArrayList<Integer> dotLengths = new ArrayList<Integer>();
        for (int i = 0; i < toneLengths.size(); i++) {
            //Ignore all the entries above avg, assuming all shorter tones are dots.
            int length = toneLengths.get(i);
            if ((length < toneAvg) && (length != 0)) {
                dotLengths.add(length);
            }
        }
        double dotAvg = getAverage(dotLengths);
        return dotAvg;
    }
    
    /**
     * Returns the average of the integers in an ArrayList.
     * @param arr
     * @return 
     */
    private double getAverage(ArrayList<Integer> arr) {
        int sum = 0;
        for (int i = 0; i < arr.size(); i++) {
            sum += arr.get(i);
        }
        double avg = (double) sum / arr.size();
        return avg;
    }
    
    /**
     * Returns the message based on the length of the tones it heard.
     * @param WPM
     * @return 
     */
    public String getMorse(int WPM) {
        double dotsPerNoise = WPM * secondsPerNoise / 60.0 * dotsPerWord;
        double noisesPerDot = 1 / dotsPerNoise;
        double TOLERANCE = noisesPerDot / 3;
        
        String morseMessage = "";
        String audioMessage = getNoiseList();
        //Split between the tones and the spaces
        String[] tonesAndSpaces = audioMessage.split("((?<= )(?=\\.))|((?<=\\.)(?= ))");
        for (String s: tonesAndSpaces) {
            if (s.matches("\\.+")) { //If tone
                if (Math.abs(s.length() - noisesPerDot*1) < TOLERANCE) {//If dot
                    morseMessage += ".";
                }
                else if (Math.abs(s.length() - noisesPerDot*3) < 3*TOLERANCE) {//If dash
                    morseMessage += "-";
                }
                else {
                    System.out.println("Not a tone: " + "\"" + s + "\"");
                }
            }
            else if (s.matches(" +")) { //If space
                if (Math.abs(s.length() - noisesPerDot*1) < 3*TOLERANCE) {//If tone space
                    morseMessage += "_";
                }
                else if (Math.abs(s.length() - noisesPerDot*3) < 2*TOLERANCE) { //If letter space
                    morseMessage += " ";
                }
                else if (Math.abs(s.length() - noisesPerDot*7) < 12*TOLERANCE) {//If word space
                    morseMessage += "|";
                }
                else {
                    System.out.println("Not a space: " + "\"" + s + "\"");
                }
            }
            else {
                System.out.println("Not a tone/space: " + "\"" + s + "\"");
            }
        }
        return morseMessage;
    }
}