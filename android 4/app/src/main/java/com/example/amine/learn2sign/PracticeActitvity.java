package com.example.amine.learn2sign;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.stetho.Stetho;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import cz.msebera.android.httpclient.Header;

import static com.example.amine.learn2sign.LoginActivity.ACTIVITY_TYPE;
import static com.example.amine.learn2sign.LoginActivity.INTENT_EMAIL;
import static com.example.amine.learn2sign.LoginActivity.INTENT_ID;
import static com.example.amine.learn2sign.LoginActivity.INTENT_SERVER_ADDRESS;
import static com.example.amine.learn2sign.LoginActivity.INTENT_TIME_WATCHED;
import static com.example.amine.learn2sign.LoginActivity.INTENT_TIME_WATCHED_VIDEO;
import static com.example.amine.learn2sign.LoginActivity.INTENT_URI;
import static com.example.amine.learn2sign.LoginActivity.INTENT_WORD;
import static com.example.amine.learn2sign.LoginActivity.VIDEO_URI;

public class PracticeActitvity extends AppCompatActivity {

    static final int REQUEST_VIDEO_CAPTURE = 1;

    @BindView(R.id.rg_practice_learn)
    RadioGroup rg_practice_learn;

    @BindView(R.id.rb_learn)
    RadioButton rb_learn;

    @BindView(R.id.rb_practice)
    RadioButton rb_practice;

    @BindView(R.id.sp_words)
    Spinner sp_words;

    @BindView(R.id.bt_record)
    Button bt_record;

    @BindView(R.id.sp_ip_address)
    Spinner sp_ip_address;

    @BindView(R.id.vv_video_learn)
    VideoView vv_video_learn;

    @BindView(R.id.vv_record)
    VideoView vv_record;

    @BindView(R.id.bt_accept_practice)
    Button bt_accept;

    @BindView(R.id.bt_reject_practice)
    Button bt_reject;

    @BindView(R.id.ll_after_record)
    LinearLayout ll_after_record;

    @BindView(R.id.tv_filename)
    TextView tv_filename;

    @BindView(R.id.pb_progress)
    ProgressBar progressBar;

    @BindView(R.id.bt_practice_more)
    Button bt_practice_more;

    @BindView(R.id.performance_indicator)
    SeekBar performanceIndicator;

    @BindView(R.id.ll_performance)
    LinearLayout ll_performance;

    @BindView(R.id.performance_rating)
     TextView performanceRating;


    String path;
    String returnedURI;
    String videoUri = "";
    SharedPreferences sharedPreferences;
    long time_started = 0;
    long time_started_return = 0;
    Activity mainActivity;
    Context context;
    private String choice = "";
    private static final boolean isLearn = false;
    static int upload_number = 0;
    static Queue<Integer> numbers = new LinkedList<>();
    static String[] signNames = new String[]{"About", "And", "Can", "Cat", "Cop","Cost", "Day", "Deaf", "Decide", "Father", "Find", "Go Out", "Gold","Goodnight", "Hearing", "Here", "Hospital", "Hurt", "If", "Large", "Hello", "Help", "Sorry", "After", "Tiger"};
    int performanceValue = 0;

    long startTime;
    long endTime;
    long seconds;

    File logFile;
    String filePath = "";
    FileOutputStream fo ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTime = System.currentTimeMillis();

