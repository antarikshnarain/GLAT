package com.lasser.play.geomania;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by antar on 06-Feb-17.
 */

public class CustomGroupListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemTitle;
    private final String[] itemContent;
    private final Integer imgid;

    public CustomGroupListAdapter(Activity context, String[] itemTitle, String[] itemContent, Integer imgid) {
        super(context, R.layout.my_list_group_gps, itemTitle);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemTitle=itemTitle;
        this.itemContent = itemContent;
        this.imgid=imgid;
    }
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.my_list_group_gps, null,true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.listTextView_Title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.listTextView_Content);

        txtTitle.setText(itemTitle[position]);
        //imageView.setImageResource(imgid);
        extratxt.setText("Description "+itemContent[position]);
        return rowView;

    };
}
