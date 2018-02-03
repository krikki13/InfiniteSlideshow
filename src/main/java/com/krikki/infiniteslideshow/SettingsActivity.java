package com.krikki.infiniteslideshow;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by KRIKKI on 24. 08. 2017.
 */

public class SettingsActivity  extends Activity {
    String path;
    SharedPreferences prefs;
    final Context context = this;
    final String DIRECTORY_NAME_KEY = "dirname";
    final String ORIENTATION_KEY = "orientation";
    final String MODE_KEY = "mode";
    final String DELAY_KEY = "delay";
    final String CLICK_BACK_KEY = "clickback";
    final String SAMPLE_SIZE_KEY = "samplesize";
    String chosenDir;
    String[] fileList;
    ListView list;
    Dialog dialog;

    int clickBack;
    int orientation;
    int mode;
    int delay;
    int sampleSize;

    static class OnBackClick {
        final static int DO_NOTHING = 0;
        final static int SHOW_SETTINGS = 1;
        final static int EXIT = 2;
        final static String[] options = {"Do nothing", "Show settings", "Exit"};
    }
    static class Orientation{
        final static int LANDSCAPE = 0;
        final static int PORTRAIT = 1;
        final static String[] options = {"Landscape", "Portrait"};
    }
    static class Mode{
        final static int AUTOMATIC = 0;
        final static int MANUAL = 1;
        final static String[] options = {"Automatic slideshow", "Manual swiping"};
        // prompt text in textview above edittext changes depending on the selected mode
        final static String[] delay_textview_prompts = {"Delay between images (in milliseconds)", "Reset time (in milliseconds)"};
    }

    static String version;

