package com.example.pmusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class SQLDatabase extends SQLiteOpenHelper {
    Context context;
    private static final String DATABASE_NAME = "AudioFiles";
    private static final int DATABASE_VERSION = 1;

    public SQLDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + AudioContract.AudioEntry.AUDIO_TABLE + " ("
                + AudioContract.AudioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AudioContract.AudioEntry.COLUMN_TITLE + " TEXT, "
                + AudioContract.AudioEntry.COLUMN_DATA + " TEXT, "
                + AudioContract.AudioEntry.COLUMN_DURATION + " TEXT)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + AudioContract.AudioEntry.AUDIO_TABLE);
    }

    public long addSong(String title, String data, String duration){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AudioContract.AudioEntry.COLUMN_TITLE, title);
        values.put(AudioContract.AudioEntry.COLUMN_DATA, data);
        values.put(AudioContract.AudioEntry.COLUMN_DURATION, duration);

        return db.insert(AudioContract.AudioEntry.AUDIO_TABLE, null, values);
    }

    public SongModel getSong(int id) {
        String query = "SELECT * FROM " + AudioContract.AudioEntry.AUDIO_TABLE
                + " WHERE " + AudioContract.AudioEntry._ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_TITLE));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_DATA));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_DURATION));

                return new SongModel(id, data, title, duration);
            } else {
                return null;
            }
        }
    }

    public Cursor searchSongs(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                AudioContract.AudioEntry._ID,
                AudioContract.AudioEntry.COLUMN_TITLE,
                AudioContract.AudioEntry.COLUMN_DATA,
                AudioContract.AudioEntry.COLUMN_DURATION
        };

        String selection = AudioContract.AudioEntry.COLUMN_TITLE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        return db.query(
                AudioContract.AudioEntry.AUDIO_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    public Cursor readAllMainTableData(){
        String query = "SELECT * FROM " + AudioContract.AudioEntry.AUDIO_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void resetTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + AudioContract.AudioEntry.AUDIO_TABLE);
        onCreate(db);
    }

    int getCount(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AudioContract.AudioEntry.AUDIO_TABLE, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return count;
    }

    public boolean checkResult(long result){
        return result != -1;
    }
}