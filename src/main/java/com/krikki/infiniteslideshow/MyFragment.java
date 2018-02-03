package com.krikki.infiniteslideshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by KRIKKI on 23. 08. 2017.
 */
public class MyFragment extends Fragment {
    public static final String IMAGE_NAME = "IMAGE_NAME";
    public static final String SAMPLE_SIZE = "SAMPLE_SIZE";
    ImageView imageView;
    Bitmap imageBitmap;
    String imageName;
    int sampleSize = 1;

    public static final MyFragment newInstance(String imageName1, int sampleSize) {
        MyFragment f = new MyFragment();
        Bundle bdl = new Bundle(2);
        bdl.putString(IMAGE_NAME, imageName1);
        bdl.putInt(SAMPLE_SIZE, sampleSize);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sampleSize = getArguments().getInt(SAMPLE_SIZE);
        View v = inflater.inflate(R.layout.my_fragment_layout, container, false);
        imageView = (ImageView)v.findViewById(R.id.imageView);
        RelativeLayout relativeLayout = (RelativeLayout) v.findViewById(R.id.relativeLayout);

        loadImage(getArguments().getString(IMAGE_NAME));
        return v;
    }


    public int getSampleSize(){
        return sampleSize;
    }
    public void setSampleSize(int sampleSize){
        this.sampleSize = sampleSize;
    }

    public void loadImage(String img){
        imageName = img;

        BitmapFactory.Options options = new BitmapFactory.Options();
        // scales down the image (sampleSize must be a power of 2)
        options.inSampleSize = sampleSize;

        imageBitmap = BitmapFactory.decodeFile(imageName, options);

        imageView.setImageBitmap(imageBitmap);
    }

    public void setImageBitmap(Bitmap image, String imageName){
        this.imageName = imageName;
        imageBitmap = image;
        imageView.setImageBitmap(imageBitmap);
    }
    public Bitmap getImageBitmap(){
        return imageBitmap;
    }

    public String getImageName(){
        return imageName;
    }


}