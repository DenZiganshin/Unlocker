package ru.denis.test.unlocker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class ConfigActivity extends Activity  implements EditDialog.EditDialogListener{
    private static final String TAG = "Tag_ConfigActivity";
    public static final String key_Json_BTN1 = "Json_Button1";
    public static final String key_Json_BTN2 = "Json_Button2";

    public static final String key_Server = "server";
    public static final String key_Login = "login";
    public static final String key_Password = "pass";
    public static final String key_Interval = "interval";
    public static final String Pref_Name="Prefs";
    public static final String key_Mode="AppMode";

    private HashMap<String, String> m_parameters = new HashMap<String, String>();

    private EditDialog editDialog = new EditDialog();

    private int currBtnId;

    /**
     * Callback on positive dialog key
     * @param fragment
     */
    @Override
    public void onDialogPositive(DialogFragment fragment) {
        String value = ((EditDialog) fragment).getValue();
        String key = ((EditDialog) fragment).getKey();
        Log.i(TAG, key+" "+value);
        m_parameters.put(key, value);
        ((Button)findViewById(currBtnId)).setText(value);
    }

    /**
     * Callback on negative dialog key
     * @param fragment
     */
    @Override
    public void onDialogNegative(DialogFragment fragment) {

    }

    /**
     * Constructor
     */
    public ConfigActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        if(!loadPref()){
            loadDefaultPref();
        }
        updateUI();
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

    @Override
    public void onBackPressed() {
        //Toast.makeText(this, "using default config", Toast.LENGTH_SHORT).show();
        savePref("dbg");
        finish();
    }

    public void onClick(View v) {
        currBtnId = v.getId();
        switch (currBtnId) {
            case R.id.btn_save_cfg:
                savePref("work");
                //startMain();
                finish();
                break;
            case R.id.btn_clear_cfg:
                loadDefaultPref();
                updateUI();
                //finish();
                break;
            case R.id.btn_dbg_cfg:
                savePref("dbg");
                //startMain();
                finish();
                break;
            case R.id.cfg_btn_server:
                editDialog.setEditValue(key_Server, m_parameters.get(key_Server));
                editDialog.show(getFragmentManager(), "editDial");
                break;
            case R.id.cfg_btn_login:
                editDialog.setEditValue(key_Login, m_parameters.get(key_Login));
                editDialog.show(getFragmentManager(), "editDial");
                break;
            case R.id.cfg_btn_json1:
                editDialog.setEditValue(key_Json_BTN1, m_parameters.get(key_Json_BTN1));
                editDialog.show(getFragmentManager(), "editDial");
                break;
            case R.id.cfg_btn_json2:
                editDialog.setEditValue(key_Json_BTN2, m_parameters.get(key_Json_BTN2));
                editDialog.show(getFragmentManager(), "editDial");
                break;
            default:
                Log.i(TAG, "some btn");
                break;
        }
    }

    private void startMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void savePref(String mode){
        SharedPreferences sharedPref = getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key_Json_BTN1, m_parameters.get(key_Json_BTN1) );
        editor.putString(key_Json_BTN2, m_parameters.get(key_Json_BTN2) );

        editor.putString(key_Mode, mode );

        editor.putString(key_Server, m_parameters.get(key_Server) );
        editor.putString(key_Login, m_parameters.get(key_Login) );
        editor.putString(key_Password, m_parameters.get(key_Password) );
        editor.putString(key_Interval, m_parameters.get(key_Interval) );

        editor.putString(key_Mode, m_parameters.get(key_Mode) );
        editor.commit();
    }

    private void loadDefaultPref() {
        m_parameters.put( key_Json_BTN1, getResources().getString(R.string.default_json1));
        m_parameters.put( key_Json_BTN2, getResources().getString(R.string.default_json2));

        m_parameters.put( key_Server, getResources().getString(R.string.default_server));
        m_parameters.put( key_Login, getResources().getString(R.string.default_login));
        m_parameters.put( key_Password, getResources().getString(R.string.default_password));
        m_parameters.put( key_Interval, getResources().getString(R.string.default_interval));
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
        m_parameters.put( key_Server, sharedPref.getString( ConfigActivity.key_Server, getResources().getString(R.string.default_server)));
        m_parameters.put( key_Login, sharedPref.getString( ConfigActivity.key_Login, getResources().getString(R.string.default_login) ));

        m_parameters.put( key_Json_BTN1, sharedPref.getString( ConfigActivity.key_Json_BTN1, getResources().getString(R.string.default_json1)));
        m_parameters.put( key_Json_BTN2, sharedPref.getString( ConfigActivity.key_Json_BTN2, getResources().getString(R.string.default_json2)));

        return true;
    }

    private  void updateUI(){
        ((Button)findViewById(R.id.cfg_btn_server)).setText( m_parameters.get(key_Server) );
        ((Button)findViewById(R.id.cfg_btn_login)).setText( m_parameters.get(key_Login) );
        ((Button)findViewById(R.id.cfg_btn_pass)).setText( m_parameters.get(key_Password) );
        ((Button)findViewById(R.id.cfg_btn_inter)).setText( m_parameters.get(key_Interval) );

        ((Button)findViewById(R.id.cfg_btn_json1)).setText( m_parameters.get(key_Json_BTN1) );
        ((Button)findViewById(R.id.cfg_btn_json2)).setText( m_parameters.get(key_Json_BTN2) );
    }

}
