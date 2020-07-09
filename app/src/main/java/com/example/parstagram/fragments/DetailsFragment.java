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
import android.widget.ImageButton;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {

    public static final String TAG = DetailsFragment.class.getSimpleName();
    public RecyclerView rvComments;
    public CommentsAdapter adapter;
    public List<Comment> allComments;
    protected Post post;
    TextView tvUsername;
    ImageView ivPFP;
    TextView tvTime;
    TextView tvUsernameComment;
    ImageView ivImage;
    ImageButton ibtnLike;
    ImageButton ibtnComment;
    TextView tvLikes;
    TextView tvComments;
    int likes;
    boolean liked;

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
        ibtnComment = (ImageButton) view.findViewById(R.id.ibtnComment);
        ibtnLike = (ImageButton) view.findViewById(R.id.ibtnLike);
        tvLikes = (TextView) view.findViewById(R.id.tvLikes);
        tvComments = (TextView) view.findViewById(R.id.tvComments);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

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
                Log.d(TAG, "Querying for refresh ");
                queryComments(0);
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
                Log.d(TAG, "Querying for load more ");
                queryComments(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvComments.addOnScrollListener(scrollListener);

        // Get comments
        Log.d(TAG, "Querying for initial retrieval ");
        queryComments(0);

        // See if the user liked the post
        liked = false;
        queryLiked();

        // When the user clicks the heart, change accordingly
        ibtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!liked) {
                    post.addLike();
                    addLike();
                } else {
                    post.removeLike();
                    removeLike();
                }
            }
        });

        // Set comment listener
        ibtnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentDialogFragment commentDialogFragment = CommentDialogFragment.newInstance(post);
                commentDialogFragment.show(((MainActivity) view.getContext()).fragmentManager, "fragment_comment_dialog");
            }
        });
    }

    // Query comments from database
    protected void queryComments(int page) {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.include(Comment.KEY_USER);
        query.setSkip(20 * page);
        query.whereEqualTo(Comment.KEY_OPOST, post);
        query.setLimit(20); // Only show 20 comments
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
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
                LinkedHashSet<Comment> temp = new LinkedHashSet<>(allComments);
                allComments.clear();
                allComments.addAll(temp); // Remove duplicates
                adapter.notifyDataSetChanged();
                setCommentCount();
            }
        });
    }

    // Set the count number of comments
    public void setCommentCount() {
        if (allComments.size() > 0) {
            tvComments.setText(format(allComments.size()));
        } else {
            tvComments.setText("");
        }
    }

    // Query if the post is liked from database
    protected void queryLiked() {
        post.getLikes().getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting likes", e);
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Query likes success!");
                // If the user liked the post, show that. Otherwise, show the post is not liked
                removeLike();
                likes = users.size();
                if (likes > 0) {
                    tvLikes.setText(format(likes));
                } else {
                    tvLikes.setText("");
                }
                for(int i = 0; i < users.size(); i++) {
                    if(users.get(i).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                        addLike();
                        break;
                    }
                }
            }
        });
    }

    private void addLike() {
        liked = true;
        ibtnLike.setSelected(true);
        ibtnLike.setImageResource(R.drawable.ufi_heart_active);
    }

    private void removeLike() {
        liked = false;
        ibtnLike.setSelected(false);
        ibtnLike.setImageResource(R.drawable.ufi_heart);
    }

    // Truncate counts in a readable format
    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value);

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}