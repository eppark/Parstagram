package com.example.parstagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

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
        private TextView tvDescription;
        private ImageButton ibtnLike;
        private ImageButton ibtnComment;
        private ImageButton ibtnShare;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            ivPFP = (ImageView) itemView.findViewById(R.id.ivPFP);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUsernameComment = (TextView) itemView.findViewById(R.id.tvUsernameComment);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            ibtnLike = (ImageButton) itemView.findViewById(R.id.ibtnLike);
            ibtnComment = (ImageButton) itemView.findViewById(R.id.ibtnComment);
            ibtnShare = (ImageButton) itemView.findViewById(R.id.ibtnShare);
        }

        public void bind(Post post) {
            currentPost = post;
            tvUsername.setText(post.getUser().getUsername());
            tvUsernameComment.setText(tvUsername.getText());
            tvDescription.setText(post.getDescription());

            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
            /*image = post.getUser().getPfp();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPFP);
            }*/
        }
    }
}
