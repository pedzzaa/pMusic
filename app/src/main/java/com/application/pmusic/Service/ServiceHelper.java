package com.application.pmusic.Service;

import android.content.Context;
import android.provider.MediaStore;

import com.application.pmusic.Database.SQLDatabase;

public class ServiceHelper {
    public ServiceHelper(){}

    public static String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
    };

    public static String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    public static boolean isDatabaseEmpty(Context context) {
        SQLDatabase helper = new SQLDatabase(context);
        int count = helper.getCount();
        helper.close();
        return count == 0;
    }
}
