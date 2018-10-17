package com.opengarden.test.server.model;

import java.text.SimpleDateFormat;

public class ChatMessage {
	// The different types of message sent by the Client
	public static final int MESSAGE = 0, LOGOUT = 1, LOGIN = 2;
    private int mID;
    private int mType;
    private String mMessage;
    private String mUsername;
    private long mDate;

    //Empty Constructor
    public ChatMessage() {

    }

    public ChatMessage(int id, String username, int type, String message, long date) {
        mID = id;
        mUsername = username;
        mType = type;
        mDate = date;
        mMessage = message;
    }


    public int getID() {
        return mID;
    }

    public void setID(int id) {
        mID = id;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }
    
    @Override
    public String toString() {
    	String msg = "";
		switch(mType){
		case ChatMessage.LOGIN:
			msg = mMessage = mUsername + " just joined" + "\n";
			break;
		case ChatMessage.LOGOUT:
			//msg = mMessage  + "\n";
			msg = mMessage = mUsername + " has logged out." + "\n";
			break;
		case ChatMessage.MESSAGE:
			// this is different from the android client
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
			String time = simpleDateFormat.format(mDate);
			msg = time + " " + mUsername + ": " + mMessage + "\n";

			break;
		default: msg = "Invalid operation." 
            		break; 		
		}
		
		
		return msg;
    }

}


