package com.example.bridgeexp.audio.tts;

import com.example.bridgeexp.audio.model.SpeechRequest;
import com.example.bridgeexp.audio.model.SpeechResult;

public interface TextToSpeechManager {

    interface Listener {
        void onReady();

        void onSpeechStarted(String utteranceId);

        void onSpeechCompleted(SpeechResult result);

        void onError(String message);
    }

    void initialize(Listener listener);

    void speak(SpeechRequest request);

    void stop();

    void release();

    boolean isReady();
}
