package navalbattle;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class GameSounds {
    enum VoiceStyle {
        JAPANESE("ja-JP", -1, "medium", "-1st"),
        ENGLISH("en-GB", 1, "medium", "+1st"),
        GERMAN("de-DE", -3, "slow", "-4st"),
        COMMAND("en-US", 0, "medium", "0st");

        private final String culture;
        private final int fallbackRate;
        private final String ssmlRate;
        private final String pitch;

        VoiceStyle(String culture, int fallbackRate, String ssmlRate, String pitch) {
            this.culture = culture;
            this.fallbackRate = fallbackRate;
            this.ssmlRate = ssmlRate;
            this.pitch = pitch;
        }
    }

    private static final float SAMPLE_RATE = 44100f;
    private static final double EFFECT_VOLUME = 0.32;
    private static final double MUSIC_VOLUME = 0.10;
    private static final ExecutorService VOICE_QUEUE = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "admiral-voice-queue");
        thread.setDaemon(true);
        return thread;
    });
    private static volatile boolean backgroundStarted;

    private GameSounds() {
    }

    static void startBackgroundScore() {
        if (backgroundStarted) {
            return;
        }
        backgroundStarted = true;
        Thread scoreThread = new Thread(GameSounds::playBackgroundLoop, "background-score");
        scoreThread.setDaemon(true);
        scoreThread.start();
    }

    static void launch() {
        playAsync(new int[][]{{190, 60}, {135, 110}}, EFFECT_VOLUME);
    }

    static void miss() {
        playAsync(new int[][]{{150, 170}}, EFFECT_VOLUME);
    }

    static void hit() {
        playAsync(new int[][]{{420, 80}, {690, 130}}, EFFECT_VOLUME);
    }

    static void sunk() {
        playAsync(new int[][]{{260, 90}, {330, 90}, {520, 210}}, EFFECT_VOLUME);
    }

    static void victory() {
        playAsync(new int[][]{{523, 120}, {659, 120}, {784, 130}, {1046, 290}}, EFFECT_VOLUME);
    }

    static void speak(String text, VoiceStyle style) {
        VOICE_QUEUE.submit(() -> speakWithWindowsVoice(text, style));
    }

    private static void speakWithWindowsVoice(String text, VoiceStyle style) {
        String escapedText = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&apos;");
        String ssml = "<speak version=\"1.0\" xml:lang=\"en-US\">"
                + "<voice xml:lang=\"" + style.culture + "\">"
                + "<prosody rate=\"" + style.ssmlRate + "\" pitch=\"" + style.pitch + "\">"
                + escapedText
                + "</prosody></voice></speak>";

        String command = "Add-Type -AssemblyName System.Speech; "
                + "$speaker = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                + "$speaker.Volume = 100; "
                + "$voice = $speaker.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.Name -eq '" + style.culture + "' } | Select-Object -First 1; "
                + "if ($voice) { $speaker.SelectVoice($voice.VoiceInfo.Name); $speaker.SpeakSsml('" + ssml.replace("'", "''") + "'); } "
                + "else { $speaker.Rate = " + style.fallbackRate + "; $speaker.Speak('" + text.replace("'", "''") + "'); }";

        ProcessBuilder builder = new ProcessBuilder("powershell", "-NoProfile", "-Command", command);
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException | InterruptedException ignored) {
            Thread.currentThread().interrupt();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private static void playBackgroundLoop() {
        int[][][] themes = {
                {{220, 260}, {0, 70}, {196, 260}, {0, 70}, {165, 360}, {0, 180}, {196, 220}, {185, 220}},
                {{147, 300}, {0, 60}, {175, 180}, {196, 180}, {220, 340}, {0, 150}, {196, 220}, {175, 260}},
                {{110, 420}, {0, 80}, {131, 240}, {147, 240}, {165, 360}, {147, 220}, {131, 300}}
        };

        while (true) {
            for (int[][] theme : themes) {
                playNotes(theme, MUSIC_VOLUME);
                sleep(900);
            }
        }
    }

    private static void playAsync(int[][] notes, double volume) {
        Thread soundThread = new Thread(() -> playNotes(notes, volume));
        soundThread.setDaemon(true);
        soundThread.start();
    }

    private static void playNotes(int[][] notes, double volume) {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            for (int[] note : notes) {
                playTone(line, note[0], note[1], volume);
                playTone(line, 0, 35, volume);
            }
            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private static void playTone(SourceDataLine line, int frequency, int milliseconds, double volume) {
        int sampleCount = (int) (milliseconds * SAMPLE_RATE / 1000);
        byte[] buffer = new byte[sampleCount];

        for (int i = 0; i < sampleCount; i++) {
            if (frequency != 0) {
                double angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE;
                buffer[i] = (byte) (Math.sin(angle) * 127 * volume);
            }
        }
        line.write(buffer, 0, buffer.length);
    }

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
