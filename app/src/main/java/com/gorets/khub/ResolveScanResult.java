package com.gorets.khub;

import android.content.SharedPreferences;
import android.widget.Toast;

public class ResolveScanResult {



    public static boolean getHandler(String text){
        String[] map = text.split("/");
        if (map[0].equals("keyTOTP")){
            return true;
        }else{
            return false;
        }
    }

    public static void setSecret(String key){

    }


}
