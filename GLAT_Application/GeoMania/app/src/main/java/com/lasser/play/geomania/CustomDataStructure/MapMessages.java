package com.lasser.play.geomania.CustomDataStructure;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by antar on 22-Apr-17.
 */

public class MapMessages {
    public double latitude;
    public double longitude;
    public String summary;
    public int gid;
    public int mid;
    public String createdby;
    public int message_state;
    public JSONObject sensorData;
    public MapMessages(){
        latitude = longitude = 0.0;
        createdby = summary = "";
        message_state = gid = mid = 0;
        sensorData = new JSONObject();
    }

}
