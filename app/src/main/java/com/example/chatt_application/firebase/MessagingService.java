package com.example.chatt_application.firebase;

import android.annotation.SuppressLint;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    //    Log.d("FCM" , "Token: "+token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
       // Log.d("FCM", "Message : " + Objects.requireNonNull(message.getNotification()).getBody());
    }
}
