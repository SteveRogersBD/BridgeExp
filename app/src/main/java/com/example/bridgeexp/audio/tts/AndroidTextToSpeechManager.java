package com.example.bridgeexp.audio.tts;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.example.bridgeexp.audio.model.SpeechRequest;
import com.example.bridgeexp.audio.model.SpeechResult;

import java.util.Locale;

public class AndroidTextToSpeechManager implements TextToSpeechManager {

    private final Context context;
    private TextToSpeech textToSpeech;
    private Listener listener;
    private boolean ready;

    public AndroidTextToSpeechManager(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void initialize(Listener listener) {
        this.listener = listener;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.SUCCESS) {
                ready = false;
                if (this.listener != null) {
                    this.listener.onError("Android TTS initialization failed.");
                }
                return;
            }

            ready = true;
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    if (AndroidTextToSpeechManager.this.listener != null) {
                        AndroidTextToSpeechManager.this.listener.onSpeechStarted(utteranceId);
                    }
                }

                @Override
                public void onDone(String utteranceId) {
                    if (AndroidTextToSpeechManager.this.listener != null) {
                        AndroidTextToSpeechManager.this.listener.onSpeechCompleted(
                                new SpeechResult(true, utteranceId, "Speech completed.")
                        );
                    }
                }

                @Override
                public void onError(String utteranceId) {
                    if (AndroidTextToSpeechManager.this.listener != null) {
                        AndroidTextToSpeechManager.this.listener.onSpeechCompleted(
                                new SpeechResult(false, utteranceId, "Speech failed.")
                        );
                    }
                }
            });

            if (this.listener != null) {
                this.listener.onReady();
            }
        });
    }

    @Override
    public void speak(SpeechRequest request) {
        if (!ready || textToSpeech == null) {
            if (listener != null) {
                listener.onError("Text to speech is not ready.");
            }
            return;
        }

        Locale locale = request.getLocale() != null ? request.getLocale() : Locale.getDefault();
        textToSpeech.setLanguage(locale);
        textToSpeech.setSpeechRate(request.getSpeed());
        textToSpeech.setPitch(request.getPitch());

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, request.getUtteranceId());

        textToSpeech.speak(
                request.getText(),
                TextToSpeech.QUEUE_FLUSH,
                params,
                request.getUtteranceId()
        );
    }

    @Override
    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    public void release() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        ready = false;
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
