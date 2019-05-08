package com.example.resue;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class SavedContactFragement extends Fragment {

    private SQLiteDatabase database;
    private String names[];
    private String mobiles[];
    private ListView savedContactListView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_saved_contacts, container, false);
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.saved_contact_toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        database = getActivity().openOrCreateDatabase("Rescue", Context.MODE_PRIVATE, null);
        savedContactListView = (ListView)view.findViewById(R.id.saved_contact_list_view);

        retriveDataFromDB();

        if(names != null && mobiles != null){
            if(names.length == mobiles.length){
                savedContactListView.setAdapter(new CustomSavedList());
            }
        }
        savedContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete From Rescue")
                        .setMessage("Are you sure you want to delete this contact("+names[position].toUpperCase()+")?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                               deleteContactFromDB(names[position]);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
        return view;
    }

    private void deleteContactFromDB(String name){
        database.execSQL("DELETE FROM rescuecontacts WHERE name = '"+name+"'");
        retriveDataFromDB();
        if(names != null && mobiles != null){
            if(names.length == mobiles.length){
                savedContactListView.setAdapter(new CustomSavedList());
            }
        }
    }
    private void  retriveDataFromDB(){
        database.execSQL("CREATE TABLE IF NOT EXISTS rescuecontacts(name varchar not null, mobile varchar not null);");
        Cursor cursor = database.rawQuery("SELECT * FROM rescuecontacts", null);
        names = new String[cursor.getCount()];
        mobiles = new String[cursor.getCount()];
        int i = 0;
        if (cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            do {
                names[i] = cursor.getString(cursor.getColumnIndex("name"));
                mobiles[i] = cursor.getString(cursor.getColumnIndex("mobile"));
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    public class CustomSavedList extends BaseAdapter{

        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.custom_contact_list, parent, false);
            TextView name = (TextView) view.findViewById(R.id.contact_name);
            TextView num = (TextView)view.findViewById(R.id.contact_num);
            name.setText(names[position]);
            num.setText(mobiles[position]);
            return view;
        }
    }
}
