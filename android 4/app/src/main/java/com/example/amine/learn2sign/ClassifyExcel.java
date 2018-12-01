package com.example.amine.learn2sign;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClassifyExcel extends Activity {

    @BindView(R.id.file_excel_spinner)
    Spinner fileSpinner;

    @BindView(R.id.tv_result)
    TextView result;

    @BindView(R.id.btn_classify)
    Button classify;

    private File selectedFile;
    private List<String> fileList = new ArrayList<>();
    private static final String filePath =
            Environment.getExternalStorageDirectory().getPath() + "/sign";
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final String url = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.excel_upload);

        //bind xml to activity
        ButterKnife.bind(this);

        Boolean hasPermission = (ContextCompat.checkSelfPermission(ClassifyExcel.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(ClassifyExcel.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        File rootDirectory = new File(filePath);
        File[] files = rootDirectory.listFiles();
        fileList.clear();

        if (files.length == 0) {
            Toast.makeText(getApplicationContext(), "NO files present to classify",
                    Toast.LENGTH_SHORT).show();
        } else {

            for (File file : files) {
                fileList.add(file.getName());
            }

            selectedFile = files[0];
        }

        ArrayAdapter<String> file_list = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fileList);

        file_list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileSpinner.setAdapter(file_list);

        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedFileName = adapterView.getItemAtPosition(i).toString();

                File rootDirectory = new File(filePath);
                File[] files = rootDirectory.listFiles();

                for(File file : files){

                    if(file.getName().equals(selectedFileName)) {
                        selectedFile =  file;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        classify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClassifyAsyncTask().execute();
            }
        });
    }

    public class ClassifyAsyncTask extends AsyncTask<String, String, String> {

        int responseCode = 0;
        public ProgressDialog dialog = new ProgressDialog(ClassifyExcel.this);
        long startTimer, endTimer;
        String responseBody;

        @Override
        protected void onPreExecute() {
            startTimer = System.currentTimeMillis();
            dialog.setTitle("Classify Loader");
            dialog.setMessage("Classifying......");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            OkHttpClient client = new OkHttpClient();
            RequestBody fileData = RequestBody.create(MediaType.parse("text/csv"), selectedFile);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type","text/csv")
                    .addFormDataPart("file", selectedFile.getName(), fileData)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                responseBody = response.body().string();
                responseCode = response.code();


            } catch (IOException e) {
                Log.e("Classify call", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String args) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            endTimer = System.currentTimeMillis();
            long timer = endTimer - startTimer;

            switch (responseCode) {

                case 200:

                    Toast.makeText(getApplicationContext(), "Classification done in "
                            + Long.toString(timer) + " ms", Toast.LENGTH_SHORT).show();

                    switch (responseBody) {

                        case "About":
                        case "Father":
                            result.setText("Result : " + responseBody);
                            break;

                        default:
                            result.setText("");
                            Toast.makeText(getApplicationContext(),
                                    "Error to Classify", Toast.LENGTH_SHORT).show();
                    }

                    break;

                default:
                    result.setText("");
                    Toast.makeText(getApplicationContext(),
                            "Server took too long to respond", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
