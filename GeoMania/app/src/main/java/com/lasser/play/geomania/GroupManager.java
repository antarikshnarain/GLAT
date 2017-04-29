package com.lasser.play.geomania;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.ContactsClass;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.json.JSONObject.NULL;

public class GroupManager extends AppCompatActivity {
    ListView listview;
    ListView listview1;
    EditText groupName;
    ImageView groupIcon;
    String group_name, group_icon, group_id;

    ProgressDialog progressDialog;

    SharedFunctions myfunction;
    ArrayList<String> latestNumbers = new ArrayList<>();
    int GALLERY_IMAGE = 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        group_name = intent.getStringExtra("title");
        group_icon = intent.getStringExtra("icon");
        group_id = intent.getStringExtra("gid");
        setContentView(R.layout.activity_group_manager);

        groupName = (EditText) findViewById(R.id.groupName);
        listview = (ListView) findViewById(R.id.contactsView);
        listview1 = (ListView) findViewById(R.id.membersView);
        groupIcon = (ImageView) findViewById(R.id.image_group_icon);
        // Get Read Contacts Permission
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 203);
        }
        progressDialog = new ProgressDialog(this);
        myfunction = new SharedFunctions(this);
        groupName.setText(group_name);
        if(group_icon.equals("") || group_icon.equals("null")){
            groupIcon.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            groupIcon.setImageBitmap(myfunction.resizeBitmap(group_icon));
        }
        progressDialog.setTitle(":((())):");
        progressDialog.setMessage("Retrieving Group Information ...");
        progressDialog.show();
        showContacts();
        progressDialog.dismiss();
        // getUserPhoneNumber();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == GALLERY_IMAGE){
            // Gallery Images
            if (resultCode == Activity.RESULT_OK){
                Uri selectedImage = data.getData();
                String filePath = getRealPathFromURI(selectedImage);
                groupIcon.setImageBitmap(myfunction.resizeBitmap(filePath));
            }
        }
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
    // OnClick Function
    public void changeGroupIcon(View v){
        Intent intent = new Intent();
        intent.setType("image/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Images"),GALLERY_IMAGE);
    }
    // Get all the phone book contacts
    private JSONArray getContactNames() throws JSONException {
        Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String aNameFromContacts[] = new String[contacts.getCount()];
        String aNumberFromContacts[] = new String[contacts.getCount()];
        int i = 0;
        int nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numberFieldColumnIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        JSONArray returnContacts = new JSONArray();
        while (contacts.moveToNext()) {
            JSONObject currJsonContact = new JSONObject();
            String contactName = contacts.getString(nameFieldColumnIndex);
            aNameFromContacts[i] = contactName;
            contactName = contactName.replaceAll("'", "\'");
            currJsonContact.put("name", contactName);
            String number = contacts.getString(numberFieldColumnIndex);
            aNumberFromContacts[i] = number;
            number = number.replaceAll("'", "\'");
            currJsonContact.put("phone", number);
            i++;
            returnContacts.put(currJsonContact);
        }
        contacts.close();
        return returnContacts;
    }
    // Show members and non members of the Group
    private void showContacts() {
        try {
            JSONArray contactObjects = getContactNames();
            JSONObject requestMap = new JSONObject();
            requestMap.put("phone", myfunction.phone);
            requestMap.put("token", myfunction.token);
            requestMap.put("contacts", contactObjects);
            //Log.d("MYAPP Contacts:", contactObjects.toString());
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "user/contacts/view";
            mydata.jsonData = requestMap;
            // Sending Request to server
            final JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if (data == null) {
                // No Data found
                return;
            }
            JSONArray members = data.getJSONArray("resp");
            JSONObject currentObj;
            final JSONObject NumbersHash = new JSONObject();
            final ArrayList<String> membersGroup = new ArrayList<String>();
            for (int i = 0; i < members.length(); i++) {
                currentObj = members.getJSONObject(i);
                //Log.d("MYAPP: Json Parse", currentObj.toString());
                membersGroup.add(currentObj.getString("dname"));
                NumbersHash.put(currentObj.getString("dname"), currentObj.getString("phone"));
                //Log.d("MYAPP: members group", membersGroup.toString());
            }
            Log.d("MYAPP: Members", "Total Members: " + membersGroup.size());
            final ArrayList<String> latestMembers = new ArrayList<String>();
            membersGroup.add("Praful");
            final ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, membersGroup);
            listview.setAdapter(adapter);
            final ArrayAdapter adaptermembers = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, latestMembers);
            listview1.setAdapter(adaptermembers);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = ((TextView) view).getText().toString();
                    membersGroup.remove(item);
                    latestMembers.add(item);
                    adapter.notifyDataSetChanged();
                    adaptermembers.notifyDataSetChanged();
                }
            });
            listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = ((TextView) view).getText().toString();
                    membersGroup.add(item);
                    latestMembers.remove(item);
                    adapter.notifyDataSetChanged();
                    adaptermembers.notifyDataSetChanged();
                }
            });
            for (String current : latestMembers)
                latestNumbers.add(NumbersHash.getString(current));
        }
        catch (JSONException e) { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }
    // OnClick UpdateGroupInformation
    public void UpdateGroupInformation(View v) {
        try {
            // To handle change of Group name
            group_name = groupName.getText().toString();
            JSONObject requestMap = new JSONObject();
            requestMap.put("phone", myfunction.phone);
            requestMap.put("token", myfunction.token);
            requestMap.put("gid", group_id);
            requestMap.put("gname", group_name);
            requestMap.put("mems", new JSONArray(latestNumbers));
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/add";
            mydata.jsonData = requestMap;

            // Making request to server
            JSONObject data = new nodeHttpRequest(getApplicationContext()).execute(mydata).get();
            Log.d("Response Group ", data.toString());
            Toast.makeText(getApplicationContext(), "Group has been Created / Updated", Toast.LENGTH_LONG).show();
            finish();
        }
        catch (JSONException e) { e.printStackTrace();  }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 203) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

