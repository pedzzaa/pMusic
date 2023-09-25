package com.example.pmusic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class UtilsMain {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
    };

    public static String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    public static void storeSongs(Context context) {
        @SuppressLint("Recycle") Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC"
        );

        SQLDatabase db = new SQLDatabase(context);
        int successfulInsertions = 0;

        while (true) {
            assert cursor != null;
            if (!cursor.moveToNext()) break;
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

            long result = db.addSong(title, data, duration);
            if(db.checkResult(result)){
                successfulInsertions++;
            }
        }
        cursor.close();

        if (context instanceof Activity) {
            int finalSuccessfulInsertions = successfulInsertions;
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context,
                    (finalSuccessfulInsertions > 0) ? "Songs loaded successfully" : "Something went wrong",
                    Toast.LENGTH_SHORT).show());
        }
    }

    public static void showAlertDialog(Context context, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Restart application for results");
        builder.setMessage("All required permissions are granted. Please restart the application for the changes to take effect.");

        builder.setPositiveButton("OK", (dialog, which) -> activity.finish());

        builder.show();
    }

    public static boolean isDatabaseEmpty(Context context) {
        SQLDatabase helper = new SQLDatabase(context);
        int count = helper.getCount();
        helper.close();
        return count == 0;
    }
}