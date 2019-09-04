package com.example.android.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button clearButton, showButton;
    private int flag = 0;
    protected String serverIP, port, lat, lng, userID, numberOfPois;
    protected static ArrayList<Poi> bestPois = new ArrayList<Poi>();
    private ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private MarkerOptions options = new MarkerOptions();
    Vec2<Double, Double> coord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        retrieveValues();
        coord = new Vec2<Double, Double>(Double.parseDouble(lat), Double.parseDouble(lng));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MarkerAdapter adapter = new MarkerAdapter(MapsActivity.this);
        mMap.setInfoWindowAdapter(adapter);
        // Add a marker in the current Position
        LatLng currentPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        mMap.addMarker(new MarkerOptions().position(currentPosition).title("Your Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
        mMap.setTrafficEnabled(true);

        showButton = (Button) findViewById(R.id.showButton);
        clearButton = (Button) findViewById(R.id.clearButton);

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showButton.setText("SHOWED");
                Client myClient = new Client();
                myClient.execute();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < markers.size(); i++) {
                    markers.get(i).remove();
                }
                Toast.makeText(MapsActivity.this, "ALL MARKERS CLEARED", Toast.LENGTH_SHORT);
            }
        });
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);



    }

    private void addMarkers() {
        int i = 0;
        for (Poi poi : bestPois) {
            LatLng temp = new LatLng(poi.getLatitude(), poi.getLongitude());
            latLngs.add(temp);
            options.position(latLngs.get(i));
            options.title(poi.getName());
            options.snippet(poi.getCategory() + "#" + poi.getImage()+"#"+poi.getID());
            options.draggable(false);
            markers.add(mMap.addMarker(options));
            i++;
        }

    }

    private void retrieveValues() {
        Intent intent = new Intent(MapsActivity.this, StartView.class);
        serverIP = getIntent().getStringExtra("SERVER_IP");
        port = getIntent().getStringExtra("PORT");
        lat = getIntent().getStringExtra("LATITUDE");
        lng = getIntent().getStringExtra("LONGITUDE");
        userID = getIntent().getStringExtra("USER_ID");
        numberOfPois = getIntent().getStringExtra("POIS");
    }


    public class Client extends AsyncTask<Void, String, String> {

        private ProgressDialog progressDialog;
        private int i = 0;
        private String resp;
        Socket requestSocket;
        ObjectOutputStream out;
        ObjectInputStream in;

        @Override
        protected String doInBackground(Void... strings) {
            try {
                int pr=Integer.parseInt(port);
                Log.e("IP",serverIP);
                Log.e("Port",port);
                requestSocket = new Socket(serverIP,pr);

                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());

                out.writeInt(Integer.parseInt(userID));
                out.writeObject(coord);
                out.writeInt(Integer.parseInt(numberOfPois));

                out.flush();

                try {
                    bestPois = (ArrayList<Poi>)in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (out != null && requestSocket != null) {
                        out.close();
                        requestSocket.close();
                    }

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

            return resp = numberOfPois + " Pois Found";
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MapsActivity.this, "ProgressDialog", "Wait to Calculate Best Pois");
        }

        @Override
        protected void onPostExecute(String par) {
            // execution of result of Long time consuming operation
            progressDialog.setMessage(par);
			addMarkers();
            progressDialog.dismiss();

        }

        @Override
        protected void onProgressUpdate(String... text) {
            //progressDialog.setMessage(text[0]);
        }

    }

}


