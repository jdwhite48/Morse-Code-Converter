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
class ProcessDotTime extends SwingWorker<Void, Integer> {

    private final JTextField dotDurationField;
    private final JButton startButton;
    private final AudioToMorse atm;
    private final JFrame converterWindow;
    Exception exception = null;
    
    public ProcessDotTime(JFrame converterWindow, JButton startButton, JTextField dotDurationField, AudioToMorse atm) {
        this.startButton = startButton;
        this.dotDurationField = dotDurationField;
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

//    /**
//     * Called from SwingWorker's publish method, adds each dot or dash to the 
//     * output jTextArea and updates its display to show the text.
//     * @param chunks 
//     */
//    @Override
//    protected void process(List<String> chunks) {
//        for (String s: chunks) {
//            outputText.append(s);
//        }
//    }
    @Override
    protected void process(List<Integer> chunks) {
        for (int i: chunks) {
            dotDurationField.setText(String.valueOf(i));
        }
    }
    
    public void setDuration(int noisesPerDot) {
        publish(noisesPerDot);
    }
}
