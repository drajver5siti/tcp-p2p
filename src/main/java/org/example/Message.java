package org.example;

import com.google.gson.Gson;

import java.util.List;

public class Message {
    public String sender;
    public String receiver;
    public String messageText;
    public List<String> fileContents;

    public Message (String sender, String receiver, String messageText, List<String> fileContents) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageText = messageText;
        this.fileContents = fileContents;
    }

    public String toJSON() {
        return (new Gson()).toJson(this);
    }

    public static Message fromJSON(String json) {
        return (new Gson()).fromJson(json, Message.class);
    }
}
