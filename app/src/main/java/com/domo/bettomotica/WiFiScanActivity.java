package com.domo.bettomotica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class WiFiScanActivity extends AppCompatActivity {

    private ListView listView;
    private Button buttonScan;

    private ArrayList<String> arrayList = new ArrayList<>();
    public ArrayAdapter adapter;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private static final String TAG = "Listener";
    private static final String SERVICE_TYPE = "_ewelink._tcp";

    NsdManager.DiscoveryListener discoveryListener;
    NsdManager nsdMgr;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifiscan_activity);

        prefs = getSharedPreferences("com.domo.bettomotica", 0); // 0 - for private mode
        editor = prefs.edit();

        buttonScan = findViewById(R.id.scanBtn);
        buttonScan.setText("Inizia la ricerca...");

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonScan.getText().toString().contains("Inizia")) {
                    scanWifi();
                    adapter.notifyDataSetChanged();
                    buttonScan.setText("Ferma la ricerca...");
                }else{
                    nsdMgr.stopServiceDiscovery(discoveryListener);
                    buttonScan.setText("Inizia la ricerca...");
                    arrayList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        listView = findViewById(R.id.wifiList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String ip = "";
                String deviceid = "";
                String shPrefip = "com.domo.bettomotica.ip";
                String shPrefDeviceId = "com.domo.bettomotica.deviceid";

                String selectedFromList = (listView.getItemAtPosition(position).toString());
                ip =  selectedFromList.substring(0, selectedFromList.indexOf(" ",0));
                //deviceid = selectedFromList.substring(0, selectedFromList.indexOf("-",0));

                deviceid = selectedFromList.substring(selectedFromList.indexOf("-",0),selectedFromList.length());
                deviceid = deviceid.replace("- eWeLink_","");
                Log.d(TAG, "Service discovery success" + deviceid);
                editor.putString("com.domo.bettomotica.ip",ip);
                editor.putString("com.domo.bettomotica.deviceid",deviceid);
                editor.apply();

                ip = prefs.getString(shPrefip,null);
                deviceid = prefs.getString(shPrefDeviceId,null);

                Intent intent = new Intent(WiFiScanActivity.this, OptionActivity.class);
                startActivity(intent);
            }
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }

    private void scanWifi() {

        arrayList.clear();
        nsdMgr = (NsdManager) getSystemService(Context.NSD_SERVICE);
        initializeDiscoveryListener();

        nsdMgr.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

        //nsdMgr.discoverServices("SERVICE_TYPE", NsdManager.PROTOCOL_DNS_SD, "_ewelink._tcp");

        //Toast.makeText(this, "Start scanning", Toast.LENGTH_SHORT).show();

    }


    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    String detail;

                    Log.d(TAG, "onServiceFound " + service.getServiceType());
                    nsdMgr.resolveService(service, new NsdManager.ResolveListener(){
                        @Override
                        public void onResolveFailed(NsdServiceInfo service, int errorCode) {

                            Log.d(TAG, "onResolveFailed " + service.getServiceType());
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo service) {
                            String detail;

                            Log.d(TAG, "onServiceResolved " + service.getServiceType());
                            //nsdMgr.resolveService(service, new WiFiScanActivity.MyResolveListener());
                            detail = service.getAttributes().toString()
                                    + " " + service.getServiceName()
                                    + " " + service.getServiceType()
                                    + " " + service.getHost()
                                    + " " + service.getPort();

                            arrayList.add(service.getHost().getHostAddress() + " - " + service.getServiceName());

                            WiFiScanActivity.this.runOnUiThread(new Runnable()
                            {
                                public void run()
                                {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdMgr.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdMgr.stopServiceDiscovery(this);
            }
        };
    }


}

