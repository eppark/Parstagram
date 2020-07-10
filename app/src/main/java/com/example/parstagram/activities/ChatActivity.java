package com.example.parstagram.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.parstagram.R;
import com.example.parstagram.adapters.ChatAdapter;
import com.example.parstagram.databinding.ActivityChatBinding;
import com.example.parstagram.models.Comment;
import com.example.parstagram.models.Message;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    ActivityChatBinding binding;
    ArrayList<Message> mMessages;
    ChatAdapter mAdapter;
    ParseUser receiver;

    // keep track of initial load to scroll to bottom of the view
    boolean mFirstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextAppearance(this, R.style.InstagramTextAppearance);
        getSupportActionBar().setTitle("Instagram");
        binding.toolbar.setTitle("Instagram");
        mFirstLoad = true;

        // Set user info
        receiver = (ParseUser) Parcels.unwrap(getIntent().getParcelableExtra("receiver"));

        // Setup adapter
        mMessages = new ArrayList<>();
        mAdapter = new ChatAdapter(ChatActivity.this, mMessages);
        binding.rvChat.setAdapter(mAdapter);

        // associate the LayoutManager with the RecylcerView
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setReverseLayout(true);
        binding.rvChat.setLayoutManager(linearLayoutManager);

        // Set up messages and refreshes
        setupMessagePosting();
        myHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);
    }

    // Setup button event handler which posts the entered message to Parse
    void setupMessagePosting() {
        // When send button is clicked, create message object on Parse
        binding.btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = binding.etMessage.getText().toString();
                Message message = new Message();
                message.setDescription(description);
                message.setReceiver(receiver);
                message.setSender(ParseUser.getCurrentUser());
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error while saving", e);
                            Toast.makeText(getApplicationContext(), "Error while saving message!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i(TAG, "Message save success!");
                        refreshMessages();
                    }
                });
                binding.etMessage.setText(null);
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    void refreshMessages() {
        /*// Get messages from database that has the current user as the sender or the receiver
        ParseQuery<Message> senderQuery = ParseQuery.getQuery(Message.class);
        senderQuery.whereEqualTo(Message.KEY_SENDER, ParseUser.getCurrentUser());
        ParseQuery<Message> receiverQuery = ParseQuery.getQuery(Message.class);
        receiverQuery.whereEqualTo(Message.KEY_RECEIVER, ParseUser.getCurrentUser());

        List<ParseQuery<Message>> currentQueries = new ArrayList<ParseQuery<Message>>();
        currentQueries.add(senderQuery);
        currentQueries.add(receiverQuery);
        ParseQuery<Message> currentUserQuery = ParseQuery.or(currentQueries);

        // Get messages from database that has the receiver as the sender or receiver
        ParseQuery<Message> senderQuery2 = ParseQuery.getQuery(Message.class);
        senderQuery2.whereEqualTo(Message.KEY_SENDER, receiver);
        ParseQuery<Message> receiverQuery2 = ParseQuery.getQuery(Message.class);
        receiverQuery2.whereEqualTo(Message.KEY_RECEIVER, receiver);

        List<ParseQuery<Message>> receiverQueries = new ArrayList<ParseQuery<Message>>();
        receiverQueries.add(senderQuery);
        receiverQueries.add(receiverQuery);
        ParseQuery<Message> receiverUserQuery = ParseQuery.or(receiverQueries);
*/
        // Now combine the two together
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);

        query.include(Message.KEY_RECEIVER);
        query.include(Message.KEY_SENDER);
        query.setLimit(20); // Only show 20 messages
        // get the latest 20 messages, order will show up newest to oldest of this group
        query.orderByDescending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {
                    mMessages.clear();
                    mMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged(); // update adapter
                    // Scroll to the bottom of the list on initial load
                    if (mFirstLoad) {
                        binding.rvChat.scrollToPosition(0);
                        mFirstLoad = false;
                    }
                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }

    // Create a handler which can run code periodically
    static final int POLL_INTERVAL = 1000; // milliseconds
    Handler myHandler = new android.os.Handler();
    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            myHandler.postDelayed(this, POLL_INTERVAL);
        }
    };
}