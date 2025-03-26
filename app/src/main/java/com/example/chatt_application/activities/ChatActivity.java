package com.example.chatt_application.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatt_application.R;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.example.chatt_application.adapters.ChatAdapter;
import com.example.chatt_application.databinding.ActivityChatBinding;
import com.example.chatt_application.models.ChatMessage;
import com.example.chatt_application.models.User;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;

    private Boolean isReceiverOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        loadReciverDetails();
        init();
        listenMessages();

        binding.imageInfo.setOnClickListener(v -> showReceiverInfoDialog());



        // For checkUp you can check that database is connected or not by this code

     /*   FirebaseFirestore.getInstance().collection("users").document(receiverUser.id).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String email = documentSnapshot.getString("email");  // Directly fetch email
                Log.d("FirestoreDebug", "Extracted Email: " + email);

                if (email == null) {
                    Log.e("FirestoreError", "Email is NULL in Firestore!");
                }
            } else {
                Log.e("FirestoreError", "Document does not exist");
            }
        }).addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching receiver data", e));

      */

    }

    @SuppressLint("SetTextI18n")
    private void showReceiverInfoDialog() {
        if (receiverUser == null || receiverUser.id == null) {
            Log.e("FirestoreError", "Receiver user data is null!");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(receiverUser.id).get().addOnSuccessListener(documentSnapshot -> {
            String email = documentSnapshot.getString("email");
            if (email == null) email = "Not available"; // Default fallback

            AlertDialog.Builder builder = new AlertDialog.Builder(this);


            // Create main layout
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(60, 40, 60, 40);
            layout.setGravity(Gravity.CENTER);
            layout.setElevation(8f); // Subtle shadow effect


            // Profile Image
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (receiverUser.image != null && !receiverUser.image.isEmpty()) {
                try {
                    Bitmap bitmap = getBitmapFromEncodedString(receiverUser.image);
                    RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    roundedBitmap.setCircular(true);
                    imageView.setImageDrawable(roundedBitmap);
                } catch (Exception e) {
                    Log.e("ImageError", "Error loading image", e);
                    imageView.setImageResource(R.drawable.background_image);
                }
            } else {
                imageView.setImageResource(R.drawable.background_image);
            }

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(300, 300);
            imageParams.setMargins(0, 10, 0, 20);
            imageView.setLayoutParams(imageParams);


            // Divider Line
            View divider1 = new View(this);
            divider1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2 // Thickness of line
            ));
            divider1.setBackgroundColor(Color.parseColor("#000000"));
            divider1.setPadding(0, 5, 0, 5);


            // Title
            TextView titleTextView = new TextView(this);
            titleTextView.setText("Receiver Info");
            titleTextView.setTextSize(22);
            titleTextView.setTypeface(null, Typeface.BOLD);
            titleTextView.setPadding(5, 5, 0, 9);

            // Name
            TextView nameTextView = new TextView(this);
            nameTextView.setText("Name: " + receiverUser.name);
            nameTextView.setTextSize(16);
            nameTextView.setTypeface(null, Typeface.BOLD);
            nameTextView.setPadding(0, 10, 0, 5);

            // Email
            TextView emailTextView = new TextView(this);
            emailTextView.setText("Email: " + email);
            emailTextView.setTextSize(16);
            emailTextView.setTypeface(null, Typeface.BOLD);
            emailTextView.setPadding(0, 5, 0, 5);

            // Status
            TextView statusTextView = new TextView(this);
            statusTextView.setText("Status: " + (isReceiverOnline ? "Online" : "Offline"));
            statusTextView.setTypeface(null, Typeface.BOLD);
            statusTextView.setTextSize(18);
            statusTextView.setGravity(Gravity.CENTER);
            statusTextView.setPadding(0, 10, 0, 15);
            statusTextView.setTextColor(isReceiverOnline ? Color.parseColor("#388E3C") : Color.RED); // Green for online, Red for offline

            // Add views to layout
            layout.addView(titleTextView);
            layout.addView(divider1);
            layout.addView(statusTextView);
            layout.addView(imageView);
            layout.addView(nameTextView);
            layout.addView(emailTextView);


            builder.setView(layout);
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
        }).addOnFailureListener(e -> Log.e("FirestoreError", "Error fetching receiver data", e));
    }


    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, getBitmapFromEncodedString(receiverUser.image), preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerReview.setAdapter(chatAdapter);
    }


    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();

        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        if (conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);

        }

        binding.inputMessage.setText(null);

    }

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = value.getLong(Constants.KEY_AVAILABILITY).intValue();
                    if (availability == 1) {
                        isReceiverOnline = true;
                    } else {
                        isReceiverOnline = false;

                    }
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);

            }
            if (isReceiverOnline) {
                binding.textAvailability.setVisibility((View.VISIBLE));
            } else {
                binding.textAvailability.setVisibility((View.GONE));
            }

        });

    }


    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id).addSnapshotListener(eventListener);

        database.collection(Constants.KEY_COLLECTION_CHAT).whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id).whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }

            Collections.sort(chatMessages, (obj1, obj2) -> {
                if (obj1.dataObject == null || obj2.dataObject == null) return 0;
                return obj1.dataObject.compareTo(obj2.dataObject);
            });


            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(count, Math.abs(chatMessages.size() - count));
                binding.chatRecyclerReview.smoothScrollToPosition(chatMessages.size() - 1);

            }
            binding.chatRecyclerReview.setVisibility(View.VISIBLE);

        }
        binding.progressBar.setVisibility(View.GONE);

        if (conversationId == null) {
            checkForConversation();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //    private void loadReciverDetails() {
//
//
//        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
//        binding.textName.setText(receiverUser.name);
//    }
    private void loadReciverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

        // Fetch receiver's image from Firestore
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                receiverUser.image = documentSnapshot.getString(Constants.KEY_IMAGE);
            }
        }).addOnFailureListener(e -> {
            // Handle error
            e.printStackTrace();
        });
    }


    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> {
            sendMessage();
        });


    }


    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }


    private void updateConversation(String message) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);

        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).add(conversation).addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }


    private void checkForConversation() {
        if (!chatMessages.isEmpty()) {
            checkForConersationRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUser.id);
            checkForConersationRemotely(receiverUser.id, preferenceManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkForConersationRemotely(String senderId, String ReceiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).whereEqualTo(Constants.KEY_SENDER_ID, senderId).whereEqualTo(Constants.KEY_RECEIVER_ID, ReceiverId).get().addOnCompleteListener(querySnapshotOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> querySnapshotOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}












