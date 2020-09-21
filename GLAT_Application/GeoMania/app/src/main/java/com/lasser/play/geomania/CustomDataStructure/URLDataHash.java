package com.lasser.play.geomania.CustomDataStructure;

import android.graphics.Bitmap;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by antar on 05-Feb-17.
 */

public class URLDataHash {
    public String url;
    public String apicall;
    public JSONObject jsonData;

    public URLDataHash(){
        url = apicall = "";
        jsonData = new JSONObject();
    }
}
