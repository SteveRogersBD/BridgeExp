package com.example.bridgeexp.chat.model;

public class SmartReply {

    private final String id;
    private final String text;

    public SmartReply(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
