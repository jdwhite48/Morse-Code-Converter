package MorseCodeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Stores the buffer for AudioToText that tells whether there is noise in a group
 * of audio samples (each represented by 1 boolean). Throws out old data if 
 * buffer size exceeded.
 * 
 * TODO: 
 * Throw out outliers before using toneAvg
 * Initially some spacce before
 * 
 * 
 * 
 * @author Jacob White
 */
public class AudioBufferAnalysis {
    //Noise factor the tone/space can deviate from
    private double TOLERANCE = 4;
    private final int MAX_SIZE;
    ArrayList<Boolean> isNoiseList = new ArrayList<Boolean>();
    
    public AudioBufferAnalysis(int elements) {
        MAX_SIZE = elements;
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
    public String getNoiseList() {
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
     * 
     * @param noiseList
     * @return 
     */
    public double getNoisesPerDot(String noiseList) {
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
     * Applies Morse code rules to determine space/tone type,
     * and from that converts it into a message.
     * @param noiseList String representation of when noises are playing
     * @param noisesPerDot How many noises are considered a dot
     * @return 
     */
    public String getMessage(String noiseList, double noisesPerDot) {
        int dotLength = (int)Math.round(noisesPerDot); //Noise length for 1 dot
        int dashLength = 3*dotLength; //Noise length for 1 dash
        int toneSpacing = dotLength; //Pause 1 dot length between dots or dashes
        int letterSpacing = 3*toneSpacing; //Pause 3 dot lengths between letters
        int wordSpacing = 7*toneSpacing; //Pause 7 dot lengths between words
        TOLERANCE = noisesPerDot / 3;
        
        //Trims empty space before/after so it won't be considered a space in message
        noiseList = noiseList.trim();
        //Split where it changes from "." to " "  or vice versa
        String[] tonesAndSpaces = noiseList.split("(?<=\\.)(?= )|(?<= )(?=\\.)");
        String message = "";
        for (String s : tonesAndSpaces) {
            if (s.matches("\\.+")) { //If tone
                if (Math.abs(s.length() - dotLength) < TOLERANCE) { //If dot
                    message += ".";
                }
                else if (Math.abs(s.length() - dashLength) < 3*TOLERANCE) { //If dash
                    message += "-";
                }
//                else {
//                    System.out.println("Not a morse tone: " + s);
//                }
            }
            else if (s.matches(" +")) { //If space
                if (Math.abs(s.length() - toneSpacing) < 3*TOLERANCE) { //If tone space
                    message += "_";
                }
                else if (Math.abs(s.length() - letterSpacing) < 2*TOLERANCE) { //If letter space
                    message += " ";
                }
                else if (Math.abs(s.length() - wordSpacing) < 12*TOLERANCE) { //If word space
                    message += "|";
                }
                else {
                    System.out.println("Not a morse space: " + "\"" + s + "\"");
                }
            }
//            else {
//                System.out.println("Not a tone/space: " + s);
//            }
        }
        return message;
    }
}
