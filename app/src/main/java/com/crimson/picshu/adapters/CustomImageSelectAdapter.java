package com.crimson.picshu.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crimson.picshu.R;
import com.crimson.picshu.gallery.Image;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Darshan on 4/18/2015.
 */
public class CustomImageSelectAdapter extends CustomGenericAdapter<Image> {

    static ArrayList<String> alPaths = new ArrayList<>();
    static ArrayList<Integer> alPathsCount = new ArrayList<>();

    static int clickpos = -1;
    static boolean zeroclick = false;

    public CustomImageSelectAdapter(Context context, ArrayList<Image> images) {
        super(context, images);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_view_item_image_select, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_view_image_select);
            //viewHolder.view = convertView.findViewById(R.id.view_alpha);
            viewHolder.tvCount = (TextView) convertView.findViewById(R.id.tvCount);
            viewHolder.plusImage = convertView.findViewById(R.id.plusImage);
            viewHolder.minusImage = convertView.findViewById(R.id.minusImage);
            viewHolder.countImage = convertView.findViewById(R.id.countImage);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imageView.getLayoutParams().width = size;
        viewHolder.imageView.getLayoutParams().height = size;

        // viewHolder.view.getLayoutParams().width = size;
        //  viewHolder.view.getLayoutParams().height = size;

        viewHolder.countImage.setText("0");
        HashMap<String, Integer> pathsNew = new HashMap<String, Integer>();
        viewHolder.plusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrayList.get(position).isSelected) {
                    if (!alPaths.contains(arrayList.get(position).path)) {
                        String newPath = arrayList.get(position).path;
                        int perImageCount = 0;

                        if (pathsNew.get(newPath)!= 0){
                                perImageCount++;
                        }

                        //if (pathsNew.get(new)){
                        pathsNew.put(newPath,1);
                        viewHolder.countImage.setText("1");
                        //}

                    }
                }
            }
        });
        /*if (arrayList.get(position).isSelected) {
            //viewHolder.view.setAlpha(0.5f);
            //((FrameLayout) convertView).setForeground(context.getResources().getDrawable(R.drawable.ic_done_white));
            if (!alPaths.contains(arrayList.get(position).path)) {
                alPaths.add(arrayList.get(position).path);
                viewHolder.tvCount.setText("1");
                alPathsCount.add(1);
                Log.d("isSelcted", "1");
                zeroclick = false;
            } else {
                int pos = alPaths.indexOf(arrayList.get(position).path);
                int val = alPathsCount.get(pos);

                if (position == clickpos && clickpos != 0) {

                    alPathsCount.remove(pos);
                    val = val + 1;
                    alPathsCount.add(pos, val);
                    Log.d("isSelcted", "2");
                    viewHolder.tvCount.setText("" + val);
                } else if (position == clickpos && clickpos == 0 && zeroclick == true) {

                    //do only once coz its triggering many times

                    alPathsCount.remove(pos);
                    val = val + 1;
                    alPathsCount.add(pos, val);
                    Log.d("isSelcted", "3");
                    viewHolder.tvCount.setText("" + val);

                } else {
                    viewHolder.tvCount.setText("" + val);
                    Log.d("isSelcted", "4");
                }
            }
            Log.d("alPaths", "value:: " + alPaths);
            Log.d("alPaths", "valueCount:: " + alPathsCount);
        } else {
            // viewHolder.view.setAlpha(0.0f);
            // ((FrameLayout) convertView).setForeground(null);
            if (alPaths.contains(arrayList.get(position).path)) {
                int pos = alPaths.indexOf(arrayList.get(position).path);
                viewHolder.tvCount.setText("" + alPathsCount.get(pos));
                Log.d("isSelcted", "5");
            } else {
                    viewHolder.tvCount.setText("0");

                Log.d("isSelcted", "6");
            }
        }*/

        Glide.with(context)
                .load(arrayList.get(position).path)
                .placeholder(R.drawable.image_placeholder).into(viewHolder.imageView);

        return convertView;
    }

    static class ViewHolder {
        ImageView plusImage, minusImage;
        TextView countImage;
        public ImageView imageView;
        // public View view;
        public TextView tvCount;
    }
}

