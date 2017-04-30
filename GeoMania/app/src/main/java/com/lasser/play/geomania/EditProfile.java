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
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static org.json.JSONObject.NULL;

public class EditProfile extends Activity {

    EditText editText;
    ImageButton editButton;
    ImageView profilePic;

    int GALLERY_IMAGE = 102;
    private final int imgWidth = 60;
    private final int imgHeight = 60;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePic = (ImageView) findViewById(R.id.imageView_profile_pic);
        editText=(EditText) findViewById(R.id.editName);

        SharedPreferences phoneDetails = getSharedPreferences("userdata", MODE_PRIVATE);

        final String nameValue = phoneDetails.getString("name", "");

        editText.setText(nameValue);


        /*
        IMAGE change CODE HERE



         */

        editButton=(ImageButton) findViewById(R.id.sumbitButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/



                SharedPreferences sharedpreferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);


                String newName=editText.getText().toString();

                SharedPreferences phoneDetails = getSharedPreferences("userdata", MODE_PRIVATE);

                String PhoneValue = phoneDetails.getString("phone", "");
                String TokenValue = phoneDetails.getString("token", "");

                JSONObject requestMap = new JSONObject();



                try {


                    requestMap.put("phone", PhoneValue);
                    requestMap.put("token", TokenValue);
                    requestMap.put("name",newName);
                    //requestMap.put("imageDetails",);     //ADD IMAGE DETAILS HERE


                }
                catch (JSONException e){
                    e.printStackTrace();

                }


                URLDataHash mydata = new URLDataHash();
                mydata.url = "192.168.43.231";
                mydata.apicall = "user/updateProfile";
                mydata.jsonData=requestMap;

                try {

                    JSONObject data = new nodeHttpRequest(getApplicationContext()).execute(mydata).get();
                    Log.d("MYAPP:", data.toString());


                    String response = data.toString();

                    if(response==NULL)
                    {
                        Log.d("NULL OBJECT","");
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Profile Has been edited", Toast.LENGTH_SHORT).show();
                    }

                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                catch (ExecutionException e){
                    e.printStackTrace();
                }

            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == GALLERY_IMAGE){
            // Gallery Images
            if (resultCode == Activity.RESULT_OK){
                Uri selectedImage = data.getData();
                String filePath = getRealPathFromURI(selectedImage);
                profilePic.setImageBitmap(resizeBitmap(filePath));
            }
        }
    }
    public void changeProfilePic(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Images"),GALLERY_IMAGE);
    }
    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public Bitmap resizeBitmap(String filePath){
        // Function to resize Bitmap from File for the Application
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = 1;
        if ((imgWidth > 0) || (imgHeight > 0)) {
            scaleFactor = Math.max(photoW/imgWidth, photoH/imgHeight);
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(filePath, bmOptions);
    }
}
