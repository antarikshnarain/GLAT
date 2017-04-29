package com.lasser.play.geomania.ListAdapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.GroupManager;
import com.lasser.play.geomania.MapsActivity;
import com.lasser.play.geomania.R;

import java.util.ArrayList;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions.*;

/**
 * Created by antar on 16-Apr-17.
 */

public class CustomGroupListAdapter_GroupView  extends ArrayAdapter<String>{
    private final Activity context;
    private final ArrayList<String> groupName;
    private final ArrayList<String> groupIcon;
    private final ArrayList<String> groupUnread;
    private final ArrayList<String> groupId;
    private SharedFunctions myfunctions;

    public CustomGroupListAdapter_GroupView(Activity context, ArrayList<String> groupId, ArrayList<String> groupName, ArrayList<String> groupIcon, ArrayList<String> groupUnread) {
        super(context, R.layout.my_list_group_view, groupName);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.groupName=groupName;
        this.groupIcon=groupIcon;
        this.groupId=groupId;
        this.groupUnread = groupUnread;
    }
    public View getView(final int position, View view, final ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.my_list_group_view, null,true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.textView_groupname);
        TextView txtUnread = (TextView) rowView.findViewById(R.id.textView_unread);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView_groupicon);

        txtTitle.setText(groupName.get(position));
        txtUnread.setText(groupUnread.get(position));
        if(groupIcon.get(position).equals("null")){
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            imageView.setImageBitmap(myfunctions.resizeBitmap(groupIcon.get(position)));
        }
        // To Delete Itself, onClick
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent map_activity_intent = new Intent().setClass(context, MapsActivity.class);
                map_activity_intent.putExtra("title", groupName.get(position));
                map_activity_intent.putExtra("icon", groupIcon.get(position));
                map_activity_intent.putExtra("gid",groupId.get(position));
                context.startActivity(map_activity_intent);
                /*
                Intent group_manager_intent = new Intent().setClass(context, GroupManager.class);
                group_manager_intent.putExtra("title",groupName.get(position));
                group_manager_intent.putExtra("icon",groupIcon.get(position));
                context.startActivity(group_manager_intent);
                */
                //ViewGroup parentView = (ViewGroup) view.getParent();
                //parentView.removeView(view);
            }
        });
        return rowView;
    };
}
