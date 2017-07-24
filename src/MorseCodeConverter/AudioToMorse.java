package MorseCodeConverter;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
/**
 * TODO:
 * Add the noise it catches to the array 
 * 
 * @author Jacob White
 */

public class AudioToMorse {
    
    //Audio samples per second
    private final int SAMPLE_RATE = 8000;
    //Estimated words per minute (based on 50 dots per word standard)
    private final int WPM = 20;
    //words per min * 50 dots per word * 1 min / 60 sec = dots per sec
    private int dps = WPM * 50/60;
    //Estimated noises per dot (a "noise" is a group of audio samples)
    private int NOISES_PER_DOT = 10;
    //Estimated number of noises per second
    private int noisesPerSec = dps * NOISES_PER_DOT;
    //The threshhold above which the program will recognize noise (max 127 for 8-bit samples)
    private final int SENSITIVITY = 6;
    private boolean stopped = true;
    
    /**
     * Listens for audio samples, captures groups of those samples ("noises"), 
     * determines whether it is loud enough to be considered sound being played,
     * and adds the result to a buffer. 
     * 
     * @param pc
     * @throws LineUnavailableException 
     */
    public void captureAudio(ProcessDotTime pc) throws LineUnavailableException {
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
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
        //A "noise" will be an estimated number of audio samples
        byte[] noise = new byte[SAMPLE_RATE / noisesPerSec];
        //Instantiate buffer that stores noise data for approx. 300 seconds (5 min)
        AudioBufferAnalysis isNoiseBuffer = new AudioBufferAnalysis(noisesPerSec*300);
        
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
                isNoiseBuffer.add(true);
            }
            else {
                isNoiseBuffer.add(false);
            }
            //While loop ends when another thread (AudioToTextWindow) calls setStopped(true);
        }
        line.stop();
        line.drain();
        line.close();
        String audioMessage = isNoiseBuffer.getNoiseList();
        double noisesPerDot = isNoiseBuffer.getNoisesPerDot(audioMessage);
        pc.setDuration((int)noisesPerDot);
        
        //System.out.println("Audio Message: " + audioMessage);
        System.out.println("Message: " + isNoiseBuffer.getMessage(audioMessage, noisesPerDot));
    }
    
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    public boolean isStopped() {
        return stopped;
    }
}
