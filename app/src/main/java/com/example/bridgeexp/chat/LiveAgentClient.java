package com.example.bridgeexp.chat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.bridgeexp.chat.model.SmartReply;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class LiveAgentClient {
    private static final String TAG = "LiveAgentClient";
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final Listener listener;
    private final Handler mainHandler;

    public interface Listener {
        void onSmartRepliesReceived(List<SmartReply> replies);
        void onTextMessageReceived(String text);
        void onAudioDataReceived(byte[] data);
        void onError(String message);
    }

    public LiveAgentClient(Listener listener) {
        this.client = new OkHttpClient();
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void connect(String url, String userId) {
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "Connected to server");
                // Send initialization message
                try {
                    JSONObject init = new JSONObject();
                    init.put("user_id", userId);
                    webSocket.send(init.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Initialization error", e);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    Log.d(TAG, "Received message from Python: " + text);
                    JSONObject json = new JSONObject(text);
                    String type = json.optString("type");

                    if ("smart_replies".equals(type)) {
                        JSONArray repliesJson = json.getJSONArray("replies");
                        List<SmartReply> replies = new ArrayList<>();
                        for (int i = 0; i < repliesJson.length(); i++) {
                            replies.add(new SmartReply(String.valueOf(i), repliesJson.getString(i)));
                        }
                        mainHandler.post(() -> listener.onSmartRepliesReceived(replies));
                    } else if ("text".equals(type)) {
                        String content = json.getString("text");
                        mainHandler.post(() -> listener.onTextMessageReceived(content));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message", e);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // This is raw audio data from Gemini's default TTS
                listener.onAudioDataReceived(bytes.toByteArray());
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket failure", t);
                mainHandler.post(() -> listener.onError(t.getMessage()));
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Websocket closing: " + reason);
            }
        });
    }

    public void sendText(String text) {
        if (webSocket != null) {
            webSocket.send(text);
        }
    }

    public void sendAudio(byte[] data) {
        if (webSocket != null) {
            webSocket.send(ByteString.of(data));
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User logout");
        }
    }
}
