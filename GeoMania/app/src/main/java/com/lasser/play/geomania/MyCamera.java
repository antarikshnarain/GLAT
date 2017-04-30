package com.lasser.play.geomania;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lasser.play.geomania.CustomDataStructure.Camera2BasicFragment;

public class MyCamera extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_camera, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }
}
