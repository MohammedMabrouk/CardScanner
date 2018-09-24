package com.example.mohamed.cardscanner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.mohamed.cardscanner.R;

public class PreferenceUtilities {

    private SharedPreferences sharedPref = null;
    private Context mContext;

    public PreferenceUtilities(Context mContext){
        this.mContext = mContext;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public boolean getPrefSaveImg() {
        return sharedPref.getBoolean(mContext.getString(R.string.pref_save_img_key), false);
    }

    public String getPrefFontSize(){
        return sharedPref.getString(mContext.getString(R.string.pref_font_size_key), "");
    }



}
