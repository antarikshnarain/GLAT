package com.lasser.play.geomania.AsyncJava;

import android.graphics.Bitmap;

import java.io.File;
import java.util.HashMap;

/**
 * Created by antar on 05-Feb-17.
 */

public class URLDataHash {
    public String url;
    public String apicall;
    public HashMap<String,Object> hashMap;
    public String attachFile;

    public URLDataHash(){
        url = apicall = "";
        attachFile = null;
        hashMap = null;
    }
}
