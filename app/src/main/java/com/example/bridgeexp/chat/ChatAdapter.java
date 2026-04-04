package com.example.bridgeexp.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridgeexp.R;
import com.example.bridgeexp.chat.model.ChatMessage;
import com.example.bridgeexp.chat.model.MessageSender;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_OTHER = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_SYSTEM = 2;

    private final List<ChatMessage> messages = new ArrayList<>();

    public void submitList(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MessageSender sender = messages.get(position).getSender();
        if (sender == MessageSender.USER) {
            return TYPE_USER;
        }
        if (sender == MessageSender.SYSTEM) {
            return TYPE_SYSTEM;
        }
        return TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageViewHolder(view);
        }

        if (viewType == TYPE_SYSTEM) {
            View view = inflater.inflate(R.layout.item_message_system, parent, false);
            return new SystemMessageViewHolder(view);
        }

        View view = inflater.inflate(R.layout.item_message_other, parent, false);
        return new OtherMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(message);
        } else if (holder instanceof OtherMessageViewHolder) {
            ((OtherMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageText;
        private final TextView timestampText;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getText());
            timestampText.setText(message.getTimestamp());
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageText;
        private final TextView timestampText;

        OtherMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getText());
            timestampText.setText(message.getTimestamp());
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageText;
        private final TextView timestampText;

        SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            timestampText = itemView.findViewById(R.id.timestampText);
        }

        void bind(ChatMessage message) {
            messageText.setText(message.getText());
            timestampText.setText(message.getTimestamp());
        }
    }
}
