package com.lasser.play.geomania;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.ContactsClass;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.lasser.play.geomania.GroupView.MyPREFERENCES;
import static org.json.JSONObject.NULL;

public class GroupManager extends AppCompatActivity {
    TextView textView;
    ListView listview;
    ListView listview1;
    EditText groupName;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("BL","Before layout");

        setContentView(R.layout.activity_group_manager);
     /*   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
*/
        Log.d("AL","After layout");

        //setContentView(R.layout.content_display_contacts);

        listview = (ListView) findViewById(R.id.contactsView);

        listview1 =(ListView) findViewById(R.id.membersView);

        textView =(TextView) findViewById(R.id.response);




        try {
            showContacts();
        }
        catch (JSONException e){}

        // getUserPhoneNumber();


    }

    private void getUserPhoneNumber()
    {

        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();

        //Toast.makeText(getApplicationContext(), mPhoneNumber, Toast.LENGTH_LONG).show();


        LinearLayout lView = new LinearLayout(this);

        TextView myText = new TextView(this);
        myText.setText("My Text");

        lView.addView(myText);

        setContentView(lView);
    }

    private JSONArray getContactNames() throws JSONException
    {

        Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        String aNameFromContacts[] = new String[contacts.getCount()];
        String aNumberFromContacts[] = new String[contacts.getCount()];
        int i = 0;

        int nameFieldColumnIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numberFieldColumnIndex = contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        List<ContactsClass> contactObjects=new ArrayList<ContactsClass>();

        JSONArray returnContacts=new JSONArray();
        while(contacts.moveToNext()) {

            JSONObject currJsonContact=new JSONObject();
            ContactsClass newcontactObject=new ContactsClass();


            String contactName = contacts.getString(nameFieldColumnIndex);
            aNameFromContacts[i] =    contactName ;
            contactName=contactName.replaceAll("'","\'");
            currJsonContact.put("name",contactName);


            String number = contacts.getString(numberFieldColumnIndex);
            aNumberFromContacts[i] =    number ;
            number=number.replaceAll("'","\'");
            currJsonContact.put("phone",number);

            i++;

            returnContacts.put(currJsonContact);
        }

        contacts.close();

        return returnContacts;
    }



    private void showContacts() throws JSONException {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        else {
            // Android version is lesser than 6.0 or the permission is already granted.
            //String[] contacts = getContactNames();

            JSONArray contactObjects=getContactNames();

            /*for(ContactsClass obj:contactObjects)
            {
                Log.d("Currnumber ",obj.number);
                Log.d("Currname",obj.name);

            }*/


            /*ArrayAdapter adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,
                    contacts);
            listview.setAdapter(adapter);
        */

            /*
            String contactString = new String();

            for (String s : contacts)
                contactString += s + ',';
            Log.d("CONTACTS",contactString);
            */

            groupName=(EditText) findViewById(R.id.groupName);

            final String gName=groupName.getText().toString();
            SharedPreferences phoneDetails = getSharedPreferences("userdata",Context.MODE_PRIVATE);

            String PhoneValue = phoneDetails.getString("phone", "");
            String TokenValue = phoneDetails.getString("token", "");
            Log.d("MYAPP: phone GM",phoneDetails.getString("phone",""));
            Log.d("MYAPP: token GM", phoneDetails.getString("token",""));
            JSONObject requestMap = new JSONObject();



            try {


                requestMap.put("phone", PhoneValue);
                requestMap.put("token", TokenValue);
                requestMap.put("contacts",contactObjects);
            }
            catch (JSONException e){
                e.printStackTrace();

            }




            Log.d("My OUTPUT", PhoneValue + " " + TokenValue);

            Log.d("My requestMap", contactObjects.toString());

            URLDataHash mydata = new URLDataHash();
            mydata.url = "192.168.43.231";
            mydata.apicall = "user/contacts/view";
            mydata.jsonData=requestMap;

            try {

                final JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
                Log.d("MYAPP:", data.toString());


                String response = data.toString();
                textView = (TextView) findViewById(R.id.response);

                if(response==NULL)
                {
                    textView.setText("NULL object");
                }
                else {
                    //textView.setText(response);
                    try {
                        JSONArray members = data.getJSONArray("resp");
                        JSONObject currentObj=new JSONObject();
                        final JSONObject NumbersHash=new JSONObject();
                        final ArrayList<String> membersGroup=new ArrayList<String>();
                        for(int i=0;i<members.length();i++)
                        {
                            currentObj=members.getJSONObject(i);
                            Log.d("MYAPP: Json Parse",currentObj.toString());
                            membersGroup.add(currentObj.getString("dname"));
                            NumbersHash.put(currentObj.getString("dname"),currentObj.getString("phone"));


                            Log.d("MYAPP: members group",membersGroup.toString());
                        }
                        String testString=new String();

                        for(String curr:membersGroup)
                            testString+=curr+",";


                        Log.d("member length",""+ membersGroup.size());
                        Log.d("Returned names",testString);

                        final ArrayList<String> latestMembers=new ArrayList<String>();

                        membersGroup.add("Praful");

                        final ArrayAdapter adapter = new ArrayAdapter<String>(this,
                                android.R.layout.simple_list_item_1,
                                membersGroup);


                        listview.setAdapter(adapter);


                        final ArrayAdapter adaptermembers=new ArrayAdapter<String>(this,
                                android.R.layout.simple_list_item_1,
                                latestMembers);
                        listview1.setAdapter(adaptermembers);


                        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                String item = ((TextView)view).getText().toString();
                                membersGroup.remove(item);

                                latestMembers.add(item);

                                adapter.notifyDataSetChanged();
                                adaptermembers.notifyDataSetChanged();
                            }
                        });

                        listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                String item = ((TextView)view).getText().toString();
                                membersGroup.add(item);

                                latestMembers.remove(item);

                                adapter.notifyDataSetChanged();
                                adaptermembers.notifyDataSetChanged();
                            }
                        });




                        textView.setText("Memebers in the group");





                        FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);

                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
             /*   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/



                                groupName=(EditText) findViewById(R.id.groupName);

                                final String gName=groupName.getText().toString();

                                SharedPreferences phoneDetails = getSharedPreferences("userdata",Context.MODE_PRIVATE);

                                String PhoneValue = phoneDetails.getString("phone", "");
                                String TokenValue = phoneDetails.getString("token", "");

                                ArrayList<String> latestNumbers=new ArrayList<>();

                                try {
                                    for (String current : latestMembers)
                                        latestNumbers.add(NumbersHash.getString(current));
                                }
                                catch (JSONException e){}

                                JSONObject requestMap=new JSONObject();

                                try {
                                    requestMap.put("phone", PhoneValue);
                                    requestMap.put("token",TokenValue);
                                    requestMap.put("gname",gName);
                                    requestMap.put("mems",new JSONArray(latestNumbers));
                                }
                                catch(JSONException e){}

                                URLDataHash mydata = new URLDataHash();
                                mydata.url = "192.168.43.231";
                                mydata.apicall = "group/add";
                                mydata.jsonData=requestMap;

                                try {

                                    JSONObject data = new nodeHttpRequest(getApplicationContext()).execute(mydata).get();
                                    Log.d("Response Group ",data.toString());
                                }
                                catch (InterruptedException e){
                                    e.printStackTrace();

                                }
                                catch (ExecutionException e){
                                    e.printStackTrace();

                                }


                                Toast.makeText(getApplicationContext(), "Group has been made",
                                        Toast.LENGTH_LONG).show();

                                Intent Intent = new Intent(view.getContext(), GroupManager.class);
                                view.getContext().startActivity(Intent);
                            }
                        });






                    }
                    catch(JSONException e){}


                }


            }


            catch (InterruptedException e){
                e.printStackTrace();

            }
            catch (ExecutionException e){
                e.printStackTrace();

            }

        }
    }



    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions,
                                            int[] grantResults)  {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted

                try {
                    showContacts();
                }catch (JSONException e){}

            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

