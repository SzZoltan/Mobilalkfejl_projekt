package com.example.it_webshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {
    private static final String CHANNEL_ID = "webshop_notification_channel";
    private final int NOTIFICATION_ID = 0;
    private NotificationManager manager;
    private Context context;
    public NotificationHandler(Context context) {
        this.context = context;
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }
    private void createChannel(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Shop Notification", NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableVibration(true);
        channel.setDescription("Notifications from IT Webshop application.");
        this.manager.createNotificationChannel(channel);
    }
    public void send(String msg){
        createChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("IT Webshop")
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_shopping_cart);

        this.manager.notify(NOTIFICATION_ID, builder.build());

    }
    public void cancel(){
        this.manager.cancel(NOTIFICATION_ID);
    }
}
