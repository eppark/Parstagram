package com.example.parstagram.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.parstagram.R;
import com.example.parstagram.models.Message;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<Message> mMessages;
    private Context mContext;

    public ChatAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_chat, parent, false);

        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Message message = mMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageOther;
        ImageView imageMe;
        TextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            imageOther = (ImageView)itemView.findViewById(R.id.ivProfileOther);
            imageMe = (ImageView)itemView.findViewById(R.id.ivProfileMe);
            body = (TextView)itemView.findViewById(R.id.tvBody);
        }

        public void bind(Message message) {
            final boolean isMe = message.getSender() != null && message.getSender().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());

            if (isMe) {
                imageMe.setVisibility(View.VISIBLE);
                imageOther.setVisibility(View.GONE);
                body.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            } else {
                imageOther.setVisibility(View.VISIBLE);
                imageMe.setVisibility(View.GONE);
                body.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            }

            final ImageView profileView = isMe ? imageMe : imageOther;
            ParseFile image = message.getSender().getParseFile("pfp");
            if (image != null) {
                Glide.with(mContext).load(image.getUrl()).circleCrop().into(profileView);
            } else {
                Glide.with(mContext).load(R.drawable.default_pfp).circleCrop().into(profileView);
            }
            body.setText(message.getDescription());
        }
    }
}
