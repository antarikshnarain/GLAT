package com.lasser.play.geomania.CustomDataStructure;

/**
 * Created by antar on 23-Apr-17.
 */

public class MessageFeed {
    public String user;
    public String usericon;
    public String messages;
    public String timestamp;
    public int message_type;
    public MessageFeed(){
        user = "";
        usericon = "null";
        messages = "";
        timestamp = "0000-00-00 HH:mm:ss";
        message_type = -1;
    }
}
