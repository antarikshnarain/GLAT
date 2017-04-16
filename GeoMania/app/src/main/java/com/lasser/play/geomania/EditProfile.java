package com.lasser.play.geomania;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static com.lasser.play.geomania.GroupView.MyPREFERENCES;
import static org.json.JSONObject.NULL;

public class EditProfile extends AppCompatActivity {

    EditText editText;
    Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editText=(EditText) findViewById(R.id.editName);

        SharedPreferences phoneDetails = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        final String nameValue = phoneDetails.getString("NameKey", "");

        editText.setText(nameValue);


        /*
        IMAGE change CODE HERE



         */

        editButton=(Button) findViewById(R.id.submitButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             /*   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/



                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


                String newName=editText.getText().toString();

                SharedPreferences phoneDetails = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

                String PhoneValue = phoneDetails.getString("PhoneKey", "");
                String TokenValue = phoneDetails.getString("TokenKey", "");

                JSONObject requestMap = new JSONObject();



                try {


                    requestMap.put("phone", PhoneValue);
                    requestMap.put("token", TokenValue);
                    requestMap.put("newname",newName);
                    //requestMap.put("imageDetails",);     //ADD IMAGE DETAILS HERE


                }
                catch (JSONException e){
                    e.printStackTrace();

                }


                URLDataHash mydata = new URLDataHash();
                mydata.url = "192.168.43.231";
                mydata.apicall = "user/edit/profile";
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
}
