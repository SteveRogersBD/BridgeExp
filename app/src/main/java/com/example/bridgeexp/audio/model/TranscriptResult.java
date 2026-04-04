package com.example.bridgeexp.audio.model;

public class TranscriptResult {

    private final String text;
    private final boolean finalResult;
    private final float confidence;

    public TranscriptResult(String text, boolean finalResult, float confidence) {
        this.text = text;
        this.finalResult = finalResult;
        this.confidence = confidence;
    }

    public String getText() {
        return text;
    }

    public boolean isFinalResult() {
        return finalResult;
    }

    public float getConfidence() {
        return confidence;
    }
}
