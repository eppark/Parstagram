package com.example.parstagram.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.parstagram.activities.ChatActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context context;
    private List<ParseUser> allMessages;

    public MessagesAdapter(Context context, List<ParseUser> allMessages) {
        this.context = context;
        this.allMessages = allMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new MessagesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseUser message = allMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final String TAG = ViewHolder.class.getSimpleName();
        ParseUser currentMessage;
        ImageView ivPFP;
        TextView tvUsername;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPFP = (ImageView) itemView.findViewById(R.id.ivPFP);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            itemView.setOnClickListener(this);
        }

        public void bind(ParseUser message) {
            currentMessage = message;

            // Set the username bold
            SpannableString str =  new SpannableString(message.getUsername());
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, str.length(), 0);
            tvUsername.setText(str);

            // Put in the image
            ParseFile image = message.getParseFile("pfp");
            if (image != null) {
                Glide.with(context).load(image.getUrl()).circleCrop().into(ivPFP);
            } else {
                Glide.with(context).load(R.drawable.default_pfp).circleCrop().into(ivPFP);
            }
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("receiver", Parcels.wrap(currentMessage));
            context.startActivity(intent);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        allMessages.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<ParseUser> list) {
        allMessages.addAll(list);
        notifyDataSetChanged();
    }
}
