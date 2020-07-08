package com.example.parstagram.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.TimeFormatter;
import com.example.parstagram.activities.MainActivity;
import com.example.parstagram.fragments.DetailsFragment;
import com.example.parstagram.fragments.ProfileFragment;
import com.example.parstagram.models.Post;
import com.example.parstagram.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = PostsAdapter.class.getSimpleName();
    private Context context;
    private List<Post> posts;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        Post currentPost;
        private TextView tvUsername;
        private ImageView ivPFP;
        private ImageView ivImage;
        private TextView tvUsernameComment;
        private ImageButton ibtnLike;
        private ImageButton ibtnComment;
        private ImageButton ibtnShare;
        private TextView tvTime;
        private boolean liked;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            ivPFP = (ImageView) itemView.findViewById(R.id.ivPFP);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUsernameComment = (TextView) itemView.findViewById(R.id.tvUsernameComment);
            ibtnLike = (ImageButton) itemView.findViewById(R.id.ibtnLike);
            ibtnComment = (ImageButton) itemView.findViewById(R.id.ibtnComment);
            ibtnShare = (ImageButton) itemView.findViewById(R.id.ibtnShare);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);

            // When the user clicks on text or a profile picture, take them to the profile page for that user
            View.OnClickListener profileListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) context).fragmentManager.beginTransaction().replace(R.id.flContainer, ProfileFragment.newInstance(currentPost.getUser())).commit();

                }
            };
            tvUsername.setOnClickListener(profileListener);
            ivPFP.setOnClickListener(profileListener);

            // When the user clicks on the post, take them to the details page for that post
            View.OnClickListener detailsListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) context).fragmentManager.beginTransaction().replace(R.id.flContainer, DetailsFragment.newInstance(currentPost)).commit();
                }
            };
            ivImage.setOnClickListener(detailsListener);
            tvUsernameComment.setOnClickListener(detailsListener);
            tvTime.setOnClickListener(detailsListener);
            ibtnComment.setOnClickListener(detailsListener);

            // When the user clicks the heart, change accordingly
            ibtnLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!liked) {
                        currentPost.addLike();
                        addLike();
                    } else {
                        currentPost.removeLike();
                        removeLike();
                    }
                }
            });
        }

        public void bind(Post post) {
            currentPost = post;
            tvUsername.setText(post.getUser().getUsername());

            // Set the username bold and description not bold
            SpannableString str =  new SpannableString(tvUsername.getText());
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
            tvUsernameComment.setText(str);
            tvUsernameComment.append("  " + post.getDescription());

            // Set the time to the correct format
            tvTime.setText(TimeFormatter.getTimeDifference(post.getCreatedAt().toString()));

            // Set the images if we have them
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
            image = post.getUser().getParseFile("pfp");
            if (image != null) {
                Glide.with(context).load(image.getUrl()).circleCrop().into(ivPFP);
            } else {
                Glide.with(context).load(R.drawable.default_pfp).circleCrop().into(ivPFP);
            }

            // Show if the user likes the post
            queryLiked();
        }

        // Query if the post is liked from database
        protected void queryLiked() {
            currentPost.getLikes().getQuery().findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Issue with getting likes", e);
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "Query likes success!");
                    // If the user liked the post, show that. Otherwise, show the post is not liked
                    removeLike();
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
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
