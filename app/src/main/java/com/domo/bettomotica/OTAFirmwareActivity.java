package com.domo.bettomotica;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OTAFirmwareActivity extends AppCompatActivity {

    TextView txtDett;
    EditText edtTxt_ip, edtTxt_filename;
    Button btnConfigura, btnSbloccaOTA;

    SharedPreferences prefs;
    String ip,deviceid;

    private static final String TAG = "WiFi setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otafirmware_activity);

        String shPrefip = "com.domo.bettomotica.ip";
        String shPrefDeviceId = "com.domo.bettomotica.deviceid";
        prefs = getSharedPreferences("com.domo.bettomotica", 0); // 0 - for private mode

        txtDett = (TextView)findViewById(R.id.txt_det);
        edtTxt_ip = (EditText)findViewById(R.id.edttxt_ip);
        edtTxt_filename = (EditText)findViewById(R.id.edttxt_filename);
        btnConfigura = (Button)findViewById(R.id.btn_configura);
        btnSbloccaOTA = (Button)findViewById(R.id.btn_unlock_ota);

        ip = prefs.getString(shPrefip,null);
        deviceid = prefs.getString(shPrefDeviceId,null);

        txtDett.setText(ip + " - " +deviceid);

        if (ip == null || deviceid == null) {
            btnConfigura.setEnabled(false);
            btnSbloccaOTA.setEnabled(false);
        }

        btnSbloccaOTA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    unlockOTA(ip, deviceid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        btnConfigura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtTxt_ip.getText().toString().trim() == "" || edtTxt_filename.getText().toString().trim() == ""){
                    Toast.makeText(getApplicationContext(), "Inserisci IP del server e nome file del Firmware", Toast.LENGTH_SHORT)
                            .show();
                }else{

                    try {
                        postOTAFirmware(ip, deviceid,edtTxt_ip.getText().toString().trim() ,edtTxt_filename.getText().toString().trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }


    public void postOTAFirmware(String ip, String deviceid, String ip_server, String filename_firmware) throws IOException {



        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://"+ ip +":8081/zeroconf/ota_flash";

        OkHttpClient client = new OkHttpClient();
        /*
        7. OTA New Firmware
        **URL: **http://[ip]:[port]/zeroconf/ota_flash

        Return value format: json

        Method: HTTP post

        e.g.

        {
            "deviceid": "100000140e",
            "data": {
                "downloadUrl": "http://192.168.1.184/ota/new_rom.bin",
                "sha256sum": "3213b2c34cecbb3bb817030c7f025396b658634c0cf9c4435fc0b52ec9644667"
            }
         }
         */
        String sha = "74217468b5b4efc9b00cde59688fecc4242c8bbdb2457e4ff1e06c776bc515b1";
        String data =  "{\"deviceid\": \"" + deviceid + "\", \"data\": { \"downloadUrl\": \"http://" + ip + "/" + filename_firmware + "\", \"sha256sum\": \"" + sha + "\" }}";
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
                //txtDett.setText(txtDett.getText() + " " + mMessage);
            }
        });
    }

    public void unlockOTA(String ip, String deviceid) throws IOException {



        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://"+ ip +":8081/zeroconf/ota_unlock";

        OkHttpClient client = new OkHttpClient();
        /*
        6. OTA Function Unlocking
        URL: http://[ip]:[port]/zeroconf/ota_unlock

        Return value format: json

        Method: HTTP post

        e.g.

        {
            "deviceid": "100000140e",
            "data": { }
         }
         */

        String data =  "{\"deviceid\": \"" + deviceid + "\", \"data\": { }}";
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
                txtDett.setText(txtDett.getText() + " " + mMessage);
            }
        });
    }

}
