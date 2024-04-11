package com.application.pmusic.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import com.application.pmusic.Service.MusicService;
import com.application.pmusic.Service.SongModel;

public class SQLDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AudioFiles";
    private static final int DATABASE_VERSION = 3;

    public SQLDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String databaseTable = "CREATE TABLE " + AudioContract.AUDIO_TABLE + " ("
                + AudioContract._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AudioContract.COLUMN_TITLE + " TEXT, "
                + AudioContract.COLUMN_DATA + " TEXT, "
                + AudioContract.COLUMN_DURATION + " TEXT, "
                + AudioContract.COLUMN_ALBUM + " TEXT, "
                + AudioContract.COLUMN_FAVORITE + " INTEGER)";

        db.execSQL(databaseTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + AudioContract.AUDIO_TABLE);
    }

    public void addSong(String title, String data, String duration, String album, int favorite){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(AudioContract.COLUMN_TITLE, title);
        values.put(AudioContract.COLUMN_DATA, data);
        values.put(AudioContract.COLUMN_DURATION, duration);
        values.put(AudioContract.COLUMN_ALBUM, album);
        values.put(AudioContract.COLUMN_FAVORITE, favorite);

        db.insert(AudioContract.AUDIO_TABLE, null, values);
    }

    public SongModel getSong(int id) {
        String query = "SELECT * FROM " + AudioContract.AUDIO_TABLE
                + " WHERE " + AudioContract._ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_TITLE));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_DATA));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_DURATION));

                return new SongModel(id, data, title, duration);
            } else {
                return null;
            }
        }
    }

    public SongModel getNextFavoriteSong(int id){
        String query = "SELECT * FROM " + AudioContract.AUDIO_TABLE
                + " WHERE " + AudioContract.COLUMN_FAVORITE + " = 1 AND "
                + AudioContract._ID + " > " + id + " ORDER BY " + AudioContract._ID + " ASC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nextId = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_TITLE));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_DATA));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_DURATION));

                return new SongModel(nextId, data, title, duration);
            } else {
                return null;
            }
        }
    }

    public Cursor searchSongs(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                AudioContract._ID,
                AudioContract.COLUMN_TITLE,
                AudioContract.COLUMN_DATA,
                AudioContract.COLUMN_DURATION,
                AudioContract.COLUMN_ALBUM,
                AudioContract.COLUMN_FAVORITE
        };

        String selection = AudioContract.COLUMN_TITLE + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        return db.query(AudioContract.AUDIO_TABLE, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor searchFavorites(String query){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                AudioContract._ID,
                AudioContract.COLUMN_TITLE,
                AudioContract.COLUMN_DATA,
                AudioContract.COLUMN_DURATION,
                AudioContract.COLUMN_ALBUM,
                AudioContract.COLUMN_FAVORITE
        };

        String selection = AudioContract.COLUMN_TITLE + " LIKE ? AND " + AudioContract.COLUMN_FAVORITE + " = 1";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        return db.query(AudioContract.AUDIO_TABLE, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor readAllMainTableData(){
        String query = "SELECT * FROM " + AudioContract.AUDIO_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public Cursor readFavoriteSongs() {
        String query = "SELECT * FROM " + AudioContract.AUDIO_TABLE
                + " WHERE " + AudioContract.COLUMN_FAVORITE + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }

    public void resetTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + AudioContract.AUDIO_TABLE);
        onCreate(db);
    }

    public void toggleFavoriteSong(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE " + AudioContract.AUDIO_TABLE + " SET "
                + AudioContract.COLUMN_FAVORITE + " = CASE "
                + "WHEN " + AudioContract.COLUMN_FAVORITE + " = 0 THEN 1 "
                + "ELSE 0 END "
                + "WHERE " + AudioContract._ID + " = ?";

        db.execSQL(query, new String[]{String.valueOf(id)});
    }

    public int getFavoriteStatus(int songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {String.valueOf(songId)};

        Cursor cursor = db.rawQuery("SELECT " + AudioContract.COLUMN_FAVORITE +
                " FROM " + AudioContract.AUDIO_TABLE +
                " WHERE " + AudioContract._ID + " = ?", selectionArgs);

        int favoriteStatus = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                favoriteStatus = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract.COLUMN_FAVORITE));
            }
            cursor.close();
        }
        db.close();
        return favoriteStatus;
    }

    public int getCount(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + AudioContract.AUDIO_TABLE, null);
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
}