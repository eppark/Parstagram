package com.example.parstagram.fragments;

import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parstagram.R;
import com.example.parstagram.models.Comment;
import com.example.parstagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class CommentDialogFragment extends DialogFragment {

    public static final String TAG = CommentDialogFragment.class.getSimpleName();
    Post post;
    EditText etCompose;
    ImageView ivPFP;
    Button btnReply;

    public CommentDialogFragment() {
        // Required empty public constructor
    }

    public interface CommentDialogFragmentListener {
        void onFinishCommentDialog(Comment comment);
    }

    public static CommentDialogFragment newInstance(Post post) {
        CommentDialogFragment fragment = new CommentDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(Post.class.getSimpleName(), Parcels.wrap(post));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Bind views
        etCompose = (EditText) view.findViewById(R.id.etCompose);
        ivPFP = (ImageView) view.findViewById(R.id.ivPFP);
        btnReply = (Button) view.findViewById(R.id.btnReply);
        btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.LightBlue));
        btnReply.setEnabled(false);

        // Fetch post from bundle
        post = Parcels.unwrap(getArguments().getParcelable(Post.class.getSimpleName()));

        // Show the profile pic if the user has one
        ParseFile image = ParseUser.getCurrentUser().getParseFile("pfp");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).circleCrop().into(ivPFP);
        } else {
            Glide.with(this).load(R.drawable.default_pfp).circleCrop().into(ivPFP);
        }

        // Set text listener
        etCompose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Integer counter = editable.toString().length();
                if (counter <= 0) {
                    btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.LightBlue));
                    btnReply.setEnabled(false);
                } else {
                    btnReply.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.InstagramBlue));
                    btnReply.setEnabled(true);
                }
            }
        });

        // Set click listener on button
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save comment
                Comment comment = saveComment(etCompose.getText().toString());
                // Return comment back to activity
                CommentDialogFragmentListener listener = (CommentDialogFragmentListener) getActivity();
                listener.onFinishCommentDialog(comment);
                dismiss(); // Close the dialog and return back to the parent activity
            }
        });

        // Show soft keyboard automatically and request focus to field
        etCompose.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private Comment saveComment(String description) {
        Comment comment = new Comment();
        comment.setDescription(description);
        comment.setOriginalPost(post);
        comment.setUser(ParseUser.getCurrentUser());
        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving comment!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Comment save success!");
            }
        });
        return comment;
    }

    public void onResume() {
        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();
        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        // Set the width of the dialog proportional to 95% of the screen width
        window.setLayout((int) (size.x * 0.95), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        // Call super onResume after sizing
        super.onResume();
    }
}