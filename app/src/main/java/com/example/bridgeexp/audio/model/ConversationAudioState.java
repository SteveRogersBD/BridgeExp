package com.example.bridgeexp.audio.model;

public class ConversationAudioState {

    private final boolean listening;
    private final boolean speaking;
    private final boolean muted;
    private final VoiceProfile activeVoiceProfile;
    private final String lastTranscript;

    public ConversationAudioState(
            boolean listening,
            boolean speaking,
            boolean muted,
            VoiceProfile activeVoiceProfile,
            String lastTranscript
    ) {
        this.listening = listening;
        this.speaking = speaking;
        this.muted = muted;
        this.activeVoiceProfile = activeVoiceProfile;
        this.lastTranscript = lastTranscript;
    }

    public boolean isListening() {
        return listening;
    }

    public boolean isSpeaking() {
        return speaking;
    }

    public boolean isMuted() {
        return muted;
    }

    public VoiceProfile getActiveVoiceProfile() {
        return activeVoiceProfile;
    }

    public String getLastTranscript() {
        return lastTranscript;
    }
}
