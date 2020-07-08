package com.example.parstagram.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_LIKES = "likes";
    public static final String KEY_CREATED_AT = "createdAt";
    private static final String TAG = Post.class.getSimpleName();

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser parseUser) {
        put(KEY_USER, parseUser);
    }

    public ParseRelation<ParseUser> getLikes() {
        return getRelation(KEY_LIKES);
    }

    public void addLike() {
        getLikes().add(ParseUser.getCurrentUser());
        saveInBackground();
    }

    public void removeLike() {
        getLikes().remove(ParseUser.getCurrentUser());
        saveInBackground();
    }

    public void resetLikes() {
        put(KEY_LIKES, new ParseRelation[0]);
    }
}