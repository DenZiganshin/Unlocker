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
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostService extends Service {
    private static final String RequestFormat = "login=%s&type=%s&button=%s";
    private static final String TAG = "Tag_PostService";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String RepeatFormat = "login=%s&type=repeat";

    String m_server, m_login;
    PostThread m_runnable;
    Thread m_th;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        String type = intent.getStringExtra(MainActivity.key_type);
        String json = intent.getStringExtra(MainActivity.key_Command);

        String request = makeRequest(m_login, type, json);
        
        m_runnable.addRequest(request);
        
        if(m_th == null){
            m_th = new Thread(m_runnable);
            m_th.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        m_server = getResources().getString(R.string.default_server);
        m_login = getResources().getString(R.string.default_login);
        m_runnable = new PostThread(m_server, makeRepeat(m_login));
        
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    private  String makeRequest(String login, String type, String json){
        return String.format(Locale.getDefault(), RequestFormat, login, type, json);
    }
    private  String makeRepeat(String login){
        return String.format(Locale.getDefault(), RepeatFormat, login, "repeat");
    }

    private class PostThread implements Runnable{

        String m_server;
        String m_repeat;
        StringBuffer m_response;
        Boolean m_isStop = false;
        Queue<String> m_requests = new LinkedList<>();

        public boolean isRun(){
            return !m_isStop;
        }
        public PostThread(String server, String repeat){
            m_server = server;
            m_repeat = repeat;
        }

        public void stop(){
            m_isStop = true;
            stopSelf();
        }

        public void addRequest(String req){
            synchronized (m_requests){
                m_requests.add(req);
            }
        }


        @Override
        public void run() {
            String response;
            String req;
            try {
                while( !m_isStop ) {
                    //request & response
                    if ( !m_requests.isEmpty() ){
                        synchronized (m_requests){
                            req = m_requests.poll();
                        }
                        response = sendPOST(req);
                        Log.i(TAG, req);
                    }else {
                        response = sendPOST(m_repeat);
                        Log.i(TAG, m_repeat);
                    }

                    if( (response == null) || (response.equals("null")) ){
                        Log.i(TAG, "response == null");
                        break;
                    }

                    // respond
                    {
                        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                        intent.putExtra(MainActivity.key_Message, response);
                        sendBroadcast(intent);
                    }
                    
                    //delay
                    Thread.sleep(2000);
                }
            }catch (IOException e){
                Log.e(TAG, e.toString());
            }catch (InterruptedException e ){
                Log.e(TAG, e.toString());
            }finally{
                stop(); // конец runnable = завершение сервиса
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
