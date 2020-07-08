package com.example.parstagram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.activities.MainActivity;
import com.example.parstagram.fragments.DetailsFragment;
import com.example.parstagram.models.Post;
import com.example.parstagram.R;
import com.parse.ParseFile;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    private List<Post> posts;

    public ImagesAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        Post currentPost;
        private ImageView ivImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);

            // When the user clicks on the post, take them to the details page for that post
            View.OnClickListener detailsListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) context).fragmentManager.beginTransaction().replace(R.id.flContainer, DetailsFragment.newInstance(currentPost)).commit();
                }
            };
            ivImage.setOnClickListener(detailsListener);
        }

        public void bind(Post post) {
            currentPost = post;
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivImage);
            }
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
