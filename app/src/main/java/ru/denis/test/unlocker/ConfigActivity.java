package ru.denis.test.unlocker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class ConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_cfg:
                break;
            default:
                break;
        }
    }
}
