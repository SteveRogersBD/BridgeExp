package com.example.bridgeexp.chat.model;

import java.util.List;

public class ConversationUiState {

    private final String liveStatus;
    private final boolean listening;
    private final List<ChatMessage> messages;
    private final List<SmartReply> smartReplies;

    public ConversationUiState(
            String liveStatus,
            boolean listening,
            List<ChatMessage> messages,
            List<SmartReply> smartReplies
    ) {
        this.liveStatus = liveStatus;
        this.listening = listening;
        this.messages = messages;
        this.smartReplies = smartReplies;
    }

    public String getLiveStatus() {
        return liveStatus;
    }

    public boolean isListening() {
        return listening;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public List<SmartReply> getSmartReplies() {
        return smartReplies;
    }
}
