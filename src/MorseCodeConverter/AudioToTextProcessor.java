package MorseCodeConverter;

import java.util.List;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;

/**
 *
 * @author Jacob White
 */
public class AudioToTextProcessor extends SwingWorker<Void, String>{
    private final JFrame converterWindow;
    private final JButton startButton;
    private final JTextArea outputText;
    private final AudioToText att;
    private final int WPM;
    Exception exception = null;
    
    public AudioToTextProcessor(JFrame converterWindow, JButton startButton, JSpinner WPMSpinner, JTextArea outputText, AudioToText att) {
        this.converterWindow = converterWindow;
        this.startButton = startButton;
        this.outputText = outputText;
        this.att = att;
        WPM = (int)WPMSpinner.getValue();
    }
    
    /**
     * Called from SwingWorker's execute(), plays audio in a separate thread from
     * GUI handling (in TextToAudioWindow) so that events from the convertButton
     * cannot perform it's action multiple times. 
     */
    @Override
    protected Void doInBackground() {
        startButton.setEnabled(false);
        try {
            att.captureAudio(this, WPM);
        }
        catch (LineUnavailableException e) {
            
            exception = e;
        }
        return null;
    }
    
        /**
     * Called from SwingWorker's execute(), re-enables the button and displays
     * any errors that may have been invoked from the playAudio
     */
    @Override
    protected void done() {
        startButton.setEnabled(true);
        if (exception instanceof LineUnavailableException) {
            JOptionPane.showMessageDialog(converterWindow, "Unable to access audio line. Please try again", "Audio Unavailable", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Called from SwingWorker's publish method, adds each dot or dash to the 
 input jTextArea and updates its display to show the text.
     * @param chunks 
     */
    @Override
    protected void process(List<String> chunks) {
        for (String s: chunks) {
            outputText.append(s);
        }
    }
    
    /**
     * Renamed SwingWorker's publish method for better readability, 
     * calls process method 
     * @param text 
     */
    public void outputText(String text) {
        publish(text);
    }
}
