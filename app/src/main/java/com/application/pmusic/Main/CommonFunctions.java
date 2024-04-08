package com.application.pmusic.Main;

import android.content.Context;
import android.widget.Toast;

public class CommonFunctions {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