        try {
            filePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/logFile.txt";
            logFile = new File(filePath);
            logFile.createNewFile();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

        //bind xml to activity
        ButterKnife.bind(this);
        Stetho.initializeWithDefaults(this);

        sp_words.setEnabled(false);
        rb_practice.setChecked(true);

        vv_record.setVisibility(View.VISIBLE);
        vv_video_learn.setVisibility(View.GONE);
        bt_accept.setVisibility(View.GONE);
        bt_accept.setEnabled(false);
        bt_reject.setVisibility(View.GONE);
        taskOnPractise();

        if(getIntent().hasExtra(VIDEO_URI)) {
            videoUri = getIntent().getStringExtra(VIDEO_URI);
        }

        final Context context = PracticeActitvity.this;

        rg_practice_learn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if ( checkedId == rb_learn.getId() ) {

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(VIDEO_URI, videoUri);
                    startActivity(intent);
                }

                if (!isLearn) {
                    sp_words.setVisibility(View.GONE);
                }
            }
        });


        sp_ip_address.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sharedPreferences.edit().putString(INTENT_SERVER_ADDRESS, sp_ip_address.getSelectedItem().toString()).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(mediaPlayer!=null)
                {
                    mediaPlayer.start();

                }

            }
        };

        vv_record.setOnCompletionListener(onCompletionListener);
        vv_video_learn.setOnCompletionListener(onCompletionListener);
        vv_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vv_record.start();
            }
        });
        vv_video_learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!vv_video_learn.isPlaying()) {
                    vv_video_learn.start();
                }
            }
        });

        time_started = System.currentTimeMillis();
        sharedPreferences =  this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        performanceIndicator.setProgress(0);
        performanceIndicator.setMax(5);

        performanceIndicator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    performanceRating.setText(String.valueOf(i));
                    performanceValue = i;

                    if(performanceValue >= 3){
                        bt_accept.setEnabled(true);
                    }
                    else{
                        bt_accept.setEnabled(false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    public void taskOnPractise() {
        Toast.makeText(getApplicationContext(),"Practice", Toast.LENGTH_SHORT).show();
        String choice = randomSignName();
        this.choice = choice;
        rb_learn.setEnabled(true);

        int i = 0;
        for (String name : signNames) {

            if (name.equals(choice)) {
                sp_words.setSelection(i);
                break;
            }
            i++;
        }

        selectPlayVideo(choice);
        bt_practice_more.setVisibility(View.GONE);
        bt_record.setVisibility(View.VISIBLE);
    }

    private void selectPlayVideo(String text) {

        path = "";
        time_started = System.currentTimeMillis();
        play_video(text);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {

        time_started = System.currentTimeMillis();
        super.onResume();

    }

    //method to generate random names for actions
    public String randomSignName()
    {
        Random randomGenerator = new Random();

        int rndNum = randomGenerator.nextInt(24);

        while(numbers.contains(rndNum)){
            rndNum = randomGenerator.nextInt(24);
        }

        numbers.add(rndNum);
        if(numbers.size() >10) {
            numbers.remove();
        }
        return signNames[rndNum];

    }

    public void play_video(String text) {
        if(text.equals("About")) {

            path = "android.resource://" + getPackageName() + "/" + R.raw._about;
        } else if(text.equals("And")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._and;
        } else if (text.equals("Can")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._can;
        }else if (text.equals("Cat")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._cat;
        }else if (text.equals("Cop")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._cop;
        }else if (text.equals("Cost")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._cost;
        }else if (text.equals("Day")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._day;
        }else if (text.equals("Deaf")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._deaf;
        }else if (text.equals("Decide")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._decide;
        }else if (text.equals("Father")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._father;
        }else if (text.equals("Find")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._find;
        }else if (text.equals("Go Out")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._go_out;
        }else if (text.equals("Gold")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._gold;
        }else if (text.equals("Goodnight")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._good_night;
        }else if (text.equals("Hearing")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._hearing;
        }else if (text.equals("Here")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._here;
        }else if (text.equals("Hospital")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._hospital;
        }else if (text.equals("Hurt")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._hurt;
        }else if (text.equals("If")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._if;
        }else if (text.equals("Large")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._large;
        }else if (text.equals("Hello")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._hello;
        }else if (text.equals("Help")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._help;
        }else if (text.equals("Sorry")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._sorry;
        }else if (text.equals("After")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._after;
        }else if (text.equals("Tiger")) {
            path = "android.resource://" + getPackageName() + "/" + R.raw._tiger;
        }
        if(!path.isEmpty()) {
            Uri uri = Uri.parse(path);
            vv_video_learn.setVideoURI(uri);
            vv_video_learn.start();
        }

    }

    @OnClick(R.id.bt_record)
    public void record_video() {

        if( ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ) {

            // Permission is not granted
            // Should we show an explanation?

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        101);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        if ( ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {

            // Permission is not granted
            // Should we show an explanation?


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        } else {
            // Permission has already been granted
            File f = new File(Environment.getExternalStorageDirectory(), "Learn2Sign");

            if (!f.exists()) {
                f.mkdirs();
            }

            time_started = System.currentTimeMillis() - time_started;

            Intent t = new Intent(this, VideoActivity.class);
            t.putExtra(INTENT_WORD,sp_words.getSelectedItem().toString());
            t.putExtra(INTENT_TIME_WATCHED, time_started);
            t.putExtra(ACTIVITY_TYPE, isLearn);

            final String id = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(INTENT_ID,"00000000");
               try {
                    fo = new FileOutputStream(logFile, true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
                    outputStreamWriter.append("User with ID "+ id + " recording video for sign "+ sp_words.getSelectedItem().toString() +"\n");
                    outputStreamWriter.close();
                    }
                catch (IOException e) {
                   Log.e("Exception", "File write failed: " + e.toString());
               }

            Log.i("Button Pressed","Record Video button Pressed in Practice Mode.");

            startActivityForResult(t,9999);

        }
    }

    private void reinitiateVideoView() {
        vv_record.setVisibility(View.GONE);
        vv_record.clearAnimation();
        vv_record.suspend();
        vv_record.setVideoURI(null);
        vv_record.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.bt_reject_practice)
    public void reject() {

        reinitiateVideoView();

        vv_video_learn.setVisibility(View.GONE);


        selectPlayVideo("");

        bt_record.setVisibility(View.VISIBLE);

        bt_reject.setVisibility(View.GONE);
        bt_accept.setVisibility(View.GONE);

        rb_learn.setEnabled(true);
        rb_practice.setEnabled(false);

        ll_performance.setVisibility(View.GONE);
        performanceIndicator.setVisibility(View.GONE);

        time_started = System.currentTimeMillis();

        final String id = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(INTENT_ID,"00000000");
        try {
            fo = new FileOutputStream(logFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
            outputStreamWriter.append("User with ID "+id + " clicked on reject for sign "+sp_words.getSelectedItem().toString()+"\n");
            outputStreamWriter.close();
            }
        catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
        }

        Log.i("Button Pressed","Reject Button Pressed.");

    }

    @OnClick(R.id.bt_practice_more)
    public void practiceMore() {

        bt_record.setVisibility(View.VISIBLE);
        bt_practice_more.setVisibility(View.GONE);
        String choice = randomSignName();
        selectPlayVideo(choice);

        int i = 0;
        for (String name : signNames) {

            if (name.equals(choice)) {
                sp_words.setSelection(i);
                break;
            }
            i++;
        }

        vv_video_learn.setVisibility(View.GONE);

        reinitiateVideoView();

        final String id = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(INTENT_ID,"00000000");
        try {
            fo = new FileOutputStream(logFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
            outputStreamWriter.append("User with ID "+id + " clicked on Practice More \n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        Log.i("Button Pressed","Practice More Button Pressed.");
    }

    @OnClick(R.id.bt_accept_practice)
    public void accept() {

        String id = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
                .getString(INTENT_ID,"00000000");

        String server_ip = getSharedPreferences(this.getPackageName(),
                Context.MODE_PRIVATE).getString(INTENT_SERVER_ADDRESS,"10.211.17.171");

        RequestParams params = new RequestParams();

        final File file = new File(returnedURI);

        try {
            params.put("checked", 1);
            params.put("uploaded_file", file);
            params.put("id",id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            fo = new FileOutputStream(logFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
            outputStreamWriter.append("User with ID "+ id + " clicked on accept for sign " + sp_words.getSelectedItem().toString() + "\n");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        Log.i("Accept Button Pressed","Accept Button was pressed.");

        final String ID = id;

        // send request
        AsyncHttpClient client = new AsyncHttpClient();

        client.post("http://" + server_ip + "/upload_video_performance.php", params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                // handle success response
                Log.e("msg success",statusCode+"");
                if(statusCode == 200) {
                    upload_number += 1;
                    Toast.makeText(PracticeActitvity.this, "Success", Toast.LENGTH_SHORT).show();
                    file.delete();

                    sharedPreferences.edit().putInt("Number_Accepted",
                            1 + sharedPreferences.getInt("Number_Accepted",0)).apply();

                    vv_video_learn.setVisibility(View.GONE);
                    bt_practice_more.setVisibility(View.VISIBLE);
                    bt_accept.setVisibility(View.GONE);
                    bt_reject.setVisibility(View.GONE);
                    vv_record.setVisibility(View.GONE);
                    ll_performance.setVisibility(View.GONE);
                    performanceIndicator.setVisibility(View.GONE);

                    reinitiateVideoView();

                    endTime = System.currentTimeMillis();
                    seconds = (endTime - startTime) / 1000;

                    try {
                        fo = new FileOutputStream(logFile, true);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
                        outputStreamWriter.append("User with ID " + ID + " successfully uploaded the video for sign "+ sp_words.getSelectedItem().toString() + "\n" );
                        outputStreamWriter.append("User with ID " + ID + " took " + seconds + " seconds to learn "+ sp_words.getSelectedItem().toString() + "\n" );
                        outputStreamWriter.close();
                    }
                    catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }

                }
                else {

                    Toast.makeText(PracticeActitvity.this,
                            "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                // handle failure response
                Log.e("msg fail",statusCode+"");

                try {
                    fo = new FileOutputStream(logFile, true);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fo);
                    outputStreamWriter.append("User with ID " + ID + " failed to uploaded the video for sign "+ sp_words.getSelectedItem().toString() + "\n" );
                    outputStreamWriter.close();
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

                Toast.makeText(PracticeActitvity.this,
                        "Something Went Wrong", Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                tv_filename.setText(bytesWritten + " out of " + totalSize);

                super.onProgress(bytesWritten, totalSize);
            }


            @Override
            public void onStart() {
                tv_filename.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                super.onStart();
            }

            @Override
            public void onFinish() {
                Log.i("Practice","Upload Successful");

                if (upload_number == 1) {
                    upload_number = 0;
                    UploadLogHelper.upload_log_file(context);
                }

                tv_filename.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                super.onFinish();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Log.e("OnActivityresult",requestCode+" "+resultCode);

        if(requestCode==2000 ) {
            //from video activity
            vv_record.setVisibility(View.GONE);
            rb_learn.setChecked(true);
            bt_record.setVisibility(View.VISIBLE);
            sp_words.setEnabled(true);
            rb_learn.setEnabled(true);
            //rb_practice.setEnabled(true);
            sp_ip_address.setEnabled(true);


        }
        if(requestCode==9999 && resultCode == 8888) {

            if(intent.hasExtra(INTENT_URI) && intent.hasExtra(INTENT_TIME_WATCHED_VIDEO)) {
                returnedURI = intent.getStringExtra(INTENT_URI);
                time_started_return = intent.getLongExtra(INTENT_TIME_WATCHED_VIDEO,0);

                vv_record.setVisibility(View.VISIBLE);
                bt_record.setVisibility(View.GONE);

                bt_accept.setVisibility(View.VISIBLE);
                bt_reject.setVisibility(View.VISIBLE);
                rb_practice.setChecked(true);
                rb_learn.setChecked(false);

                rb_learn.setEnabled(false);
                rb_practice.setEnabled(false);

                sp_words.setEnabled(false);

                performanceIndicator.setProgress(0);
                bt_accept.setEnabled(false);

                ll_performance.setVisibility(View.VISIBLE);
                performanceIndicator.setVisibility(View.VISIBLE);

                vv_record.setVideoURI(Uri.parse(returnedURI));
                int try_number = sharedPreferences.getInt("record_"+sp_words.getSelectedItem().toString(),0);
                try_number++;
                String toAdd  = sp_words.getSelectedItem().toString()+"_"+try_number+"_"+time_started_return + "";
                HashSet<String> set = (HashSet<String>) sharedPreferences.getStringSet("RECORDED",new HashSet<String>());
                set.add(toAdd);
                sharedPreferences.edit().putStringSet("RECORDED", set).apply();
                sharedPreferences.edit().putInt("record_"+sp_words.getSelectedItem().toString(), try_number).apply();

                vv_video_learn.setVisibility(View.VISIBLE);
                vv_video_learn.start();
                vv_record.start();
            }

        }

        if(requestCode==9999 && resultCode==7777)
        {
            if(intent!=null) {
                //create folder
                if(intent.hasExtra(INTENT_URI) && intent.hasExtra(INTENT_TIME_WATCHED_VIDEO)) {

                    returnedURI = intent.getStringExtra(INTENT_URI);
                    time_started_return = intent.getLongExtra(INTENT_TIME_WATCHED_VIDEO,0);
                    File f = new File(returnedURI);
                    f.delete();

                    rb_practice.setChecked(true);
                    rb_learn.setChecked(false);

                    rb_learn.setEnabled(true);
                    rb_practice.setEnabled(false);

                    time_started = System.currentTimeMillis();
                    vv_video_learn.suspend();
                }
            }

        }
    }

    //Menu Item for logging out
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.menu_logout:

                Log.i("Menu Logout Done","Logged out of Menu.");

                mainActivity = this;
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("ALERT");
                alertDialog.setMessage("Logging out will delete all the data!");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferences.edit().clear().apply();
                                File f = new File(Environment.getExternalStorageDirectory(), "Learn2Sign");
                                if (f.isDirectory())
                                {
                                    String[] children = f.list();
                                    for (int i = 0; i < children.length; i++)
                                    {
                                        new File(f, children[i]).delete();
                                    }
                                }
                                startActivity(new Intent(mainActivity,LoginActivity.class));
                                mainActivity.finish();

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();
                return true;

            case R.id.menu_upload_server:

                Toast.makeText(PracticeActitvity.this,
                        "Upload menu not active in Practice Mode", Toast.LENGTH_SHORT).show();

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class SaveFile extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            FileOutputStream fileOutputStream = null;
            FileInputStream fileInputStream = null;
            try {
                fileOutputStream = new FileOutputStream(strings[0]);
                fileInputStream = (FileInputStream) getContentResolver().openInputStream(Uri.parse(strings[1]));
                Log.d("msg", fileInputStream.available() + " ");
                byte[] buffer = new byte[1024];
                while (fileInputStream.available() > 0) {

                    fileInputStream.read(buffer);
                    fileOutputStream.write(buffer);
                    publishProgress(fileInputStream.available()+"");
                }

                fileInputStream.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getApplicationContext(),"Video Saved Successfully",Toast.LENGTH_SHORT).show();
        }
    }
}
