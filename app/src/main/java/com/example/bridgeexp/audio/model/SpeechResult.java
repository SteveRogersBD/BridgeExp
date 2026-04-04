package com.example.bridgeexp.audio.model;

public class SpeechResult {

    private final boolean success;
    private final String utteranceId;
    private final String message;

    public SpeechResult(boolean success, String utteranceId, String message) {
        this.success = success;
        this.utteranceId = utteranceId;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUtteranceId() {
        return utteranceId;
    }

    public String getMessage() {
        return message;
    }
}
