package ru.denis.test.unlocker;

import android.app.Activity;
import android.content.*;
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
    public final static String BROADCAST_ACTION = "ru.denis.test.unlocker.servicebroadcast";
    public static final String key_Message = "key_Message";
    public static final String key_Command = "key_cmd_json";
    public static final String key_type = "key_Message_type";

    private  String Server = "";
    private  String Login = "";
    private String OpenDoor1 = "";
    private String OpenDoor2 = "";

    BroadcastReceiver m_br;

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
        }

        setContentView(R.layout.layout_main);
        m_br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String res = intent.getStringExtra(key_Message);
                Log.i(TAG, res);
                /*try {
                    ArrayList<Msg> result= new ArrayList<>();
                    JSONArray jObject = new JSONArray(res.toString());
                    for(int i=0; i<jObject.length(); i++){
                        try {
                            JSONObject jobj = jObject.getJSONObject(i);
                            String message = jobj.getString("message");
                            int level = jobj.getInt("level");
                            String type = jobj.getString("type");

                            if (type.equals("message")) {
                            } else if (type.equals("command")) {
                            }
                        } catch (JSONException e) {
                            // Oops
                            Log.i(TAG , e.toString());
                            return;
                        }
                    }

                }catch (JSONException e){
                    Log.i(TAG , e.toString());
                }*/
            }
        };
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(m_br, intFilt);
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

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        unregisterReceiver(m_br);
        super.onDestroy();
    }

    public void onClick(View v){
        int id = rnd.nextInt(255);
        String req = "";
        String type = "";
        switch(v.getId()){
            case R.id.btn_exec_line1:
                req = OpenDoor1;
                type = "command";
                break;
            case R.id.btn_exec_line2:
                req = OpenDoor2;
                type = "command";
                break;
        }

        if( req.isEmpty() )
            return;

        Intent intent = new Intent(this, PostService.class);
        // это ключевые значения
        intent.putExtra(key_Command, req);
        intent.putExtra(key_type, type);
        // стартуем сервис
        startService(intent);

        /*
        if( !req.isEmpty() ) {
            Log.i(TAG, "do request");
            new IOTask().execute(Login, type, req);
        }
         */
    }

    public void onClear(View v){
        /*
        SharedPreferences sharedPref = getSharedPreferences(ConfigActivity.Pref_Name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        finish();
         */
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);

    }

    private boolean loadPref(){
        SharedPreferences sharedPref = getSharedPreferences(ConfigActivity.Pref_Name, Context.MODE_PRIVATE);
        if(! sharedPref.contains(ConfigActivity.key_Server) ) {
            // first app load
            Log.i(TAG, "no config");
            return false;
        }
        String mode = sharedPref.getString(ConfigActivity.key_Mode, "debug");
        if( ! mode.equals( "debug") ){
            Log.i(TAG, "debug mode");
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
}
