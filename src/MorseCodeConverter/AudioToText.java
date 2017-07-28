package MorseCodeConverter;

import javax.sound.sampled.*;
/**
 * TODO:
 * Handle English vs. Morse and finding the message.
 * 
 * @author Jacob White
 */

public class AudioToText {
    
    //Audio samples per second
    private final double SAMPLE_RATE = 8000;
    //The threshhold above which the program will recognize noise (max 127 for 8-bit samples)
    private final int SENSITIVITY = 12;

    //Estimated noises per dot for 20 WPM (a "noise" is a group of audio samples)
    private final int NOISES_PER_DOT = 10;
    //50 dot units per word standard (e.g. PARIS)
    private final int DOTS_PER_WORD = 50;
    //Estimated words per minute
    private final int WPM = 20;
    //words per min * 50 dots per word * 1 min / 60 sec = dots per sec
    private final int DOTS_PER_SEC = WPM * DOTS_PER_WORD / 60;
    //Estimated number of noises per second
    private final double NOISES_PER_SEC = DOTS_PER_SEC * NOISES_PER_DOT;
    private boolean stopped = true;
    
    /**
     * Listens for audio samples, captures groups of those samples ("noises"), 
     * determines whether it is loud enough to be considered sound being played,
     * and adds the result to a buffer. Then it displays the words-per-minute
     * that it calculates from the buffer's contents.
     * 
     * @param wpmp
     * @throws LineUnavailableException 
     */
    public void captureAudio(WPMProcessor wpmp) throws LineUnavailableException {
        AudioFormat af = new AudioFormat((float)SAMPLE_RATE, 8, 1, true, false);
        TargetDataLine line;
        DataLine.Info dli = new DataLine.Info(TargetDataLine.class, af);
        try {
            line = (TargetDataLine) AudioSystem.getLine(dli);
            line.open(af);
        }
        catch (LineUnavailableException e) {
            throw e;
        }
        line.start();
        //A "noise" will be an estimated number of bytes (1 sample = 8 bits = 1 byte)
        byte[] noise = new byte[(int)(SAMPLE_RATE / NOISES_PER_SEC)];
        //The time it takes to fill the "noise" array.
        double secPerNoise = 1 / NOISES_PER_SEC;
        //Instantiate buffer that stores noise data for approx. 300 seconds (5 min)
        AudioAnalysisBuffer noiseBuffer = new AudioAnalysisBuffer(secPerNoise,(int)NOISES_PER_SEC*300);
        
        
        boolean isFirstByte = true;
        while (!stopped) {
            int totalBytesRead = 0;
            int remainingBytes = noise.length;
            //Only starts collecting data when starts playing sound
            if (isFirstByte) {
                noise[0] = listenForFirstByte(line);
                totalBytesRead++;
                remainingBytes--;
                isFirstByte = false;
            }
            //Fill the noise buffer with samples from the line
            while ((remainingBytes > 0) && !stopped) {
                int numBytesRead = line.read(noise, totalBytesRead, remainingBytes);
                totalBytesRead += numBytesRead;
                remainingBytes -= numBytesRead;
            }
            //Find on average how loud the "noise" was
            int byteSum = 0;
            for (byte b: noise) {
                byteSum += Math.abs(b);
            }
            int byteAvg = byteSum/noise.length;
            //Determine whether each "noise" is loud enough & store in buffer
            if (byteAvg > SENSITIVITY) {
                noiseBuffer.add(true);
            }
            else {
                noiseBuffer.add(false);
            }
            wpmp.setWPM(noiseBuffer.getWPM());
            //While loop ends when another thread (currently AudioToTextWindow) calls setStopped(true);
        }
        line.stop();
        line.drain();
        line.close();
    }
    
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    public boolean isStopped() {
        return stopped;
    }
    
