package com.example.bridgeexp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;

import android.util.Log;
import org.json.JSONObject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridgeexp.audio.AudioManagerFacade;
import com.example.bridgeexp.audio.VoiceProfileRegistry;
import com.example.bridgeexp.audio.model.ConversationAudioState;
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
import com.example.bridgeexp.chat.LiveAgentClient;
import com.example.bridgeexp.chat.model.SmartReply;
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
    private boolean isSpeaking;
    private String lastSpokenText = "";
    private String liveTranscript = "";

    // Live Agent Integration
    private LiveAgentClient liveAgentClient;
    private boolean isLiveMode = true; 

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
        renderAudioState();
        setupLiveAgent();
    }

    private void setupLiveAgent() {
        liveAgentClient = new LiveAgentClient(new LiveAgentClient.Listener() {
            @Override
            public void onSmartRepliesReceived(List<SmartReply> replies) {
                smartReplyAdapter.submitList(replies);
            }

            @Override
            public void onTextMessageReceived(String text) {
                binding.liveStatusText.setText(text);
            }

            @Override
            public void onAudioDataReceived(byte[] data) {
                // Not used in text-only mode
            }

            @Override
            public void onError(String message) {
                binding.liveStatusText.setText("Error: " + message);
            }
        });
        
        // Connect automatically
        liveAgentClient.connect("ws://192.168.1.249:8765", "test_user_001");
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
                renderAudioState();
            }

            @Override
            public void onSpeechStarted(String utteranceId) {
                isSpeaking = true;
                binding.liveStatusText.setText(R.string.chat_status_speaking);
                renderAudioState();
            }

            @Override
            public void onSpeechCompleted(SpeechResult result) {
                isSpeaking = false;
                binding.liveStatusText.setText(R.string.chat_status_speech_done);
                renderAudioState();
            }

            @Override
            public void onError(String message) {
                isSpeaking = false;
                binding.liveStatusText.setText(message);
                renderAudioState();
            }
        });
    }

    private void setupMessages() {
        chatAdapter = new ChatAdapter();
        binding.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.messagesRecyclerView.setAdapter(chatAdapter);
        
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (chatAdapter.getItemCount() > 0) {
                    binding.messagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            }
        });
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
        chatAdapter.submitList(new ArrayList<>(currentMessages));
        smartReplyAdapter.submitList(uiState.getSmartReplies());
        scrollToBottom();
    }

    private void setupActions() {
        binding.backButton.setOnClickListener(view -> finish());
        binding.sendButton.setOnClickListener(view -> sendCurrentMessage());
        binding.listenButton.setOnClickListener(view -> toggleListening());
        binding.replayButton.setOnClickListener(view -> replayLastSpokenText());
        binding.stopSpeakingButton.setOnClickListener(view -> stopSpeaking());
        binding.voiceProfileText.setOnClickListener(view -> cycleVoiceProfile());
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
            renderAudioState();
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
                        liveTranscript = "";
                        renderAudioState();
                    }

                    @Override
                    public void onPartialTranscript(TranscriptResult result) {
                        liveTranscript = result.getText();
                        renderAudioState();
                    }

                    @Override
                    public void onFinalTranscript(TranscriptResult result) {
                        isListening = false;
                        liveTranscript = result.getText();
                        updateListenButtonState(false);
                        binding.liveStatusText.setText(R.string.chat_status_ready);
                        renderAudioState();
                        appendIncomingMessage(result.getText());

                        // --- NEW: Send to Python Agent for Smart Replies ---
                        try {
                            JSONObject msg = new JSONObject();
                            msg.put("user_id", "test_user_001");
                            msg.put("text", result.getText());
                            liveAgentClient.sendText(msg.toString());
                        } catch (Exception e) {
                            Log.e("ChatActivity", "Failed to send to agent", e);
                        }
                    }

                    @Override
                    public void onListeningStopped() {
                        isListening = false;
                        updateListenButtonState(false);
                        renderAudioState();
                    }

                    @Override
                    public void onError(String message) {
                        isListening = false;
                        updateListenButtonState(false);
                        binding.liveStatusText.setText(message);
                        renderAudioState();
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
        chatAdapter.submitList(new ArrayList<>(currentMessages));
        binding.messageInput.setText("");
        lastSpokenText = text;
        audioManagerFacade.speakText(text, activeVoiceProfile);
        renderAudioState();
        scrollToBottom();

        // --- NEW: Send manually typed text to Python Agent ---
        try {
            org.json.JSONObject msg = new org.json.JSONObject();
            msg.put("user_id", "test_user_001");
            msg.put("text", text);
            if (liveAgentClient != null) {
                liveAgentClient.sendText(msg.toString());
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "Failed to send to agent", e);
        }
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
        chatAdapter.submitList(new ArrayList<>(currentMessages));
        liveTranscript = "";
        renderAudioState();
        scrollToBottom();
    }

    private void replayLastSpokenText() {
        if (lastSpokenText == null || lastSpokenText.trim().isEmpty()) {
            return;
        }
        audioManagerFacade.speakText(lastSpokenText, activeVoiceProfile);
    }

    private void stopSpeaking() {
        audioManagerFacade.stopSpeaking();
        isSpeaking = false;
        binding.liveStatusText.setText(R.string.chat_status_ready);
        renderAudioState();
    }

    private void cycleVoiceProfile() {
        List<VoiceProfile> profiles = VoiceProfileRegistry.getDefaultProfiles();
        if (profiles.isEmpty()) {
            return;
        }

        int currentIndex = 0;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(activeVoiceProfile.getId())) {
                currentIndex = i;
                break;
            }
        }

        int nextIndex = (currentIndex + 1) % profiles.size();
        activeVoiceProfile = profiles.get(nextIndex);
        renderAudioState();
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

    private void renderAudioState() {
        ConversationAudioState audioState = new ConversationAudioState(
                isListening,
                isSpeaking,
                false,
                activeVoiceProfile,
                liveTranscript
        );

        binding.voiceProfileText.setText(audioState.getActiveVoiceProfile().getDisplayName());
        binding.replayButton.setAlpha(lastSpokenText.isEmpty() ? 0.45f : 1f);
        binding.replayButton.setEnabled(!lastSpokenText.isEmpty());
        binding.stopSpeakingButton.setAlpha(audioState.isSpeaking() ? 1f : 0.45f);
        binding.stopSpeakingButton.setEnabled(audioState.isSpeaking());

        if (audioState.getLastTranscript() == null || audioState.getLastTranscript().isEmpty()) {
            binding.liveTranscriptPreview.setText(R.string.chat_live_transcript_hint);
            binding.liveTranscriptPreview.setTextColor(getColor(R.color.bridge_text_muted));
        } else {
            binding.liveTranscriptPreview.setText(audioState.getLastTranscript());
            binding.liveTranscriptPreview.setTextColor(getColor(R.color.bridge_text_primary));
        }
    }

    private void scrollToBottom() {
        binding.messagesRecyclerView.post(() -> {
            if (chatAdapter.getItemCount() > 0) {
                binding.messagesRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
    }
}
