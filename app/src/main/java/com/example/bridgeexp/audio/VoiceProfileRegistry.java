package com.example.bridgeexp.audio;

import com.example.bridgeexp.audio.model.VoiceProfile;
import com.example.bridgeexp.audio.model.VoiceProfileType;
import com.example.bridgeexp.audio.model.VoiceProvider;

import java.util.ArrayList;
import java.util.List;

public final class VoiceProfileRegistry {

    private VoiceProfileRegistry() {
    }

    public static List<VoiceProfile> getDefaultProfiles() {
        List<VoiceProfile> profiles = new ArrayList<>();
        profiles.add(new VoiceProfile(
                "android-default-1",
                "Bridge Clear",
                VoiceProvider.ANDROID_SYSTEM,
                VoiceProfileType.DEFAULT,
                null,
                true
        ));
        profiles.add(new VoiceProfile(
                "android-default-2",
                "Bridge Warm",
                VoiceProvider.ANDROID_SYSTEM,
                VoiceProfileType.DEFAULT,
                null,
                true
        ));
        return profiles;
    }

    public static VoiceProfile getFallbackProfile() {
        return getDefaultProfiles().get(0);
    }
}
