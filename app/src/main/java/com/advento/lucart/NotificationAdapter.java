package com.advento.lucart;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Log the notification data for debugging
        Log.d("NotificationAdapter", "Title: " + notification.getTitle() + ", Message: " + notification.getMessage());
        Log.d("NotificationAdapter", "Timestamp: " + notification.getDate());
        Log.d("NotificationAdapter", "Status: " + notification.getStatus());

        // Get title and message
        String title = notification.getTitle();
        String message = notification.getMessage();

        // Set default text if title or message is empty or null
        holder.titleTextView.setText(title != null && !title.isEmpty() ? title : "No Title");
        holder.messageTextView.setText(message != null && !message.isEmpty() ? message : "No Message");
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, messageTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tvNotificationTitle);
            messageTextView = itemView.findViewById(R.id.tvNotificationMessage);
        }
    }
}