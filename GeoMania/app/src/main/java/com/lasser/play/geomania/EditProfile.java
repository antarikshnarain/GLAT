package com.lasser.play.geomania;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static org.json.JSONObject.NULL;

public class EditProfile extends Activity {

    EditText editText;
    ImageButton editButton;
    ImageView profilePic;

    SharedFunctions myfunction;

    private boolean update_profile_pic_flag=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        profilePic = (ImageView) findViewById(R.id.imageView_profile_pic);
        editText=(EditText) findViewById(R.id.editName);
        myfunction = new SharedFunctions(this);
        editText.setText(myfunction.user);


        /*
        IMAGE change CODE HERE



         */

        editButton=(ImageButton) findViewById(R.id.sumbitButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update User Information
                try {
                    // Upload Profile Pic
                    //myfunction.uploadFile()

                    String newName = editText.getText().toString();
                    JSONObject requestMap = new JSONObject();
                    requestMap.put("phone", myfunction.phone);
                    requestMap.put("token", myfunction.token);
                    requestMap.put("newname", newName);
                    //requestMap.put("imageDetails",);     //ADD IMAGE DETAILS HERE

                    URLDataHash mydata = new URLDataHash();
                    mydata.url = "192.168.43.231";
                    mydata.apicall = "user/edit/profile";
                    mydata.jsonData = requestMap;

                    JSONObject data = new nodeHttpRequest(getApplicationContext()).execute(mydata).get();
                    Log.d("MYAPP:", data.toString());


                    String response = data.toString();

                    if (response == NULL) {
                        Log.d("NULL OBJECT", "");
                    } else {
                        Toast.makeText(getApplicationContext(), "Profile Has been edited", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == myfunction.GALLERY_IMAGE){
            // Gallery Images
            if (resultCode == Activity.RESULT_OK){
                Uri selectedImage = data.getData();
                String filePath = myfunction.getRealPathFromURI(selectedImage);
                profilePic.setImageBitmap(myfunction.resizeBitmap(filePath));
                update_profile_pic_flag = true;
            }
        }
    }
    public void changeProfilePic(View v){
        update_profile_pic_flag = false;
        Intent intent = new Intent();
        intent.setType("image/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Images"),myfunction.GALLERY_IMAGE);
    }
}
