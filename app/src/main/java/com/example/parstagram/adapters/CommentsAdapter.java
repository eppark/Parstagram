package com.example.parstagram.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.R;
import com.example.parstagram.TimeFormatter;
import com.example.parstagram.models.Comment;
import com.parse.ParseFile;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private Context context;
    private List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        Comment currentComment;
        ImageView ivPFP;
        TextView tvUsername;
        TextView tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            ivPFP = (ImageView) itemView.findViewById(R.id.ivPFP);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
        }

        public void bind(Comment comment) {
            currentComment = comment;

            // Set the username bold and description not bold
            SpannableString str =  new SpannableString(comment.getUser().getUsername());
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
            tvUsername.setText(str);
            tvUsername.append("  " + comment.getDescription());

            // Set the time to the correct format
            tvTime.setText(TimeFormatter.getTimeDifference(comment.getCreatedAt().toString()));

            // Put in the image
            ParseFile image = currentComment.getUser().getParseFile("pfp");
            if (image != null) {
                Glide.with(context).load(image.getUrl()).circleCrop().into(ivPFP);
            } else {
                Glide.with(context).load(R.drawable.default_pfp).circleCrop().into(ivPFP);
            }
        }
    }
}