    /**
     * Read a group of audio samples (i.e. bytes) from the line, not capturing data until the bytes avg
     * above the sensitivity threshold. Return the avg of that group of bytes.
     * @param line
     * @return 
     */
    private byte listenForFirstByte(TargetDataLine line) {
        byte avgByte = 0;
        byte[] buff = new byte[15];
        while (!stopped) {
            int remainingBytes = buff.length;
            int totalBytesRead = 0;
            while ((remainingBytes > 0) && !stopped) {
                int numBytesRead;
                numBytesRead = line.read(buff, totalBytesRead, remainingBytes);
                totalBytesRead += numBytesRead;
                remainingBytes -= numBytesRead;
            }
            int byteSum = 0;
            for (byte b: buff) {
                byteSum += Math.abs(b);
            }
            int byteAvg = byteSum/buff.length;
            if (byteAvg > SENSITIVITY) {
                avgByte = (byte)byteAvg;
                break;
            }
        }
        return avgByte;
    }

/**
     * Listens for audio samples, captures groups of those samples ("noises"), 
     * determines whether it is loud enough to be considered sound being played,
     * and adds the result to a buffer. Then it converts the audio into Morse
     * text 
     * 
     * @param attp
     * @param WPM The words-per-minute that the message has been determined to play at.
     * @throws LineUnavailableException 
     */
    public void captureAudio(AudioToTextProcessor attp, int WPM) throws LineUnavailableException {
        AudioFormat af = new AudioFormat((float)SAMPLE_RATE, 8, 1, true, false);
        TargetDataLine line;
        DataLine.Info dli = new DataLine.Info(TargetDataLine.class, af);
        try {
            line = (TargetDataLine) AudioSystem.getLine(dli);
            line.open(af);
        }
        catch (LineUnavailableException e) {
            throw e;
        }
        line.start();
        //(60 sec/min) / (WPM words/min)*(50 dot/word) = 1.2 / WPM sec/dot
        double secPerDot = 60.0 / (DOTS_PER_WORD * WPM);
        //(1.2 / WPM sec/dot) * SAMPLE_RATE bytes/sec = 1.2 * SAMPLE_RATE/WPM byte/dot
        double bytesPerDot = SAMPLE_RATE * secPerDot;
        //bytes that make up a dot unit (1 sample = 8 bits = 1 byte)
        byte[] dotUnit = new byte[(int)bytesPerDot];
        
        //String that will contain "." if noise and " " if not, each character representing a dot unit in length.
        String audioMessage = "";
        
        boolean isFirstByte = true;
        while (!stopped) {
            int totalBytesRead = 0;
            int remainingBytes = dotUnit.length;
            //Only start collecting data when sound starts playing
            if (isFirstByte) {
                dotUnit[0] = listenForFirstByte(line);
                totalBytesRead++;
                remainingBytes--;
                isFirstByte = false;
            }
            //Make sure the byte array has been filled.
            while ((remainingBytes > 0) && !stopped) {
                int numBytesRead = line.read(dotUnit, totalBytesRead, remainingBytes);
                totalBytesRead += numBytesRead;
                remainingBytes -= numBytesRead;
            }
            //Find on average how loud the audio was within the dotUnit
            int byteSum = 0;
            for (byte b: dotUnit) {
                byteSum += Math.abs(b);
            }
            int byteAvg = byteSum/dotUnit.length;
            //Determine whether each dot unit is loud enough to be considered a sound
            if (byteAvg > SENSITIVITY) {
                audioMessage += ".";
            }
            else {
                audioMessage += " ";
            }
            //While loop ends when another thread (AudioToTextWindow) calls setStopped(true);
        }
        line.stop();
        line.drain();
        line.close();
        System.out.println(audioMessage);
        String morseMessage = "";
        
        String[] tonesAndSpaces = audioMessage.split("((?<= )(?=\\.))|((?<=\\.)(?= ))");
        for (String s: tonesAndSpaces) {
            if (s.matches("\\.+")) { //If tone
                switch (s.length()) {
                    case 1:
                        //If dot
                        morseMessage += ".";
                        break;
                    case 3:
                        //If dash
                        morseMessage += "-";
                        break;
                    default:
                        System.out.println("Not a tone:" + "\"" + s + "\"");
                        break;
                }
            }
            else if (s.matches(" +")) { //If space
                switch (s.length()) {
                    case 1:
                        //If space between tones
                        morseMessage += "_";
                        break;
                    case 3:
                        //If space between letters
                        morseMessage += " ";
                        break;
                    case 7:
                        //If space between words
                        morseMessage += "|";
                        break;
                    default:
                        System.out.println("Not a space:" + "\"" + s + "\"");
                        break;
                }
            }
            else {
                System.out.println("Not a tone or space:" + "\"" + s + "\"");
            }
        }
        attp.outputText(morseMessage);
    }
}
