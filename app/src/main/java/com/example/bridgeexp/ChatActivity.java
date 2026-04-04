package com.example.bridgeexp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bridgeexp.chat.ChatAdapter;
import com.example.bridgeexp.chat.SmartReplyAdapter;
import com.example.bridgeexp.chat.mock.ConversationMockData;
import com.example.bridgeexp.chat.model.ChatMessage;
import com.example.bridgeexp.chat.model.ConversationUiState;
import com.example.bridgeexp.chat.model.MessageSender;
import com.example.bridgeexp.chat.model.MessageType;
import com.example.bridgeexp.databinding.ActivityChatBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatAdapter chatAdapter;
    private SmartReplyAdapter smartReplyAdapter;
    private final List<ChatMessage> currentMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMessages();
        setupSmartReplies();
        bindState(ConversationMockData.createUiState());
        setupActions();
        updateSendState();
    }

    private void setupMessages() {
        chatAdapter = new ChatAdapter();
        binding.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.messagesRecyclerView.setAdapter(chatAdapter);
    }

    private void setupSmartReplies() {
        smartReplyAdapter = new SmartReplyAdapter(reply -> {
            binding.messageInput.setText(reply.getText());
            binding.messageInput.setSelection(reply.getText().length());
        });

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        binding.smartRepliesRecyclerView.setLayoutManager(layoutManager);
        binding.smartRepliesRecyclerView.setAdapter(smartReplyAdapter);
    }

    private void bindState(ConversationUiState uiState) {
        binding.liveStatusText.setText(uiState.getLiveStatus());

        currentMessages.clear();
        currentMessages.addAll(uiState.getMessages());
        chatAdapter.submitList(currentMessages);
        smartReplyAdapter.submitList(uiState.getSmartReplies());
        scrollToBottom();
    }

    private void setupActions() {
        binding.backButton.setOnClickListener(view -> finish());
        binding.sendButton.setOnClickListener(view -> sendCurrentMessage());
        binding.messageInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND
                    || (keyEvent != null
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                sendCurrentMessage();
                return true;
            }
            return false;
        });
        binding.messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void sendCurrentMessage() {
        String text = binding.messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        ChatMessage message = new ChatMessage(
                UUID.randomUUID().toString(),
                MessageSender.USER,
                MessageType.TEXT,
                text,
                "Now",
                null,
                true
        );

        currentMessages.add(message);
        chatAdapter.submitList(currentMessages);
        binding.messageInput.setText("");
        scrollToBottom();
    }

    private void updateSendState() {
        boolean enabled = binding.messageInput.getText() != null
                && binding.messageInput.getText().toString().trim().length() > 0;
        binding.sendButton.setEnabled(enabled);
        binding.sendButton.setClickable(enabled);
        binding.sendButton.setAlpha(enabled ? 1f : 0.7f);
        binding.sendButton.setBackgroundResource(
                enabled ? R.drawable.bg_primary_button : R.drawable.bg_send_button_disabled
        );
    }

    private void scrollToBottom() {
        binding.messagesRecyclerView.post(() -> {
            if (chatAdapter.getItemCount() > 0) {
                binding.messagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }
}
