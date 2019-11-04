package ru.denis.test.unlocker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostService extends Service {
    private static final String RequestFormat = "login=%s&type=%s&button=%s";
    private static final String TAG = "Tag_PostService";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String RepeatFormat = "login=%s&type=repeat";

    PostThread m_runnable = null;
    ExecutorService m_es;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        String Server = intent.getStringExtra(ConfigActivity.key_Server);
        String login = intent.getStringExtra(ConfigActivity.key_Login);
        String type = intent.getStringExtra(MainActivity.key_type);
        String json = intent.getStringExtra(MainActivity.key_Command);

        String request = makeRequest(login, type, json);
        String repeat = makeRepeat(login);

        if( m_runnable == null){
            Log.i(TAG, "create Runnable");
            m_runnable = new PostThread(startId, Server);
            m_runnable.addRequest(request, repeat);
            m_es.execute(m_runnable);

        }else {
            m_runnable.addRequest(request, repeat);
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(TAG, "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        m_es = Executors.newFixedThreadPool(2);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private  String makeRequest(String login, String type, String json){
        return String.format(Locale.getDefault(), RequestFormat, login, type, json);
    }
    private  String makeRepeat(String login){
        return String.format(Locale.getDefault(), RepeatFormat, login, "repeat");
    }

    private class PostThread implements Runnable{

        String m_server;
        String m_request;
        String m_repeat;
        StringBuffer m_response;
        Boolean m_isStop = false;
        int m_id;

        public boolean isRun(){
            return !m_isStop;
        }
        public PostThread(int Id, String server){
            m_server = server;
            m_id = Id;
        }

        public void stop(){
            m_isStop = true;
            stopSelfResult(m_id);
        }

        public void addRequest(String req, String repeat){
            m_request = req;
            m_repeat = repeat;
        }


        @Override
        public void run() {
            String response;
            try {
                while( !m_isStop ) {
                    //request & response
                    if (m_request != null){
                        response = sendPOST(m_request);
                        Log.i(TAG, m_request);
                        m_request = null;
                    }else{
                        response = sendPOST(m_repeat);
                        Log.i(TAG, m_repeat);
                    }

                    if( (response == null) || (response.equals("null")) ){
                        Log.i(TAG, "response == null");
                        stop();
                        break;
                    }

                    // respond
                    {
                        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                        intent.putExtra(MainActivity.key_Message, response);
                        sendBroadcast(intent);
                    }


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
        }

        private String sendPOST(String req) throws IOException {
            URL obj = new URL(m_server);
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
                return null;
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

            return m_response.toString();
        }
    }
}
