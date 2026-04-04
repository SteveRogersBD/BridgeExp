package com.example.bridgeexp.audio;

import android.content.Context;
import android.content.Intent;

import com.example.bridgeexp.audio.model.SpeechRequest;
import com.example.bridgeexp.audio.model.VoiceProfile;
import com.example.bridgeexp.audio.stt.AndroidSpeechToTextManager;
import com.example.bridgeexp.audio.stt.SpeechToTextManager;
import com.example.bridgeexp.audio.tts.AndroidTextToSpeechManager;
import com.example.bridgeexp.audio.tts.TextToSpeechManager;

import java.util.Locale;
import java.util.UUID;

public class AudioManagerFacade {

    private final TextToSpeechManager textToSpeechManager;
    private final SpeechToTextManager speechToTextManager;

    public AudioManagerFacade(Context context) {
        this.textToSpeechManager = new AndroidTextToSpeechManager(context);
        this.speechToTextManager = new AndroidSpeechToTextManager(context);
    }

    public void initializeTts(TextToSpeechManager.Listener listener) {
        textToSpeechManager.initialize(listener);
    }

    public void speakText(String text, VoiceProfile voiceProfile) {
        SpeechRequest request = new SpeechRequest(
                text,
                voiceProfile,
                Locale.getDefault(),
                1.0f,
                1.0f,
                UUID.randomUUID().toString()
        );
        textToSpeechManager.speak(request);
    }

    public void startListening(Intent recognizerIntent, SpeechToTextManager.Listener listener) {
        speechToTextManager.startListening(recognizerIntent, listener);
    }

    public void stopListening() {
        speechToTextManager.stopListening();
    }

    public void stopSpeaking() {
        textToSpeechManager.stop();
    }

    public void release() {
        textToSpeechManager.release();
        speechToTextManager.release();
    }
}
