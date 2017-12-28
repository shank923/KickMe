package com.example.user.kickme;

import com.example.user.kickme.User_Acitivity.User;

import java.util.Date;

/**
 * Created by smthls00 on 23/12/2017.
 */

public class ChatMessage {

    private String messageText;
    private String messageName;
    private String messageSurname;
    private long messageTime;

    public ChatMessage(String messageText, String messageName, String messageSurname) {
        this.messageText = messageText;
        this.messageName = messageName;
        this.messageSurname = messageSurname;

        // Initialize to current time
        messageTime = new Date().getTime();
    }

    public ChatMessage(){

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getMessageSurname() {
        return messageSurname;
    }

    public void setMessageSurname(String messageSurname) {
        this.messageSurname = messageSurname;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
