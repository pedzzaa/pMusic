package com.application.pmusic.Fragments;

import android.database.Cursor;

public interface Loading {
    void displaySongs(String searchQuery);
    void storeDataInArray(Cursor cursor);
}