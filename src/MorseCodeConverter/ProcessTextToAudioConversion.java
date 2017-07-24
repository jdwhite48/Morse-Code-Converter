package MorseCodeConverter;

import java.util.List;
import javax.swing.*;
import javax.sound.sampled.LineUnavailableException;

/** SwingWorker that handles appending text to outputText as the 
 * audio plays (converted in mta), and also prevents events from queueing 
 * (from clicking convertButton repeatedly) as it plays. 
 */
class ProcessTextToAudioConversion extends SwingWorker<Void, String> {

    private final JTextArea outputText;
    private final JButton convertButton;
    private final TextToAudio mta;
    private final String output;
    private final JFrame converterWindow;
    Exception exception = null;
    
    public ProcessTextToAudioConversion(String output, JFrame converterWindow, JButton convertButton, JTextArea outputText, TextToAudio mta) {
        this.convertButton = convertButton;
        this.outputText = outputText;
        this.mta = mta;
        this.output = output;
        this.converterWindow = converterWindow;
    }

    /**
     * Called from SwingWorker's execute(), plays audio in a separate thread from
     * GUI handling (in TextToAudioWindow) so that events from the convertButton
     * cannot perform it's action multiple times. 
     */
    @Override
    protected Void doInBackground() {
        convertButton.setEnabled(false);
        try {
            mta.playAudio(output, this);
        }
        catch (LineUnavailableException | InterruptedException | InvalidTransmissionException e) {
            
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
        convertButton.setEnabled(true);
        if (exception instanceof LineUnavailableException) {
            JOptionPane.showMessageDialog(converterWindow, "Unable to access audio line. Please try again", "Audio Unavailable", JOptionPane.ERROR_MESSAGE);
        }
        else if (exception instanceof InterruptedException) {
            JOptionPane.showMessageDialog(converterWindow, "Audio line interrupted. Please try again", "Audio Interrupted", JOptionPane.ERROR_MESSAGE);
        }
        else if (exception instanceof InvalidTransmissionException) {
            JOptionPane.showMessageDialog(converterWindow, "No Morse code message found. Remember to contain message with starting and ending signals.", "Invalid Transmission Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called from SwingWorker's publish method, adds each dot or dash to the 
     * output jTextArea and updates its display to show the text.
     * @param chunks 
     */
    @Override
    protected void process(List<String> chunks) {
        for (String s: chunks) {
            outputText.append(s);
        }
        //outputText.update(outputText.getGraphics());
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
