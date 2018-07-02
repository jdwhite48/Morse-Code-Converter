package MorseCodeConverter;

import javax.sound.sampled.*;

/**
 * https://stackoverflow.com/questions/3780406/how-to-play-a-sound-alert-in-a-java-application
 * 
 * BUG:
 * Crackling/Popping noise when a sound starts playing, fading tone doesn't 
 * completely fix
 * 
 * @author RealHowTo
 * @author Jacob White
 */
public class PlaySoundUtils {

  public static int SAMPLE_RATE = 8000;
  public static int fadeDur = 80;

  public static void tone(int hz, int msecs, SourceDataLine line) 
     throws LineUnavailableException, InterruptedException
  {
     tone(hz, msecs, 1.0, line);
  }

  /**
   * Plays a pure tone of a given frequency, duration, and volume with linear fading.
   * Throws LineUnavailableException if SourceDataLine cannot open.
   *
   * @param hz
   * @param msecs
   * @param vol
   * @throws LineUnavailableException 
   */
  public static void tone(int hz, int msecs, double vol, SourceDataLine line) throws LineUnavailableException,InterruptedException {
    long begin = new java.util.Date().getTime();
      byte[] buf = new byte[1];
    double duration = msecs*SAMPLE_RATE/1000; //duration in number of samples
    fadeDur=(int)Math.round(duration/20);
    for (double i = 0; i < duration; i++) {
        double fadeRate = 1.0; 
        if (i < fadeDur) { //Linear fade-in
            fadeRate = i / fadeDur;
        }
        else if (i > (duration - fadeDur)){ //Linear fade-out
            fadeRate = (duration - i) / fadeDur;
        }
        double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
        //127.0 to account for 8-bit audio sample for the line
        buf[0] = (byte) Math.round(Math.sin(angle) * 127.0 * vol * fadeRate);
        //line.start();
        line.write(buf, 0, 1); //byte array, byte offset, data byte
    }
    long end = new java.util.Date().getTime();
    if((msecs-(end-begin))>0){
        Thread.sleep(msecs-(end-begin));    
    }
    //line.drain();
    //line.stop();
  }
}
