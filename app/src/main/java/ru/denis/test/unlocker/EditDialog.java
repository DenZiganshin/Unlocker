package ru.denis.test.unlocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditDialog extends DialogFragment {

    public interface EditDialogListener{
        public void onDialogPositive(DialogFragment fragment);
        public void onDialogNegative(DialogFragment fragment);
    }

    private EditDialogListener m_listener;
    private String m_value;
    private String m_key;
    private EditText m_et_line;

    public String getValue() {
        return m_value;
    }
    public String getKey() {
        return m_key;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            m_listener = (EditDialogListener) activity;
        }catch (ClassCastException e){

        }

    }



    /**
     *
     * @param key
     * @param val
     */
    public void setEditValue(String key, String val){
        this.m_key = key;
        this.m_value = val;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_edit, null);
        m_et_line = v.findViewById(R.id.dialog_edit_line);
        m_et_line.setText(m_value);
        ((TextView)v.findViewById(R.id.dialog_edit_label)).setText(m_key);

        builder.setView(v);
        builder.setPositiveButton("save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_value = m_et_line.getText().toString();
                m_listener.onDialogPositive(EditDialog.this);
            }
        });
        builder.setNegativeButton("cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_listener.onDialogNegative(EditDialog.this);
            }
        });
        return builder.create();
    }
}
