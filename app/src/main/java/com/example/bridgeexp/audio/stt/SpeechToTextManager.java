package com.example.bridgeexp.audio.stt;

import android.content.Intent;

import com.example.bridgeexp.audio.model.TranscriptResult;

public interface SpeechToTextManager {

    interface Listener {
        void onListeningStarted();

        void onPartialTranscript(TranscriptResult result);

        void onFinalTranscript(TranscriptResult result);

        void onListeningStopped();

        void onError(String message);
    }

    void startListening(Intent recognizerIntent, Listener listener);

    void stopListening();

    void cancel();

    void release();

    boolean isListening();
}
