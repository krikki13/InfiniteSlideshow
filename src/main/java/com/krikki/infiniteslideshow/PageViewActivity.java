package com.krikki.infiniteslideshow;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import static com.krikki.infiniteslideshow.SettingsActivity.Orientation;
import static com.krikki.infiniteslideshow.SettingsActivity.OnBackClick;
import static com.krikki.infiniteslideshow.SettingsActivity.Mode;


public class PageViewActivity extends FragmentActivity {
    MyPageAdapter pageAdapter;
    ViewPager pager;
    List<MyFragment> fragments;
    String[] imageList;
    Handler handler=new Handler();
    Runnable autoUpdate;
    int delay = 5000;
    int mode = Mode.AUTOMATIC;
    int clickBack;
    int currentIndex = 0; // points to the middle, visible image
    String directoryPath;
    int sampleSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_view_layout);

        Bundle extras = getIntent().getExtras();
        if(extras !=null){
            directoryPath = extras.getString("PATH");
            delay = extras.getInt("DELAY");
            mode = extras.getInt("MODE");
            switch(extras.getInt("ORIENTATION")){
                case Orientation.LANDSCAPE: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); break;
                case Orientation.PORTRAIT: this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
            clickBack = extras.getInt("BACKCLICK");
            sampleSize = extras.getInt("SAMPLESIZE");

        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        File[] fileList = new File(directoryPath).listFiles(new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
            }
        });
        if(fileList == null){
            Toast.makeText(getApplicationContext(), "Directory does not exist!", Toast.LENGTH_LONG).show();
            if (clickBack == OnBackClick.SHOW_SETTINGS) { // if it is set to go to settings on back click, it goes also in this case, otherwise the app quits
                finish();
                return;
            }else {
                showNoImagesFoundImage();
                return;
            }
        }

        imageList = new String[fileList.length];
        for(int i=0; i<fileList.length; i++){
            imageList[i] = fileList[i].getAbsolutePath();

        }

        if(imageList.length==0) {
            Toast.makeText(getApplicationContext(), "No images could be found!", Toast.LENGTH_LONG).show();
            if (clickBack == OnBackClick.SHOW_SETTINGS) { // if it is set to go to settings on back click, it goes also in this case, otherwise the app quits
                finish();
                return;
            }else{
                showNoImagesFoundImage();
                return;
            }
        }else if(imageList.length==1){
            String img = imageList[0];
            imageList = new String[]{img,img,img};
        }else if(imageList.length==2){
            String img = imageList[0];
            String img1 = imageList[1];
            imageList = new String[]{img,img1,img,img1};
        }
        fragments = getFragments();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
        pager = (ViewPager)findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);
        pager.setCurrentItem(1);
        hideFunctionButtons();



        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE){

                    String img = fragments.get(pager.getCurrentItem()).imageName;

                    if(!imageList[currentIndex].equals(img)){ // image was swiped by user
                        Log.d("Debugx", "DO IT");
                        updateNumbers(pager.getCurrentItem(), false);
                    }else{ // user was touching the screen, but did not swipe in the end (the image remains the same)
                        if(delay != 0) // if delay is 0, nothing will be happening automatically
                            handler.postDelayed(autoUpdate, delay);
                    }
                }else{
                    if(delay != 0)
                        handler.removeCallbacks(autoUpdate);
                }
            }


        });
        autoUpdate = new Runnable() {
            @Override
            public void run() {
                Log.d("Debugx", "TIME to go on");
                if(mode==0)
                    updateNumbers(2, true);
                else {
                    if(currentIndex == 0){
                        handler.postDelayed(autoUpdate, delay);
                    }else{
                        currentIndex = 0;
                        updateNumbers(1, true);
                    }
                }

            }
        };
        if(delay != 0)
                handler.postDelayed(autoUpdate, delay);
    }

    @Override
    public void onBackPressed() {
        if (clickBack == OnBackClick.SHOW_SETTINGS) {
            finish();
        } else if (clickBack == OnBackClick.EXIT) {
            this.finishAffinity();
        }
    }


    private List<MyFragment> getFragments() {
        List<MyFragment> fList = new ArrayList<MyFragment>();
        fList.add(MyFragment.newInstance(imageList[imageList.length-1], sampleSize));
        fList.add(MyFragment.newInstance(imageList[0], sampleSize));
        fList.add(MyFragment.newInstance(imageList[1], sampleSize));
        return fList;
    }

    private void updateNumbers(int currentItem,  boolean automatic) {
        // currentIndex can be 0-2 and means left, middle or right image or fragment
        // currentItem is the consecutive number of image in imageList
        hideFunctionButtons();
        if(currentItem == 0){ // user swiped left
            currentIndex--;
            if(currentIndex<0)
                currentIndex = imageList.length-1;
        }else if(currentItem == 2){ // user swiped right
            currentIndex++;
            if(currentIndex==imageList.length)
                currentIndex = 0;

        }

        // currentItem can be 1 only, when it is set to manual mode and reset has occured (timer reached 0)
        if(currentItem==1) {
            if (currentIndex == 0) {
                fragments.get(0).loadImage(imageList[imageList.length - 1]);
                fragments.get(1).loadImage(imageList[0]);
                fragments.get(2).loadImage(imageList[1]);
            } else if (currentIndex == imageList.length - 1) {
                fragments.get(0).loadImage(imageList[currentIndex - 1]);
                fragments.get(1).loadImage(imageList[currentIndex]);
                fragments.get(2).loadImage(imageList[0]);
            } else {
                fragments.get(0).loadImage(imageList[currentIndex - 1]);
                fragments.get(1).loadImage(imageList[currentIndex]);
                fragments.get(2).loadImage(imageList[currentIndex + 1]);
            }
        }else { // the code in else is more efficient because it reuses 2 images, while code in if loads all 3 images everytime
            if (currentIndex == 0 && currentItem == 0) { // it went from 1. image to 0. (left)
                fragments.get(2).setImageBitmap(fragments.get(1).getImageBitmap(), fragments.get(1).getImageName());
                fragments.get(1).setImageBitmap(fragments.get(0).getImageBitmap(), fragments.get(0).getImageName());
                fragments.get(0).loadImage(imageList[imageList.length - 1]);
            } else if (currentIndex == imageList.length - 1 && currentItem == 2) {
                // it went from before-last to last image (right)
                fragments.get(0).setImageBitmap(fragments.get(1).getImageBitmap(), fragments.get(1).getImageName());
                fragments.get(1).setImageBitmap(fragments.get(2).getImageBitmap(), fragments.get(2).getImageName());
                fragments.get(2).loadImage(imageList[0]);
            } else {
                if (currentItem == 0) { // swiped left
                    fragments.get(2).setImageBitmap(fragments.get(1).getImageBitmap(), fragments.get(1).getImageName());
                    fragments.get(1).setImageBitmap(fragments.get(0).getImageBitmap(), fragments.get(0).getImageName());
                    fragments.get(0).loadImage(imageList[currentIndex - 1]);
                } else { // swiped right
                    fragments.get(0).setImageBitmap(fragments.get(1).getImageBitmap(), fragments.get(1).getImageName());
                    fragments.get(1).setImageBitmap(fragments.get(2).getImageBitmap(), fragments.get(2).getImageName());
                    fragments.get(2).loadImage(imageList[currentIndex + 1]);
                }
            }
        }
        pager.setCurrentItem(1, false);
        if(delay != 0)
            handler.postDelayed(autoUpdate, delay);
    }

    private void hideFunctionButtons(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    private void showNoImagesFoundImage(){
        // shows an error image saying there were no images found in the selected directory (same is shown also if directory does not exist)
        setContentView(R.layout.my_fragment_layout);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout);
        rl.setBackgroundColor(Color.WHITE);
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setImageResource(R.drawable.no_image_found);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(delay != 0)
            handler.removeCallbacks(autoUpdate);
    }
}