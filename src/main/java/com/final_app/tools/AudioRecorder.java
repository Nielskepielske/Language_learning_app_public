package com.final_app.tools;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A robust, instance-based audio recorder.
 * This recorder dynamically finds a supported audio format and manages resources correctly.
 * It is designed to be used in a try-with-resources block for safety.
 *
 * Example Usage:
 * <pre>
 * {@code
 * File audioFile = new File("my_recording.wav");
 * try (AudioRecorder recorder = new AudioRecorder()) {
 *     recorder.startRecording(audioFile);
 *     // Let it record for some time...
 *     Thread.sleep(5000); // e.g., 5 seconds
 * } catch (Exception e) {
 *     e.printStackTrace();
 * }
 * // At this point, the recording is stopped and all resources are released automatically.
 * }
 * </pre>
 */
public class AudioRecorder implements AutoCloseable {

    private enum State { IDLE, RECORDING, STOPPED }

    private TargetDataLine microphone;
    private State state;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AudioRecorder() {
        this.state = State.IDLE;
    }

    /**
     * Starts recording audio to the specified file.
     * The recording runs on a separate thread, so this method returns immediately.
     *
     * @param targetFile The file where the audio will be saved.
     * @throws IOException if there is an issue writing to the file.
     * @throws LineUnavailableException if a microphone line cannot be opened with a supported format.
     * @throws IllegalStateException if the recorder is already recording or has been stopped.
     */
    public void startRecording(File targetFile) throws IOException, LineUnavailableException {
        if (state != State.IDLE) {
            throw new IllegalStateException("Recorder is not idle. Current state: " + state);
        }

        // 1. --- Find a supported AudioFormat dynamically ---
        AudioFormat format = findSupportedFormat();
        System.out.println("Using supported format: " + format);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // This check is more robust than just trying to get the line.
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Audio line not supported for format: " + format);
        }

        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        state = State.RECORDING;

        System.out.println("Recording started...");

        // 2. --- Run the audio writing on a background thread ---
        executor.submit(() -> {
            try (AudioInputStream audioStream = new AudioInputStream(microphone)) {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, targetFile);
                System.out.println("Finished writing to file.");
            } catch (IOException e) {
                // This error will be suppressed unless the future is checked,
                // but it's good practice to log it.
                System.err.println("Error writing audio stream: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Stops the recording and closes the microphone line.
     * The file writing will complete shortly after.
     *
     * @throws IllegalStateException if the recorder is not currently recording.
     */
    public void stopRecording() {
        if (state != State.RECORDING) {
            // It's okay to call stop on an already stopped recorder, so we don't throw an error.
            return;
        }

        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
        state = State.STOPPED;
        System.out.println("Recording stopped.");
    }

    /**
     * The core of the robust solution. It iterates through a list of preferred audio formats
     * and returns the first one the system's audio hardware supports.
     *
     * @return A supported AudioFormat.
     * @throws LineUnavailableException if no supported format can be found.
     */
    private AudioFormat findSupportedFormat() throws LineUnavailableException {
        // Define a list of ideal formats, from most to least desirable
        AudioFormat[] preferredFormats = new AudioFormat[]{
                // Standard CD quality, little-endian (most common for WAV)
                new AudioFormat(44100, 16, 1, true, false),
                // Same, but big-endian
                new AudioFormat(44100, 16, 1, true, true),
                // 16kHz for speech recognition, little-endian
                new AudioFormat(16000, 16, 1, true, false),
                // Same, big-endian
                new AudioFormat(16000, 16, 1, true, true),
                // 8kHz is a last resort, little-endian
                new AudioFormat(8000, 16, 1, true, false),
                // Same, big-endian
                new AudioFormat(8000, 16, 1, true, true)
        };

        for (AudioFormat format : preferredFormats) {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (AudioSystem.isLineSupported(info)) {
                return format;
            }
        }

        throw new LineUnavailableException("No supported audio format found for recording. Checked " + preferredFormats.length + " common formats.");
    }

    public boolean isRecording() {
        return state == State.RECORDING;
    }

    /**
     * Implements the AutoCloseable interface. This ensures that when the recorder is used
     * in a try-with-resources block, recording is stopped and threads are shut down.
     */
    @Override
    public void close() {
        if (state == State.RECORDING) {
            stopRecording();
        }
        // Gracefully shut down the background thread
        executor.shutdown();
    }
}

