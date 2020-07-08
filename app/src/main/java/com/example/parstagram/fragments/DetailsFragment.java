package com.example.parstagram.fragments;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parstagram.EndlessRecyclerViewScrollListener;
import com.example.parstagram.R;
import com.example.parstagram.TimeFormatter;
import com.example.parstagram.activities.MainActivity;
import com.example.parstagram.adapters.CommentsAdapter;
import com.example.parstagram.models.Comment;
import com.example.parstagram.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {

    public static final String TAG = DetailsFragment.class.getSimpleName();
    protected RecyclerView rvComments;
    protected CommentsAdapter adapter;
    protected List<Comment> allComments;
    protected Post post;
    TextView tvUsername;
    ImageView ivPFP;
    TextView tvTime;
    TextView tvUsernameComment;
    ImageView ivImage;
    int skip;

    // Swipe to refresh and scroll to load more comments endlessly
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(Post post) {
        DetailsFragment frag = new DetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("post", Parcels.wrap(post));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvComments = (RecyclerView) view.findViewById(R.id.rvComments);
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        ivPFP = (ImageView) view.findViewById(R.id.ivPFP);
        tvTime = (TextView) view.findViewById(R.id.tvTime);
        tvUsernameComment = (TextView) view.findViewById(R.id.tvUsernameComment);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        skip = 0;

        post = Parcels.unwrap(getArguments().getParcelable("post"));
        ParseUser op = post.getUser();

        // Set views
        tvUsername.setText(op.getUsername());
        // Set the username bold and description not bold
        SpannableString str =  new SpannableString(tvUsername.getText());
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
        tvUsernameComment.setText(str);
        tvUsernameComment.append("  " + post.getDescription());

        // Set the time to the correct format
        tvTime.setText(TimeFormatter.getTimeDifference(post.getCreatedAt().toString()));

        // Set the images if we have them
        ParseFile image = op.getParseFile("pfp");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).circleCrop().into(ivPFP);
        } else {
            Glide.with(this).load(R.drawable.default_pfp).circleCrop().into(ivPFP);
        }
        image = post.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivImage);
        }

        // Set comments, adapter, and layout
        allComments = new ArrayList<>();
        adapter = new CommentsAdapter(getContext(), allComments);
        rvComments.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvComments.setLayoutManager(linearLayoutManager);

        // When the user clicks on text or a profile picture, take them to the profile page for that user
        View.OnClickListener profileListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getContext()).fragmentManager.beginTransaction().replace(R.id.flContainer, ProfileFragment.newInstance(post.getUser())).commit();
            }
        };
        tvUsername.setOnClickListener(profileListener);
        ivPFP.setOnClickListener(profileListener);

        // Set the refresher
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                skip = 0;
                queryComments();
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.blackPrimary);

        // Retain an instance for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                queryComments();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvComments.addOnScrollListener(scrollListener);

        // Get comments
        queryComments();
    }

    // Query comments from database
    protected void queryComments() {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_USER);
        query.setSkip(skip);
        query.whereEqualTo(Comment.KEY_OPOST, post);
        query.setLimit(20); // Only show 20 comments
        query.addAscendingOrder(Comment.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> comments, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting comments", e);
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Query comments success!");
                allComments.addAll(comments);
                adapter.notifyDataSetChanged();
                skip += comments.size(); // Skip the next values next time
            }
        });
    }
}