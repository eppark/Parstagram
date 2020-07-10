package com.example.parstagram.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.parstagram.EndlessRecyclerViewScrollListener;
import com.example.parstagram.R;
import com.example.parstagram.adapters.MessagesAdapter;
import com.example.parstagram.databinding.ActivityMainBinding;
import com.example.parstagram.databinding.ActivityMessagesBinding;
import com.example.parstagram.models.Message;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private static final String TAG = MessagesActivity.class.getSimpleName();
    protected List<ParseUser> allUsers;
    protected MessagesAdapter adapter;

    // Swipe to refresh and endless scrolling
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set ViewBinding
        final ActivityMessagesBinding binding = ActivityMessagesBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextAppearance(this, R.style.InstagramTextAppearance);
        getSupportActionBar().setTitle("Instagram");
        binding.toolbar.setTitle("Instagram");

        // Set message user list, adapter, and layout
        allUsers = new ArrayList<>();
        adapter = new MessagesAdapter(this, allUsers);
        binding.rvMessages.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvMessages.setLayoutManager(linearLayoutManager);

        // Set the refresher
        // Setup refresh listener which triggers new data loading
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryMessages(0);
                binding.swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(R.color.blackPrimary);

        // Retain an instance for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                queryMessages(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        binding.rvMessages.addOnScrollListener(scrollListener);

        // Get initial messages
        queryMessages(0);
    }

    private void queryMessages(int page) {
        // Get users
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereNotEqualTo(ParseUser.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        query.setSkip(20 * page);
        query.setLimit(20); // Only show 20 users
        query.addDescendingOrder(ParseUser.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting users", e);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Query users success!");
                allUsers.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });
    }
}