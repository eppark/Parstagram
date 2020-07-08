package com.example.parstagram.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

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

    public void resetLikes() { put(KEY_LIKES, new JSONArray()); }

    public ArrayList<String> getLikes() throws JSONException {
        ArrayList<String> likeslist = new ArrayList<String>();
        JSONArray jArray = (JSONArray) getJSONArray(KEY_LIKES);
        if (jArray != null) {
            for (int i = 0; i < jArray.length(); i++) {
                likeslist.add(jArray.getString(i));
            }
        }
        return likeslist;
    }

    public void addLike(String user) {
        add(KEY_LIKES, user);
    }

    public void removeLike(String user) {
        try {
            ArrayList<String> likes = getLikes();
            likes.remove(user);
            put(KEY_LIKES, likes);
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception when removing like", e);
        }
    }
}