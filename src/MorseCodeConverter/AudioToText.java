package MorseCodeConverter;

import javax.sound.sampled.*;
/**
 * TODO:
 * Handle English vs. Morse and finding the message.
 * 
 * @author Jacob White
 */

public class AudioToText {
    
    //50 dot units per word standard (e.g. PARIS)
    private final int DOTS_PER_WORD = 50;
    //Audio samples per second
    private final double SAMPLE_RATE = 8000;
    //Estimated words per minute
    private final int WPM = 20;
    //words per min * 50 dots per word * 1 min / 60 sec = dots per sec
    private final int DOTS_PER_SEC = WPM * DOTS_PER_WORD / 60;
    //Estimated noises per dot (a "noise" is a group of audio samples)
    private final int NOISES_PER_DOT = 12;
    //Estimated number of noises per second
    private final double NOISES_PER_SEC = DOTS_PER_SEC * NOISES_PER_DOT;
    //The threshhold above which the program will recognize noise (max 127 for 8-bit samples)
    private final int SENSITIVITY = 6;
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
        
        while (!stopped) {
            //Fill the noise buffer with samples from the line.
            int totalBytesRead = 0;
            int remainingBytes = noise.length;
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
     * Listens for audio samples, captures groups of those samples ("noises"), 
     * determines whether it is loud enough to be considered sound being played,
     * and adds the result to a buffer. Then it converts the audio into Morse
     * text 
     * 
     * @param attp
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
        //Different than class variable if WPM != 20;
        double noisesPerSec = WPM * NOISES_PER_DOT * DOTS_PER_WORD / 60;
        //A "noise" will be a number of bytes (1 sample = 8 bits = 1 byte)
        byte[] noise = new byte[(int)(SAMPLE_RATE / noisesPerSec)];
        //The time it takes to fill the "noise" array.
        double secPerNoise = 1 / noisesPerSec;
        //Instantiate buffer that stores noise data for 300 seconds (5 min)
        AudioAnalysisBuffer noiseBuffer = new AudioAnalysisBuffer(secPerNoise, (int)noisesPerSec*300);
        
        while (!stopped) {
            //Fill the noise buffer with samples from the line.
            int totalBytesRead = 0;
            int remainingBytes = noise.length;
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
            //While loop ends when another thread (AudioToTextWindow) calls setStopped(true);
        }
        line.stop();
        line.drain();
        line.close();
        String morseMessage = noiseBuffer.getMorse(WPM);
        attp.outputText(morseMessage);
    }
}
