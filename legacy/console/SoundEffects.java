// Legacy reference only. This class is not referenced by the active JavaFX runtime.
// The active sound implementation is navalbattle.GameSounds.

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;

public class SoundEffects {

    private static final float SAMPLE_RATE = 44100f;
    private static final double VOLUME = 0.35;

    public static void playLaunch() {
        playAsync(new int[][]{
                {180, 70},
                {120, 110}
        });
    }

    public static void playMiss() {
        playAsync(new int[][]{
                {160, 180}
        });
    }

    public static void playHit() {
        playAsync(new int[][]{
                {440, 90},
                {660, 120}
        });
    }

    public static void playSunk() {
        playAsync(new int[][]{
                {220, 90},
                {330, 90},
                {440, 180}
        });
    }

    public static void playVictory() {
        playNotes(new int[][]{
                {523, 120},
                {659, 120},
                {784, 120},
                {1046, 260}
        });
    }

    private static void playAsync(int[][] notes) {
        Thread soundThread = new Thread(() -> playNotes(notes));
        soundThread.setDaemon(true);
        soundThread.start();
    }

    private static void playNotes(int[][] notes) {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();

            for (int[] note : notes) {
                playTone(line, note[0], note[1]);
                playTone(line, 0, 35);
            }

            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private static void playTone(SourceDataLine line, int frequency, int milliseconds) {
        int sampleCount = (int) (milliseconds * SAMPLE_RATE / 1000);
        byte[] buffer = new byte[sampleCount];

        for (int i = 0; i < sampleCount; i++) {
            if (frequency == 0) {
                buffer[i] = 0;
            } else {
                double angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE;
                buffer[i] = (byte) (Math.sin(angle) * 127 * VOLUME);
            }
        }

        line.write(buffer, 0, buffer.length);
    }
}
