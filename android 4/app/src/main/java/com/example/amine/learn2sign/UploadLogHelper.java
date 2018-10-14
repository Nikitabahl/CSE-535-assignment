package com.example.amine.learn2sign;

import android.content.Context;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

import cz.msebera.android.httpclient.Header;

import static com.example.amine.learn2sign.LoginActivity.INTENT_ID;
import static com.example.amine.learn2sign.LoginActivity.INTENT_SERVER_ADDRESS;

public class UploadLogHelper {

    public static void upload_log_file(final Context context) {

        Toast.makeText(context,"Upload Log file to Server", Toast.LENGTH_LONG).show();
        String id = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(INTENT_ID,"00000000");

        String server_ip = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(INTENT_SERVER_ADDRESS,"10.211.17.171");

        File n = new File(context.getFilesDir().getPath());
        File f = new File(n.getParent()+"/shared_prefs/" + context.getPackageName() +".xml");
        AsyncHttpClient client_logs = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("uploaded_file",f);
            params.put("id",id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client_logs.post("http://"+server_ip+"/upload_log_file.php", params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode==200)
                    Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, "Log File could not be uploaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "Log File could not be uploaded", Toast.LENGTH_SHORT).show();

            }
        });


    }
}
