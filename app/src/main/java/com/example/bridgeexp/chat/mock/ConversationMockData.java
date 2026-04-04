package com.example.bridgeexp.chat.mock;

import com.example.bridgeexp.R;
import com.example.bridgeexp.chat.model.ChatMessage;
import com.example.bridgeexp.chat.model.ConversationUiState;
import com.example.bridgeexp.chat.model.EmotionTag;
import com.example.bridgeexp.chat.model.MessageSender;
import com.example.bridgeexp.chat.model.MessageType;
import com.example.bridgeexp.chat.model.SmartReply;

import java.util.ArrayList;
import java.util.List;

public final class ConversationMockData {

    private ConversationMockData() {
    }

    public static ConversationUiState createUiState() {
        return new ConversationUiState(
                "Listening and translating",
                true,
                getMessages(),
                getSmartReplies()
        );
    }

    public static List<ChatMessage> getMessages() {
        List<ChatMessage> messages = new ArrayList<>();

        messages.add(new ChatMessage(
                "1",
                MessageSender.OTHER,
                MessageType.TEXT,
                "I can meet you downstairs in ten minutes.",
                "9:42 PM",
                new EmotionTag("Calm", R.color.bridge_chip_text),
                false
        ));

        messages.add(new ChatMessage(
                "2",
                MessageSender.SYSTEM,
                MessageType.ACTION_STATUS,
                "Emotion insight detected: calm tone with steady pace.",
                "9:42 PM",
                null,
                false
        ));

        messages.add(new ChatMessage(
                "3",
                MessageSender.USER,
                MessageType.TEXT,
                "Perfect, I am on my way.",
                "9:43 PM",
                null,
                true
        ));

        return messages;
    }

    public static List<SmartReply> getSmartReplies() {
        List<SmartReply> replies = new ArrayList<>();
        replies.add(new SmartReply("1", "Perfect, I am on my way."));
        replies.add(new SmartReply("2", "Give me 10 minutes."));
        replies.add(new SmartReply("3", "Can you wait outside?"));
        return replies;
    }
}
