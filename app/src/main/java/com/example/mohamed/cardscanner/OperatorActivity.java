package com.example.mohamed.cardscanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mohamed.cardscanner.Utils.PermissionsUtilities;

public class OperatorActivity extends AppCompatActivity {

    // UI components
    private Button etisalatBtn;
    private Button vodafoneBtn;
    private Button orangeBtn;
    private Button weBtn;


    private static String cardNumber;

    private static final int etisalatId = 1;
    private static final int vodafoneId = 2;
    private static final int orangeId = 3;
    private static final int weId = 4;

    private static final String TAG = "OperatorActivity" + "TAGG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        // get card number
        cardNumber = getIntent().getExtras().get("cardNumber").toString();

        // init ui components
        etisalatBtn = findViewById(R.id.btn_etisalat);
        vodafoneBtn = findViewById(R.id.btn_vodafone);
        orangeBtn = findViewById(R.id.btn_orange);
        weBtn = findViewById(R.id.btn_we);


        etisalatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall(cardNumber, etisalatId);
            }
        });

        vodafoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall(cardNumber, vodafoneId);
            }
        });

        orangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall(cardNumber, orangeId);
            }
        });

        weBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall(cardNumber, weId);
            }
        });

    }


    private void makeCall(String number, int operatorId) {
        String finalNum = "";
        switch (operatorId) {
            case 1:
                finalNum = "*556*" + number + "#";
                break;
            case 2:
                finalNum = "*858*" + number + "#";
                break;
            case 3:
                finalNum = "*102*" + number + "#";
                break;
            case 4:
                finalNum = "*555*" + number + "#";
                break;
            default:
                finalNum = "";
                break;
        }

        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", finalNum, null));
        startActivity(intent);
    }


}
