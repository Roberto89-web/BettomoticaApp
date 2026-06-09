package com.domo.bettomotica;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.option_activity);

        // Get reference of widgets from XML layout
        final ListView ListView = (ListView) findViewById(R.id.option_list);


        // Initializing a new String Array
        String[] option = new String[] {
                "Modifica Wifi",
                "Modifica Firmware",
                "Usa Interruttore"
        };

        // Create a List from String Array elements
        final List<String> option_list = new ArrayList<String>(Arrays.asList(option));

        // Create an ArrayAdapter from List
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, option_list);

        // DataBind ListView with items from ArrayAdapter
        ListView.setAdapter(arrayAdapter);

        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Object clickItemObj = adapterView.getAdapter().getItem(index);
                if (clickItemObj.toString() == "Modifica Wifi"){
                    Intent intent = new Intent(OptionActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }else if(clickItemObj.toString() == "Modifica Firmware"){
                    Intent intent = new Intent(OptionActivity.this, OTAFirmwareActivity.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(OptionActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                //Toast.makeText(OptionActivity.this, "You clicked " + clickItemObj.toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
