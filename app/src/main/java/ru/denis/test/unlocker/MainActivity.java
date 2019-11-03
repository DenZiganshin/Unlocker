package ru.denis.test.unlocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import java.io.OutputStream;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends Activity
{
    private static final String REQ_FORMAT = "login=%s&type=%s&button=%s";
    private static final String TAG = "Tag_MainActivity";

    private  String Server = "";
    private  String Login = "";
    private String OpenDoor1 = "";
    private String OpenDoor2 = "";

    TextView tv_resp, tv_status;
    EditText et_input_msg;

    Random rnd = new Random(System.currentTimeMillis());

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //check pref
        if(!loadPref()){
            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);
            //finish();
        }

        setContentView(R.layout.layout_main);
        tv_resp = (TextView) findViewById(R.id.tv_msg_resp);
        tv_status = (TextView) findViewById(R.id.tv_msg_status);
        et_input_msg = (EditText) findViewById(R.id.et_msg_send);
        tv_resp.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart");
        loadPref();
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
        String req = "";
        String type = "";
        switch(v.getId()){
            case R.id.btn_exec_line1:
                req = OpenDoor1;
                type = "command";
                Log.i(TAG, "btn1: "+req);
                break;
            case R.id.btn_exec_line2:
                req = OpenDoor2;
                type = "command";
                Log.i(TAG, "btn2: "+req);
                break;
            case R.id.btn_send:
                req = et_input_msg.getText().toString();
                type = "message";
                break;
        }
        if( !req.isEmpty() ) {
            Log.i(TAG, "do request");
            new IOTask().execute(Login, type, req);
        }
    }

    public void onClear(View v){
        SharedPreferences sharedPref = getSharedPreferences(ConfigActivity.Pref_Name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        finish();
    }

    private boolean loadPref(){
        SharedPreferences sharedPref = getSharedPreferences(ConfigActivity.Pref_Name, Context.MODE_PRIVATE);
        if(! sharedPref.contains(ConfigActivity.key_Server) ) {
            // first app load
            Log.i(TAG, "no config");
            return false;
        }
        Server = sharedPref.getString( ConfigActivity.key_Server, getResources().getString(R.string.default_server));
        Login = sharedPref.getString( ConfigActivity.key_Login, getResources().getString(R.string.default_login) );

        OpenDoor1 = sharedPref.getString( ConfigActivity.key_Json_BTN1, getResources().getString(R.string.default_json1));
        OpenDoor2 = sharedPref.getString( ConfigActivity.key_Json_BTN2, getResources().getString(R.string.default_json2));

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private class Msg {
        public int m_level;
        public String m_message;
        public String m_type;
        public Msg(int level, String msg, String type){
            m_level = level;
            m_message = msg;
            m_type = type;
        }
    }

    /**
     * класс для работы с сервером
     */
    class IOTask extends AsyncTask<String, Msg, Integer>{

        private static final String USER_AGENT = "Mozilla/5.0";
        private static final String RepeatFormat = "login=%s&type=repeat";

        StringBuffer m_response;
        String m_repeat_req;


        @Override
        protected Integer doInBackground(String... strings) {
            //prepare requests
            String req = String.format(Locale.getDefault(), REQ_FORMAT, strings[0], strings[1], strings[2]);
            m_repeat_req = String.format(Locale.getDefault(), RepeatFormat, strings[0]);
            Log.i(TAG, req);
            try {
                while( true ) {
                    //request & response
                    sendPOST(req);
                    //parse response
                    Msg[] msg = parse();
                    if(msg == null){
                        break;
                    }
                    // analyze data
                    //for (int i=0; i<msg.length; i++) {
                        if( (msg[msg.length-1].m_level == 9) && (msg.length == 1) ){
                            Log.i(TAG, "done");
                            break; //we done;
                        }else{
                            //we need repeat
                            Log.i(TAG, "need repeat");
                            req = String.format(Locale.getDefault(), RepeatFormat, strings[0]);
                        }
                    //}
                    // update ui
                    publishProgress(msg);

                    //delay
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

        private Msg[] parse(){
            if(m_response == null)
                return null;
            // parse json
            try {
                ArrayList<Msg> result= new ArrayList<>();
                JSONArray jObject = new JSONArray(m_response.toString());
                for(int i=0; i<jObject.length(); i++){
                    try {
                        JSONObject jobj = jObject.getJSONObject(i);
                        String message = jobj.getString("message");
                        int level = jobj.getInt("level");
                        String type = jobj.getString("type");
                        result.add( new Msg(level, message,  type) );
                    } catch (JSONException e) {
                        // Oops
                        Log.i(TAG , e.toString());
                        return null;
                    }
                }
                return result.toArray(new Msg[0]);

            }catch (JSONException e){
                Log.i(TAG , e.toString());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Msg... values) {
            tv_resp.setText("");
            tv_status.setText("");
            if(values == null) {
                Log.i(TAG, "got null");
                return;
            }
            Log.i(TAG, "onProgressUpdate:" + Integer.toString(values.length));
            for(int i=0; i<values.length; i++) {
                Msg m = values[i];
                Log.i(TAG, "onProgressUpdate:" + m.m_type + " " + m.m_message);
                if (m.m_type.equals("message")) {
                    Log.i(TAG, "ui message");
                    tv_resp.setText(m.m_message);
                } else if (m.m_type.equals("command")) {
                    Log.i(TAG, "ui command");
                    tv_status.setText(m.m_message);
                }
                /*
                if( (i+1) < values.length){
                    tv_status.append(", ");
                    tv_resp.append(", ");
                }
                 */
            }
        }

        @Override
        protected void onPostExecute(Integer i) {
        }

        /**
         * Отправка запроса серверу
         * @param req
         * @return
         * @throws IOException
         */
        private boolean sendPOST(String req) throws IOException {
            URL obj = new URL(Server);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);

            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(req.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            Log.i(TAG ,"POST Response Code :: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.i(TAG , "POST request not worked");
                return false;
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            m_response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                m_response.append(inputLine);
            }
            in.close();

            // print result
            Log.i(TAG , m_response.toString());

            return true;
        }
    }
}
