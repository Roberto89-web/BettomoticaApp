package com.domo.bettomotica;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Response";
    SharedPreferences prefs;
    Button postReq;
    TextView txtstate,txtinfo;

    String ip,deviceid,state;


    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("com.domo.bettomotica", 0); // 0 - for private mode
        editor = prefs.edit();

        state = "on";

        postReq = (Button)findViewById(R.id.postswitchReq);
        txtstate = (TextView)findViewById(R.id.txtstate);
        txtinfo = (TextView)findViewById(R.id.txt_info);

        String shPrefip = "com.domo.bettomotica.ip";
        String shPrefDeviceId = "com.domo.bettomotica.deviceid";

    // use a default value using new Date()
        ip = prefs.getString(shPrefip,null);
        deviceid = prefs.getString(shPrefDeviceId,null);

        txtinfo.setText(ip + " - " + deviceid);

        //ip = edtTxtIp.getText().toString();
        //deviceid = edtTxtDeviceId.getText().toString();
        if (ip == null || deviceid == null){
            txtstate.setText("Inserisci id dispositivo e IP");
            Intent intent = new Intent(MainActivity.this, WiFiScanActivity.class);
            startActivity(intent);

        }else {
            try {
                postInfoRequest(ip, deviceid);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        postReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip = prefs.getString("com.domo.bettomotica.ip",null);
                deviceid = prefs.getString("com.domo.bettomotica.deviceid",null);

                if (ip == null || deviceid == null){
                    txtstate.setText("Inserisci id dispositivo e IP");
                }else {
                    try {
                        postSwitchRequest(ip, deviceid, state);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, IstruzioniActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        ip = prefs.getString("com.domo.bettomotica.ip",null);
        deviceid = prefs.getString("com.domo.bettomotica.deviceid",null);

        txtinfo.setText(ip + " - " + deviceid);

        try {
            postInfoRequest(ip, deviceid);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // action with ID action_settings was selected
            case R.id.action_settings:
                Intent intent = new Intent(this, WiFiScanActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;



    }



    public void postSwitchRequest(String ip, String deviceid, String StrState) throws IOException {



        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://"+ ip +":8081/zeroconf/switch";

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();

        //name = ((city.getName() == null) ? "N/A" : city.getName());
        String switchState;
        if (StrState.contains("on")){
            switchState = "off";
        }else{
            switchState = "on";
        }

        txtstate.setText(switchState);
        state = switchState;

        String data =  "{\"deviceid\": \"" + deviceid + "\", \"data\": { \"switch\": \"" + switchState + "\" } }";
        RequestBody body = RequestBody.create(MEDIA_TYPE, data);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();
                Log.e(TAG, mMessage);
            }
        });
    }

    public void postInfoRequest(String ip, String deviceid) throws IOException {


        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://"+ ip +":8081/zeroconf/info";

        OkHttpClient client = new OkHttpClient();

        String data =  "{\"deviceid\": \"" + deviceid + "\", \"data\": {  } }";
        RequestBody body = RequestBody.create(MEDIA_TYPE, data);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String mMessage = response.body().string();
                Log.e(TAG, mMessage);
                try {
                    JSONObject jObject = new JSONObject(mMessage);
                    String sdata = jObject.getString("data");

                    JSONObject jdata = new JSONObject(sdata);

                    state = "";
                    state = jdata.getString("switch");
                    txtstate.setText(state);

                }catch (JSONException e){
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}