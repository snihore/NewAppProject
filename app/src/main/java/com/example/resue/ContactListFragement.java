package com.example.resue;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactListFragement extends Fragment {

//    private ArrayList<String> names, numbers;
        private ArrayList<String> contacts0, contacts1;
//    private ArrayList<String> contactsName, contactsNumber;
    private EditText contactListSearchField;
    ListView contactsListView;

    private SQLiteDatabase database;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragement_contact_list, container, false);

        database = getActivity().openOrCreateDatabase("Rescue", Context.MODE_PRIVATE, null);

        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        contactsListView = (ListView)view.findViewById(R.id.contacts_list_view);
        contactListSearchField = (EditText)view.findViewById(R.id.contact_list_search_field);

//        names = new ArrayList<>();
//        numbers = new ArrayList<>();
//        contactsName = new ArrayList<>();
//        contactsNumber = new ArrayList<>();
        contacts0 = new ArrayList<>();



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1786);
            }else{
                getContacts();
            }
        }else{
            getContacts();
        }

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Save To Rescue")
                        .setMessage("Are you sure you want to save this contact("+contacts1.get(position).split("##")[0].toUpperCase()+")?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                String[] c =contacts1.get(position).split("##");
                                storeToDatabase(c[0], c[1]);
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        contactListSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    getContacts();
                }else{
                    searchItem(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void  storeToDatabase(String name, String mobile){
        database.execSQL("CREATE TABLE IF NOT EXISTS rescuecontacts(name varchar not null, mobile varchar not null);");
//        database.execSQL("DELETE FROM rescuecontacts;");

        Cursor cursor = database.rawQuery("SELECT * FROM rescuecontacts WHERE name = \""+name+"\"", null);
//        String[] names = new String[cursor.getCount()];
//        String[] mobiles = new String[cursor.getCount()];
//        int i = 0;
//        if (cursor.getCount() > 0)
//        {
//            cursor.moveToFirst();
//            do {
//                names[i] = cursor.getString(cursor.getColumnIndex("name"));
//                mobiles[i] = cursor.getString(cursor.getColumnIndex("mobile"));
//                i++;
//            } while (cursor.moveToNext());
//            cursor.close();
//        }

        if(cursor.getCount() == 0){
            Cursor limit_cursor = database.rawQuery("SELECT * FROM rescuecontacts ", null);
            if(limit_cursor.getCount() > 4){
                Toast.makeText(getActivity().getApplicationContext(), "Not select more then 5 contacts !", Toast.LENGTH_SHORT).show();
            }else{
                database.execSQL("INSERT INTO rescuecontacts VALUES(\""+name+"\", \""+mobile+"\");");
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1786){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getContacts();
            }
        }
    }

    private void getContacts() {
        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//            numbers.add(number);
//            names.add(name);
//            Log.i(name, number);
            String contact = name+"##"+number;
            if(!contacts0.contains(contact)){

                contacts0.add(contact);
            }
        }
//        contactsName = new ArrayList<>(names);
//        contactsNumber = new ArrayList<>(numbers);
        contacts1 = new ArrayList<>(contacts0);
        contactsListView.setAdapter(new CustomList());

    }
    private void searchItem(String textToSearch){
        for(String item: contacts0){
            if(!item.contains(textToSearch)){
                contacts1.remove(item);
            }
        }
        new CustomList().notifyDataSetChanged();

    }

    public class CustomList extends BaseAdapter{

        @Override
        public int getCount() {
            return contacts1.size();
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
            String contact = contacts1.get(position);
            String[] c = contact.split("##");
            num.setText(c[1]);
            name.setText(c[0]);
            return view;
        }
    }
}
