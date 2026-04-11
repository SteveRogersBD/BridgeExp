package com.example.bridgeexp.audio;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveAudioHandler {
    private static final String TAG = "LiveAudioHandler";
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private boolean isRecording = false;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public interface AudioDataListener {
        void onAudioData(byte[] data);
    }

    @SuppressLint("MissingPermission")
    public void start(AudioDataListener listener) {
        int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufSize);

        int outBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT, outBufSize, AudioTrack.MODE_STREAM);

        isRecording = true;
        audioRecord.startRecording();
        audioTrack.play();

        // Mic thread
        executor.execute(() -> {
            byte[] buffer = new byte[minBufSize];
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    byte[] data = new byte[read];
                    System.arraycopy(buffer, 0, data, 0, read);
                    listener.onAudioData(data);
                }
            }
        });
    }

    public void onIncomingAudio(byte[] data) {
        if (audioTrack != null && isRecording) {
            audioTrack.write(data, 0, data.length);
        }
    }

    public void stop() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
