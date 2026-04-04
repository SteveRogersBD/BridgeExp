package com.example.bridgeexp.audio.stt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.example.bridgeexp.audio.model.TranscriptResult;

import java.util.ArrayList;

public class AndroidSpeechToTextManager implements SpeechToTextManager {

    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private Listener listener;
    private boolean listening;

    public AndroidSpeechToTextManager(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void startListening(Intent recognizerIntent, Listener listener) {
        this.listener = listener;

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            if (this.listener != null) {
                this.listener.onError("Speech recognition is not available on this device.");
            }
            return;
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    listening = true;
                    if (AndroidSpeechToTextManager.this.listener != null) {
                        AndroidSpeechToTextManager.this.listener.onListeningStarted();
                    }
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    listening = false;
                    if (AndroidSpeechToTextManager.this.listener != null) {
                        AndroidSpeechToTextManager.this.listener.onListeningStopped();
                    }
                }

                @Override
                public void onError(int error) {
                    listening = false;
                    if (AndroidSpeechToTextManager.this.listener != null) {
                        AndroidSpeechToTextManager.this.listener.onError(
                                "Speech recognition error code: " + error
                        );
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    dispatchResult(results, true);
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    dispatchResult(partialResults, false);
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }

                private void dispatchResult(Bundle bundle, boolean finalResult) {
                    if (AndroidSpeechToTextManager.this.listener == null || bundle == null) {
                        return;
                    }

                    ArrayList<String> matches = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );
                    float[] scores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                    if (matches == null || matches.isEmpty()) {
                        return;
                    }

                    float confidence = (scores != null && scores.length > 0) ? scores[0] : 0f;
                    TranscriptResult result = new TranscriptResult(
                            matches.get(0),
                            finalResult,
                            confidence
                    );

                    if (finalResult) {
                        AndroidSpeechToTextManager.this.listener.onFinalTranscript(result);
                    } else {
                        AndroidSpeechToTextManager.this.listener.onPartialTranscript(result);
                    }
                }
            });
        }

        Intent intent = recognizerIntent != null
                ? recognizerIntent
                : createDefaultRecognizerIntent();
        speechRecognizer.startListening(intent);
    }

    @Override
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    @Override
    public void cancel() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
        listening = false;
    }

    @Override
    public void release() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        listening = false;
    }

    @Override
    public boolean isListening() {
        return listening;
    }

    public static Intent createDefaultRecognizerIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        return intent;
    }
}
