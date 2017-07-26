package MorseCodeConverter;

import java.util.List;
import javax.swing.*;
import javax.sound.sampled.LineUnavailableException;

/** SwingWorker that handles publishing the calculated unit time of the morse code
 * as it interprets the incoming audio.
 * 
 * TODO:
 * Rework into publishing global value for dot unit of time
 */
class WPMProcessor extends SwingWorker<Void, Integer> {

    private final JSpinner WPMSpinner;
    private final JButton startButton;
    private final AudioToText atm;
    private final JFrame converterWindow;
    Exception exception = null;
    
    public WPMProcessor(JFrame converterWindow, JButton startButton, JSpinner WPMSpinner, AudioToText atm) {
        this.startButton = startButton;
        this.WPMSpinner = WPMSpinner;
        this.atm = atm;
        this.converterWindow = converterWindow;
    }

    @Override
    protected Void doInBackground() {
        startButton.setEnabled(false);
        try {
            atm.captureAudio(this);
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

    @Override
    protected void process(List<Integer> chunks) {
        for (int i: chunks) {
            WPMSpinner.setValue(i);
        }
    }
    
    /**
     * Updates the WPM shown on the jTextField passed into this object.
     * @param wpm 
     */
    public void setWPM(int wpm) {
        publish(wpm);
    }
}
