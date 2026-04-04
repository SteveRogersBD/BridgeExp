package com.example.bridgeexp.audio.model;

public class VoiceProfile {

    private final String id;
    private final String displayName;
    private final VoiceProvider provider;
    private final VoiceProfileType type;
    private final String providerVoiceId;
    private final boolean availableOffline;

    public VoiceProfile(
            String id,
            String displayName,
            VoiceProvider provider,
            VoiceProfileType type,
            String providerVoiceId,
            boolean availableOffline
    ) {
        this.id = id;
        this.displayName = displayName;
        this.provider = provider;
        this.type = type;
        this.providerVoiceId = providerVoiceId;
        this.availableOffline = availableOffline;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public VoiceProvider getProvider() {
        return provider;
    }

    public VoiceProfileType getType() {
        return type;
    }

    public String getProviderVoiceId() {
        return providerVoiceId;
    }

    public boolean isAvailableOffline() {
        return availableOffline;
    }
}
