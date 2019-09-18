package ru.denis.test.unlocker;

import android.app.Activity;
import android.os.Bundle;
import java.net.HttpURLConnection;
import java.net.URL;
import android.view.View;
import java.io.OutputStream;
import android.util.Log;
import java.lang.Exception;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;


public class MainActivity extends Activity
{
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String POST_URL = "http://btn.skudkit.ru";
    private static final String POST_PARAMS = "login=denis&type=command&button={id: 11, operation:\"open_door\", direction: 0}";
    private static final String TAG = "MyActivity";


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

    }

    private static void sendPOST() throws IOException {
        URL obj = new URL(POST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        int responseCode = con.getResponseCode();
        Log.i(TAG ,"POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            Log.i(TAG , response.toString());
        } else {
            Log.i(TAG , "POST request not worked");
        }
    }


    public void onClick(View v){
        switch(v.getId()){
            case R.id.btn_send:
                Log.i(TAG, "Lets go!");
                try{
                    sendPOST();
                }catch(Exception error) {
                    Log.i(TAG, "exception "+error.toString());
                }
                break;

        }
    }
}
