package com.application.pmusic.Database;

import android.provider.BaseColumns;

public final class AudioContract implements BaseColumns{
    public static final String AUDIO_TABLE = "AudioData";
    public static final String COLUMN_TITLE = "Title";
    public static final String COLUMN_DATA = "Data";
    public static final String COLUMN_DURATION = "Duration";
    public static final String COLUMN_ALBUM = "Album";
    public static final String COLUMN_FAVORITE = "Favorite";
}