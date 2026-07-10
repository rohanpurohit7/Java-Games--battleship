package navalbattle;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;
import java.io.IOException;

final class GameSounds {
    private static final float SAMPLE_RATE = 44100f;
    private static final double VOLUME = 0.32;

    private GameSounds() {
    }

    static void launch() {
        playAsync(new int[][]{{190, 60}, {135, 110}});
    }

    static void miss() {
        playAsync(new int[][]{{150, 170}});
    }

    static void hit() {
        playAsync(new int[][]{{420, 80}, {690, 130}});
    }

    static void sunk() {
        playAsync(new int[][]{{260, 90}, {330, 90}, {520, 210}});
    }

    static void victory() {
        playAsync(new int[][]{{523, 120}, {659, 120}, {784, 130}, {1046, 290}});
    }

    static void speak(String text, int rate) {
        Thread voiceThread = new Thread(() -> speakWithWindowsVoice(text, rate));
        voiceThread.setDaemon(true);
        voiceThread.start();
    }

    private static void speakWithWindowsVoice(String text, int rate) {
        ProcessBuilder builder = new ProcessBuilder(
                "powershell",
                "-NoProfile",
                "-Command",
                "Add-Type -AssemblyName System.Speech; "
                        + "$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                        + "$speaker.Rate = " + rate + "; "
                        + "$speaker.Volume = 100; "
                        + "$speaker.Speak('" + text.replace("'", "''") + "');"
        );

        try {
            builder.start();
        } catch (IOException ignored) {
            Toolkit.getDefaultToolkit().beep();
        }
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
            if (frequency != 0) {
                double angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE;
                buffer[i] = (byte) (Math.sin(angle) * 127 * VOLUME);
            }
        }
        line.write(buffer, 0, buffer.length);
    }
}
