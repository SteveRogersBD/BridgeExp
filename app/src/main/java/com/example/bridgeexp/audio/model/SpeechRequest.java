package com.example.bridgeexp.audio.model;

import java.util.Locale;

public class SpeechRequest {

    private final String text;
    private final VoiceProfile voiceProfile;
    private final Locale locale;
    private final float speed;
    private final float pitch;
    private final String utteranceId;

    public SpeechRequest(
            String text,
            VoiceProfile voiceProfile,
            Locale locale,
            float speed,
            float pitch,
            String utteranceId
    ) {
        this.text = text;
        this.voiceProfile = voiceProfile;
        this.locale = locale;
        this.speed = speed;
        this.pitch = pitch;
        this.utteranceId = utteranceId;
    }

    public String getText() {
        return text;
    }

    public VoiceProfile getVoiceProfile() {
        return voiceProfile;
    }

    public Locale getLocale() {
        return locale;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPitch() {
        return pitch;
    }

    public String getUtteranceId() {
        return utteranceId;
    }
}
