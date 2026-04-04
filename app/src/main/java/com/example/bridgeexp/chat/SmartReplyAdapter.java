package com.example.bridgeexp.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bridgeexp.R;
import com.example.bridgeexp.chat.model.SmartReply;

import java.util.ArrayList;
import java.util.List;

public class SmartReplyAdapter extends RecyclerView.Adapter<SmartReplyAdapter.ViewHolder> {

    public interface OnReplyClickListener {
        void onReplyClicked(SmartReply reply);
    }

    private final List<SmartReply> replies = new ArrayList<>();
    private final OnReplyClickListener listener;

    public SmartReplyAdapter(OnReplyClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SmartReply> newReplies) {
        replies.clear();
        replies.addAll(newReplies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_smart_reply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(replies.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView replyText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            replyText = itemView.findViewById(R.id.replyText);
        }

        void bind(SmartReply reply, OnReplyClickListener listener) {
            replyText.setText(reply.getText());
            itemView.setOnClickListener(view -> listener.onReplyClicked(reply));
        }
    }
}
