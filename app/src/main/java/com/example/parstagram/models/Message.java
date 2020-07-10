package com.example.parstagram.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_SENDER = "sender";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_CREATED_AT = "createdAt";

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseUser getSender() {
        return getParseUser(KEY_SENDER);
    }

    public void setSender(ParseUser parseUser) {
        put(KEY_SENDER, parseUser);
    }

    public ParseUser getReceiver() {
        return getParseUser(KEY_RECEIVER);
    }

    public void setReceiver(ParseUser parseUser) {
        put(KEY_RECEIVER, parseUser);
    }
}
