package com.example.mohamed.cardscanner.view;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mohamed.cardscanner.R;
import com.example.mohamed.cardscanner.utils.PreferenceUtilities;

public class OperatorActivity extends AppCompatActivity {

    // UI components
    private TextView guideText;
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

    // preferences
    private PreferenceUtilities prefUtils;
    private static final int PREF_FONT_SIZE_NORMAL = 18;
    private static final int PREF_FONT_SIZE_LARGE = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator);

        // get card number
        cardNumber = getIntent().getExtras().get("cardNumber").toString();

        // init
        prefUtils = new PreferenceUtilities(this);
        guideText = findViewById(R.id.guide_tv);
        etisalatBtn = findViewById(R.id.btn_etisalat);
        vodafoneBtn = findViewById(R.id.btn_vodafone);
        orangeBtn = findViewById(R.id.btn_orange);
        weBtn = findViewById(R.id.btn_we);

        loadPreferences();

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

    private void loadPreferences() {
        if (prefUtils.getPrefFontSize().equals("0")) {
            guideText.setTextSize(PREF_FONT_SIZE_NORMAL);
            etisalatBtn.setTextSize(PREF_FONT_SIZE_NORMAL);
            vodafoneBtn.setTextSize(PREF_FONT_SIZE_NORMAL);
            orangeBtn.setTextSize(PREF_FONT_SIZE_NORMAL);
            weBtn.setTextSize(PREF_FONT_SIZE_NORMAL);
        } else if (prefUtils.getPrefFontSize().equals("1")) {
            guideText.setTextSize(PREF_FONT_SIZE_LARGE);
            etisalatBtn.setTextSize(PREF_FONT_SIZE_LARGE);
            vodafoneBtn.setTextSize(PREF_FONT_SIZE_LARGE);
            orangeBtn.setTextSize(PREF_FONT_SIZE_LARGE);
            weBtn.setTextSize(PREF_FONT_SIZE_LARGE);
        }
    }


}
