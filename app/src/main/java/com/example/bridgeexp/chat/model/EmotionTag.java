package com.example.bridgeexp.chat.model;

public class EmotionTag {

    private final String label;
    private final int colorResId;

    public EmotionTag(String label, int colorResId) {
        this.label = label;
        this.colorResId = colorResId;
    }

    public String getLabel() {
        return label;
    }

    public int getColorResId() {
        return colorResId;
    }
}
