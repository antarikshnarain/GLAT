package com.lasser.play.geomania.ListAdapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lasser.play.geomania.R;

import java.util.ArrayList;

/**
 * Created by antar on 06-Feb-17.
 */

public class CustomGroupListAdapter_MediaList extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> itemTitle;
    private final ArrayList<String> imagePath;
    private final int imgWidth = 200;
    private final int imgHeight = 200;
    public CustomGroupListAdapter_MediaList(Activity context, ArrayList<String> itemTitle, ArrayList<String> imagePath) {
        super(context, R.layout.my_list_group_sensors, itemTitle);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.itemTitle=itemTitle;
        this.imagePath=imagePath;
    }
    public View getView(int position, View view, final ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.my_list_media, null,true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.listTextView_Title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.listImageView);
        txtTitle.setText(itemTitle.get(position));
        imageView.setImageBitmap(resizeBitmap(imagePath.get(position)));
        //imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,320,240,true));
        //imageView.setImageResource(imgid);

        // To Delete Itself, onClick
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup parentView = (ViewGroup) view.getParent();
                parentView.removeView(view);
            }
        });
        return rowView;
    };

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
