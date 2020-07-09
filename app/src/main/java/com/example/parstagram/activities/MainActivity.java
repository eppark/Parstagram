package com.example.parstagram.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.parstagram.R;
import com.example.parstagram.databinding.ActivityMainBinding;
import com.example.parstagram.fragments.CommentDialogFragment;
import com.example.parstagram.fragments.ComposeFragment;
import com.example.parstagram.fragments.DetailsFragment;
import com.example.parstagram.fragments.PostsFragment;
import com.example.parstagram.fragments.ProfileFragment;
import com.example.parstagram.models.Comment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity implements CommentDialogFragment.CommentDialogFragmentListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public final FragmentManager fragmentManager = getSupportFragmentManager();
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextAppearance(this, R.style.InstagramTextAppearance);
        getSupportActionBar().setTitle("Instagram");
        binding.toolbar.setTitle("Instagram");

        // Set the bottom navigation view
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_profile:
                    default:
                        fragment = ProfileFragment.newInstance(ParseUser.getCurrentUser());
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });

        // Set default selection
        binding.bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    // When returning from the comment dialog, show it on the feed
    @Override
    public void onFinishCommentDialog(Comment comment) {
        ((DetailsFragment) fragmentManager.findFragmentByTag("DETAILS_TAG")).allComments.add(0, comment);
        ((DetailsFragment) fragmentManager.findFragmentByTag("DETAILS_TAG")).adapter.notifyDataSetChanged();
    }
}