package com.lasser.play.geomania.CustomDataStructure;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by antar on 31-Mar-17.
 */

public class UserSendMessage implements Serializable {
    public double gps_longi;
    public double gps_lati;
    public int type;
    public Calendar date;
    public MessageData msg;

    public UserSendMessage(){
        gps_lati = 0.0;
        gps_longi = 0.0;
        type=1;
        date = Calendar.getInstance();
        msg = new MessageData();
    }
    public class MessageData{
        public String text;
        public String media;

        MessageData(){
            text="";
            media="";
        }
    }
}

