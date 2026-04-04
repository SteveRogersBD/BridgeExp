package com.example.bridgeexp.chat.model;

public class ChatMessage {

    private final String id;
    private final MessageSender sender;
    private final MessageType type;
    private final String text;
    private final String timestamp;
    private final EmotionTag emotionTag;
    private final boolean spokenAloud;

    public ChatMessage(
            String id,
            MessageSender sender,
            MessageType type,
            String text,
            String timestamp,
            EmotionTag emotionTag,
            boolean spokenAloud
    ) {
        this.id = id;
        this.sender = sender;
        this.type = type;
        this.text = text;
        this.timestamp = timestamp;
        this.emotionTag = emotionTag;
        this.spokenAloud = spokenAloud;
    }

    public String getId() {
        return id;
    }

    public MessageSender getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public EmotionTag getEmotionTag() {
        return emotionTag;
    }

    public boolean isSpokenAloud() {
        return spokenAloud;
    }
}
