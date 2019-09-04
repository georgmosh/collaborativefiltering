package com.example.android.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartView extends AppCompatActivity {

    private Button button;
    private EditText tmp,ip,por,lati,longi,user,poi;
    private String serverIP,port,lat,lng,userID,numberOfPois;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startview);
        ip=(EditText)findViewById(R.id.serverIP);
        por=(EditText)findViewById(R.id.port);
        lati=(EditText)findViewById(R.id.lat);
        longi=(EditText)findViewById(R.id.lng);
        user=(EditText)findViewById(R.id.userID);
        poi=(EditText)findViewById(R.id.numberOfPois);

        button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(checkIP() && checkuserID() && checkPort() && checkLatitude() && checkLongtitude() && checkPOIS()) {
                    Intent intent = new Intent(StartView.this, MapsActivity.class);
                    intent.putExtra("SERVER_IP",serverIP);
                    intent.putExtra("PORT",port);
                    intent.putExtra("LATITUDE",lat);
                    intent.putExtra("LONGITUDE",lng);
                    intent.putExtra("USER_ID",userID);
                    intent.putExtra("POIS",numberOfPois);
                    startActivity(intent);
                }
            }
        });

    }

    private boolean checkIP(){
        tmp = (EditText) findViewById(R.id.serverIP);
        serverIP = tmp.getText().toString();
        if (serverIP.matches("")) {
            Toast.makeText(this, "Please enter a valid IP!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean checkPort(){
        tmp = (EditText) findViewById(R.id.port);
        port = tmp.getText().toString();
        if (port.matches("")) {
            Toast.makeText(this, "Please enter a valid port!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean checkLatitude(){
        tmp = (EditText) findViewById(R.id.lat);
        lat = tmp.getText().toString();
        if (lat.matches("")) {
            Toast.makeText(this, "Please enter valid coordinates!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private boolean checkLongtitude(){
        tmp = (EditText) findViewById(R.id.lng);
        lng = tmp.getText().toString();
        if (lng.matches("")) {
            Toast.makeText(this, "Please enter valid coordinates!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkuserID(){
        tmp = (EditText) findViewById(R.id.userID);
        userID = tmp.getText().toString();
        if (userID.matches("")) {
            Toast.makeText(this, "Something is Missing", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkPOIS(){
        tmp = (EditText) findViewById(R.id.numberOfPois);
        numberOfPois = tmp.getText().toString();
        if (numberOfPois.matches("")) {
            /*
             * Assume default value for POIs amount equals 10.
             */
            numberOfPois = "10";
        }
        return true;
    }
}