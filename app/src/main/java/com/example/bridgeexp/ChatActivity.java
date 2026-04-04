package com.example.bridgeexp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bridgeexp.audio.AudioManagerFacade;
import com.example.bridgeexp.audio.VoiceProfileRegistry;
import com.example.bridgeexp.audio.model.SpeechResult;
import com.example.bridgeexp.audio.model.TranscriptResult;
import com.example.bridgeexp.audio.model.VoiceProfile;
import com.example.bridgeexp.audio.stt.AndroidSpeechToTextManager;
import com.example.bridgeexp.audio.stt.SpeechToTextManager;
import com.example.bridgeexp.audio.tts.TextToSpeechManager;
import com.example.bridgeexp.chat.ChatAdapter;
import com.example.bridgeexp.chat.SmartReplyAdapter;
import com.example.bridgeexp.chat.mock.ConversationMockData;
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
    private AudioManagerFacade audioManagerFacade;
    private VoiceProfile activeVoiceProfile;
    private boolean isListening;

    private final List<com.example.bridgeexp.chat.model.ChatMessage> currentMessages =
            new ArrayList<>();

    private final ActivityResultLauncher<String> microphonePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startSpeechRecognition();
                } else {
                    binding.liveStatusText.setText(R.string.chat_status_mic_denied);
                    updateListenButtonState(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        audioManagerFacade = new AudioManagerFacade(this);
        activeVoiceProfile = VoiceProfileRegistry.getFallbackProfile();

        initializeAudio();
        setupMessages();
        setupSmartReplies();
        bindState(ConversationMockData.createUiState());
        setupActions();
        updateSendState();
        updateListenButtonState(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioManagerFacade != null) {
            audioManagerFacade.release();
        }
    }

    private void initializeAudio() {
        audioManagerFacade.initializeTts(new TextToSpeechManager.Listener() {
            @Override
            public void onReady() {
                binding.liveStatusText.setText(R.string.chat_status_ready);
            }

            @Override
            public void onSpeechStarted(String utteranceId) {
            }

            @Override
            public void onSpeechCompleted(SpeechResult result) {
            }

            @Override
            public void onError(String message) {
                binding.liveStatusText.setText(message);
            }
        });
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
        binding.listenButton.setOnClickListener(view -> toggleListening());
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

    private void toggleListening() {
        if (isListening) {
            audioManagerFacade.stopListening();
            isListening = false;
            binding.liveStatusText.setText(R.string.chat_status_not_listening);
            updateListenButtonState(false);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startSpeechRecognition() {
        audioManagerFacade.startListening(
                AndroidSpeechToTextManager.createDefaultRecognizerIntent(),
                new SpeechToTextManager.Listener() {
                    @Override
                    public void onListeningStarted() {
                        isListening = true;
                        binding.liveStatusText.setText(R.string.chat_status_listening);
                        updateListenButtonState(true);
                    }

                    @Override
                    public void onPartialTranscript(TranscriptResult result) {
                        binding.messageInput.setText(result.getText());
                        binding.messageInput.setSelection(result.getText().length());
                    }

                    @Override
                    public void onFinalTranscript(TranscriptResult result) {
                        isListening = false;
                        updateListenButtonState(false);
                        binding.liveStatusText.setText(R.string.chat_status_ready);
                        appendIncomingMessage(result.getText());
                    }

                    @Override
                    public void onListeningStopped() {
                        isListening = false;
                        updateListenButtonState(false);
                    }

                    @Override
                    public void onError(String message) {
                        isListening = false;
                        updateListenButtonState(false);
                        binding.liveStatusText.setText(message);
                    }
                }
        );
    }

    private void sendCurrentMessage() {
        String text = binding.messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        com.example.bridgeexp.chat.model.ChatMessage message =
                new com.example.bridgeexp.chat.model.ChatMessage(
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
        audioManagerFacade.speakText(text, activeVoiceProfile);
        scrollToBottom();
    }

    private void appendIncomingMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        com.example.bridgeexp.chat.model.ChatMessage message =
                new com.example.bridgeexp.chat.model.ChatMessage(
                        UUID.randomUUID().toString(),
                        MessageSender.OTHER,
                        MessageType.TEXT,
                        text.trim(),
                        "Now",
                        null,
                        false
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

    private void updateListenButtonState(boolean listening) {
        binding.listenButton.setText(listening ? R.string.chat_stop : R.string.chat_listen);
        binding.listenButton.setBackgroundResource(
                listening ? R.drawable.bg_toggle_active : R.drawable.bg_glass_card
        );
        binding.listenButton.setTextColor(getColor(
                listening ? R.color.bridge_toggle_active_text : R.color.bridge_text_primary
        ));
    }

    private void scrollToBottom() {
        binding.messagesRecyclerView.post(() -> {
            if (chatAdapter.getItemCount() > 0) {
                binding.messagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }
}
