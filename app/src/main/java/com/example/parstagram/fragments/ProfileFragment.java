package com.example.parstagram.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parstagram.helpers.EndlessRecyclerViewScrollListener;
import com.example.parstagram.activities.LoginActivity;
import com.example.parstagram.adapters.ImagesAdapter;
import com.example.parstagram.models.Post;
import com.example.parstagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private static final int PERMISSION_REQUEST_CODE = 40;
    protected RecyclerView rvPosts;
    protected ImagesAdapter adapter;
    protected List<Post> allPosts;
    protected ParseUser user;
    TextView tvUsername;
    ImageView ivPFP;
    Button btnLogout;
    ProgressBar pbLoading;
    int skip;

    // Swipe to refresh and scroll to load more posts endlessly
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    private File photoFile;
    private String photoFileName = "pfp.jpg";

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(ParseUser user) {
        ProfileFragment frag = new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = (RecyclerView) view.findViewById(R.id.rvPosts);
        tvUsername = (TextView) view.findViewById(R.id.tvUsername);
        ivPFP = (ImageView) view.findViewById(R.id.ivPFP);
        btnLogout = (Button) view.findViewById(R.id.btnLogout);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        pbLoading = (ProgressBar) view.findViewById(R.id.pbLoading);
        pbLoading.setVisibility(View.GONE); // Hide at first
        skip = 0;

        // Show the logout button if it's the current user
        user = Parcels.unwrap(getArguments().getParcelable("user"));
        if (user != ParseUser.getCurrentUser()) {
            btnLogout.setVisibility(View.GONE);
        } else  {
            btnLogout.setVisibility(View.VISIBLE);
        }
        tvUsername.setText(user.getUsername());

        // Show the profile pic if the user has one
        ParseFile image = user.getParseFile("pfp");
        if (image != null) {
            Glide.with(this).load(image.getUrl()).circleCrop().into(ivPFP);
        } else {
            Glide.with(this).load(R.drawable.default_pfp).circleCrop().into(ivPFP);
        }

        // Set posts, adapter, and layout
        allPosts = new ArrayList<>();
        adapter = new ImagesAdapter(getContext(), allPosts);
        rvPosts.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rvPosts.setLayoutManager(gridLayoutManager);

        ivPFP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        // Set the refresher
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                skip = 0;
                pbLoading.setVisibility(View.VISIBLE); // Show loading bar
                queryPosts(0);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.blackPrimary);

        // Retain an instance for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                pbLoading.setVisibility(View.VISIBLE); // Show loading bar
                queryPosts(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Log the user out when the button is pressed
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Log the user out of the database and go back to the login screen
                ParseUser.logOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        // Get posts
        pbLoading.setVisibility(View.VISIBLE); // Show loading bar
        queryPosts(0);
    }

    // Query posts from database
    protected void queryPosts(int page) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.setSkip(20 * page);
        query.whereEqualTo(Post.KEY_USER, user);
        query.setLimit(20); // Only show 20 posts
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Query posts success!");
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                pbLoading.setVisibility(View.GONE); // Hide at first
            }
        });
    }

    private void launchCamera() {
        if (!checkPermission()) {
            requestPermission();
        }

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.parstagram.fileprovider", photoFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileProvider);
        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                Glide.with(this).load(takenImage).circleCrop().into(ivPFP);
                photoFile = getPhotoFileUri(photoFileName);
                savePFP(photoFile);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void savePFP(File photoFile) {
        user.put("pfp", new ParseFile(photoFile));
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving PFP!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "PFP save success!");
            }
        });
    }
}