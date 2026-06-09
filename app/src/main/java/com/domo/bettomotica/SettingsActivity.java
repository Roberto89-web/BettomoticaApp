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

public class SettingsActivity extends AppCompatActivity {

    TextView txtDett;
    EditText edtTxt_ssid, edtTxt_pwd;
    Button btnConfigura;

    SharedPreferences prefs;
    String ip,deviceid;

    private static final String TAG = "WiFi setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        String shPrefip = "com.domo.bettomotica.ip";
        String shPrefDeviceId = "com.domo.bettomotica.deviceid";
        prefs = getSharedPreferences("com.domo.bettomotica", 0); // 0 - for private mode

        txtDett = (TextView)findViewById(R.id.txt_det);
        edtTxt_ssid = (EditText)findViewById(R.id.edttxt_ssid);
        edtTxt_pwd = (EditText)findViewById(R.id.edttxt_pwd);
        btnConfigura = (Button)findViewById(R.id.btn_configura);

        ip = prefs.getString(shPrefip,null);
        deviceid = prefs.getString(shPrefDeviceId,null);

        txtDett.setText(ip + " - " +deviceid);

        if (ip == null || deviceid == null) {
            btnConfigura.setEnabled(false);
        }

        btnConfigura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtTxt_pwd.getText().toString().trim() == "" || edtTxt_ssid.getText().toString().trim() == ""){
                    Toast.makeText(getApplicationContext(), "Inserisci SSID e Password della rete Wi Fi", Toast.LENGTH_SHORT)
                            .show();
                 }else{

                    try {
                        postWiFiSettingRequest(ip, deviceid,edtTxt_ssid.getText().toString().trim() ,edtTxt_pwd.getText().toString().trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }


    public void postWiFiSettingRequest(String ip, String deviceid, String ssid, String pwd) throws IOException {



        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://"+ ip +":8081/zeroconf/wifi";

        OkHttpClient client = new OkHttpClient();



        /*
        {
            "deviceid": "100000140e",
            "data": {
                "ssid": "eWeLink",
                "password": "WeLoveIoT"
            }
         }
         */

        String data =  "{\"deviceid\": \"" + deviceid + "\", \"data\": { \"ssid\": \"" + ssid + "\", \"password\": \"" + pwd + "\" }}";
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