    TextView tvMode, tvDelay;
    EditText directoryNameInput, delayInput, sampleSizeInput;
    Button check, browse, bSaveSettings, bSaveNPlay, bMode, bOrientation, bBackClick, bSampleSizeInfo;
    //ToggleButton toggleButtonSwipe;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        PackageInfo pInfo = null;
        try {
            pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        version = pInfo.versionName;
        Toast.makeText(getApplicationContext(), "Infinite Slideshow, v"+version, Toast.LENGTH_LONG).show();
        TextView tv = (TextView) findViewById(R.id.tvVersion);
        tv.setText("Infinite Slideshow, v"+version);

        prefs = this.getSharedPreferences("com.krikki.infiniteslideshow", Context.MODE_PRIVATE);
        path = prefs.getString(DIRECTORY_NAME_KEY, "");
        orientation = prefs.getInt(ORIENTATION_KEY, Orientation.LANDSCAPE);
        clickBack = prefs.getInt(CLICK_BACK_KEY, OnBackClick.SHOW_SETTINGS);
        mode = prefs.getInt(MODE_KEY, Mode.AUTOMATIC);
        delay = prefs.getInt(DELAY_KEY, -1);
        sampleSize = prefs.getInt(SAMPLE_SIZE_KEY, 1);



        check = (Button) findViewById(R.id.bCheck);
        browse = (Button) findViewById(R.id.bBrowse);
        bSaveSettings = (Button) findViewById(R.id.bSaveSet);
        bSaveNPlay = (Button) findViewById(R.id.bSaveNPlay);
        bMode = (Button) findViewById(R.id.bMode);
        bOrientation = (Button) findViewById(R.id.bOrientation);
        bBackClick = (Button) findViewById(R.id.bClickBack);
        bSampleSizeInfo = (Button) findViewById(R.id.bSampleSizeInfo);
        directoryNameInput = (EditText) findViewById(R.id.folderNameInput);
        delayInput = (EditText) findViewById(R.id.delayInput);
        sampleSizeInput = (EditText) findViewById(R.id.sampleSizeInput);
        tvMode = (TextView) findViewById(R.id.tvMode);
        tvDelay = (TextView) findViewById(R.id.tvDelay);


        // setting initial values on buttons, edittexts
        directoryNameInput.setText(path);
        bOrientation.setText(Orientation.options[orientation]);
        bBackClick.setText(OnBackClick.options[clickBack]);
        bMode.setText(Mode.options[mode]);
        tvDelay.setText(Mode.delay_textview_prompts[mode]);

        if (delay < 0) {
            delayInput.setText("");
        } else {
            delayInput.setText("" + delay);
        }
        sampleSizeInput.setText("" + sampleSize);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        // END OF INITIALIZATION

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                File file = new File(directoryNameInput.getText().toString());
                if (file.exists()) {
                    if (file.isDirectory()) {
                        Toast.makeText(getApplicationContext(), "Directory exists and it contains " + file.list().length + " files", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "File you entered is not a directory", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "The file you entered does NOT exist!", Toast.LENGTH_LONG).show();
                }


            }
        });
        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showFileBrowserDialog(directoryNameInput.getText().toString());
            }
        });

        bOrientation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select orientation")
                        .setItems(Orientation.options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bOrientation.setText(Orientation.options[which]);
                                orientation = which;
                            }
                        });
                builder.create().show();
            }
        });

        bMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select mode")
                        .setItems(Mode.options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bMode.setText(Mode.options[which]);
                                tvDelay.setText(Mode.delay_textview_prompts[which]);
                                mode = which;
                            }
                        });
                builder.create().show();
            }
        });

        bBackClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Select action on click on video")
                        .setItems(OnBackClick.options, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bBackClick.setText(OnBackClick.options[which]);
                                clickBack = which;
                            }
                        });
                builder.create().show();
            }
        });
        bSampleSizeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Sample Size Info")
                        .setMessage("Setting bigger number for sample size will reduce the size of an image, making it easier to load and preventing possible memory errors. \n" +
                                    "Number must be a positive integer based on powers of 2.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
            }
        });

        bSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                saveSettings();
            }
        });
        bSaveNPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(saveSettings()) {
                    launchPageViewActivity();
                }

            }
        });

        if(delay >= 0 && !path.equals("")){
            launchPageViewActivity(); // in this case everything was already set before
        }else {
            // if rights have not been given yet for access to external storage
            if (ContextCompat.checkSelfPermission(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                askForPermission();
            }
        }
    }

    private void askForPermission() {
        ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 69);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 69: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    Toast.makeText(getApplicationContext(), "Great! Let's go to work", Toast.LENGTH_LONG).show();
                } else {
                    // Permission was denied
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle("Permission Denied!")
                            .setMessage("Application cannot work as it is supposed to.")
                            .setNegativeButton("Ask me again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    askForPermission();

                                }
                            })
                            .setPositiveButton("Better! I don't trust you anyway", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();

                                }
                            })

                            ;
                    builder.create().show();
                }
            }
        }
    }

    private void launchPageViewActivity() {
        Intent myIntent = new Intent(SettingsActivity.this, PageViewActivity.class);
        myIntent.putExtra("PATH", path);
        myIntent.putExtra("DELAY", delay);
        myIntent.putExtra("MODE", mode);
        myIntent.putExtra("ORIENTATION", orientation);
        myIntent.putExtra("BACKCLICK", clickBack);
        myIntent.putExtra("SAMPLESIZE", sampleSize);
        SettingsActivity.this.startActivity(myIntent);
    }

    private boolean saveSettings(){
        // check if settings seem valid (if not return false)
        path = directoryNameInput.getText().toString();
        int delay1;

        try{
            delay1 = Integer.parseInt(delayInput.getText().toString());
            if(delay1<0){
                Toast.makeText(getApplicationContext(), "Delay number must be a positive integer!", Toast.LENGTH_LONG).show();
                return false;
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Delay number must be a positive integer!", Toast.LENGTH_LONG).show();
            return false;
        }

        try{
            int sampleS = Integer.parseInt(sampleSizeInput.getText().toString());
            if(sampleS<=0){
                Toast.makeText(getApplicationContext(), "Sample size must be a positive integer based on powers of 2!", Toast.LENGTH_LONG).show();
                return false;
            }
            sampleSize = (int) Math.pow(2, (int) (Math.log(sampleS) / Math.log(2)));
            if(sampleS != sampleSize){
                sampleSizeInput.setText(""+sampleSize);
                Toast.makeText(getApplicationContext(), "Sample size has been changed to " + sampleSize + " to be based on power of 2", Toast.LENGTH_LONG).show();
                return false;
            }

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Sample size must be a positive integer based on powers of 2!", Toast.LENGTH_LONG).show();
            return false;
        }
        File folder = new File(path);
        if(!folder.isDirectory()) {
            Toast.makeText(getApplicationContext(), "The file you selected is not a directory!", Toast.LENGTH_LONG).show();
            return false;
        }

        // settings seem valid, save them
        prefs.edit().putString(DIRECTORY_NAME_KEY, path).apply();
        prefs.edit().putInt(DELAY_KEY, delay1).apply();
        delay = delay1;

        prefs.edit().putInt(ORIENTATION_KEY, orientation).apply();
        prefs.edit().putInt(CLICK_BACK_KEY,clickBack).apply();
        prefs.edit().putInt(MODE_KEY,mode).apply();
        prefs.edit().putInt(SAMPLE_SIZE_KEY, sampleSize).apply();
        // if folder path does not exist, settings are saved but the method still returns false (because of that it will not play slideshow if you selected "Save and play")
        if(!folder.exists()){
            Toast.makeText(getApplicationContext(), "Settings were saved, but directory you selected does NOT exist!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), "Settings were saved successfully!", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private void showFileBrowserDialog(String path1){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.file_browser_dialog);


        list = (ListView) dialog.findViewById(R.id.list);
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(SettingsActivity.this, R.layout.simple_row);

        final TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
        final Button bOK = (Button) dialog.findViewById(R.id.bOK);
        //bOk.setEnabled(false);
            /*String storage = Environment.getExternalStorageDirectory().toString();
            int i=0;
            while(new File(storage).exists() && i<100) {
                listAdapter.add(storage);
                storage = storage.substring(0, storage.length() - 1) + Integer.valueOf(storage.charAt(storage.length() - 1)) + 1;
                i++;
            }*/

        if(new File(path1).exists() && new File(path1).isDirectory()) {
            path = path1;
            listAdapter.add("/...");
        }else
            path="/storage/";
        final File[] files = new File(path).listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory())
                listAdapter.add("" + files[i].getName()+"/");
            else
                listAdapter.add("" + files[i].getName());
        }
        list.setAdapter(listAdapter);
        dialogTitle.setText(path);
        dialog.show();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                // something was clicked in browser
                String itemString = (String) list.getItemAtPosition(position);
                Log.d("Debug", "Clicked " + (itemString));

                if (itemString.endsWith("/") || itemString.equals("/...")) {  // folder or back was clicked
                    if (itemString.equals("/...")) { // back (to parent directory)
                        path = path.substring(0, path.lastIndexOf("/",path.length()-2) + 1);
                    }else {
                        path += itemString; // go in child directory
                    }

                    listAdapter.clear();
                    File[] files = new File(path).listFiles();

                    if (!path.equals("/storage/")) // if it equals storage you cannot go further back
                        listAdapter.add("/...");
                    int i = 0;
                    try {
                        for (i = 0; i < files.length; i++) {
                            if (files[i].isDirectory())
                                listAdapter.add("" + files[i].getName() + "/");
                            else
                                listAdapter.add("" + files[i].getName());
                        }
                    } catch (Exception e) {
                        Log.d("Debug", "EXCEPTION THROWN");
                        if (path.equals("/storage/emulated/") && new File("/storage/emulated/0").exists())
                            listAdapter.add("0/");
                    }
                    dialogTitle.setText(path);
                    list.setAdapter(listAdapter);
                } else {
                }


            }
        });
        bOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                directoryNameInput.setText(path);
                dialog.cancel();
            }
        });
    }

}