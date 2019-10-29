package ru.denis.test.unlocker;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import java.io.OutputStream;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Exception;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends Activity
{
    private static final String REQ_FORMAT = "login=%s&type=%s&button=%s";
    private static final String OpenDoor1 = "{\"id\": %d, \"operation\":\"open_door\", \"direction\": 0}";
    private static final String OpenDoor2 = "{\"id\": %d, \"operation\":\"open_door\", \"direction\": 1}";
    private static final String TAG = "MyActivity";

    TextView tv_resp;
    EditText et_input_msg;

    Random rnd = new Random(System.currentTimeMillis());

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        tv_resp = (TextView) findViewById(R.id.tv_msg_resp);
        et_input_msg = (EditText) findViewById(R.id.et_msg_send);
        tv_resp.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }



    public void onClick(View v){
        int id = rnd.nextInt(255);
        switch(v.getId()){
            case R.id.btn_exec_line1:
                Log.i(TAG, "Lets go!");
                String req1 = String.format(Locale.getDefault(), REQ_FORMAT, "denis", "command", String.format(Locale.getDefault(), OpenDoor1, id)  );
                tv_resp.append( "send: "+ req1 + "\n");
                new IOTask().execute("denis", "command", String.format(Locale.getDefault(), OpenDoor1, id) );
                break;
            case R.id.btn_exec_line2:
                Log.i(TAG, "Lets go!");
                String req2 = String.format(Locale.getDefault(), REQ_FORMAT, "denis", "command", String.format(Locale.getDefault(), OpenDoor2, id));
                tv_resp.append( "send: "+ req2 + "\n");
                new IOTask().execute("denis", "command", String.format(Locale.getDefault(), OpenDoor2, id) );
                break;
            case R.id.btn_send:
                String req3 = String.format(Locale.getDefault(), REQ_FORMAT, "denis", "message",  et_input_msg.getText().toString());
                tv_resp.append( "send: "+ req3 + "\n");
                new IOTask().execute("denis", "message",  et_input_msg.getText().toString() );
                break;

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    class IOTask extends AsyncTask<String, String, Integer>{

        private static final String USER_AGENT = "Mozilla/5.0";
        private static final String POST_URL = "http://btn.skudkit.ru";
        private static final String RepeatFormat = "login=%s&type=repeat";

        StringBuffer response;
        String repeat_req;

        @Override
        protected Integer doInBackground(String... strings) {
            //prepare requests
            String req = String.format(Locale.getDefault(), REQ_FORMAT, strings[0], strings[1], strings[2]);
            repeat_req = String.format(Locale.getDefault(), RepeatFormat, strings[0]);
            Log.i(TAG, req);
            try {
                boolean isDone = false;
                //first request
                sendPOST(req);
                publishProgress(response.toString());
                //wait for answer
                int NineCnt = 0;
                while( true ) {
                    if(parse()){
                        NineCnt++;
                    }
                    if (NineCnt >= 2){
                        break;
                    }
                    sendPOST(repeat_req);
                    publishProgress(response.toString());
                    try {
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        Log.i(TAG , e.toString());
                        break;
                    }
                }
            }catch (IOException e){
                Log.e(TAG, e.toString());
            }
            return 0;
        }

        private boolean parse(){
            // parse json
            try {
                JSONArray jObject = new JSONArray(response.toString());
                for(int i=0; i<jObject.length(); i++){
                    String msg;
                    int opLevel;
                    String type;
                    try {
                        JSONObject jobj = jObject.getJSONObject(i);
                        msg = jobj.getString("message");
                        opLevel = jobj.getInt("level");
                        type = jobj.getString("type");
                    } catch (JSONException e) {
                        // Oops
                        Log.i(TAG , e.toString());
                        return false;
                    }
                    if(opLevel == 9){
                        //done
                        return true;
                    }
                }
            }catch (JSONException e){
                Log.i(TAG , e.toString());
                return false;
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tv_resp.append("recv: "+response.toString()+"\n");
        }

        @Override
        protected void onPostExecute(Integer i) {
            if(i == 0)
                tv_resp.append("recv: "+response.toString()+"\n");
            else
                tv_resp.append("recv: "+"got some error\n");
        }

        private boolean sendPOST(String req) throws IOException {
            URL obj = new URL(POST_URL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);

            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(req.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            Log.i(TAG ,"POST Response Code :: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.i(TAG , "POST request not worked");
                return false;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            Log.i(TAG , response.toString());

            return true;
        }
    }
}
