package com.example.pmusic;

import android.provider.BaseColumns;

public final class AudioContract {
    private AudioContract(){}

    public static class AudioEntry implements BaseColumns {
        public static final String AUDIO_TABLE = "AudioData";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATA = "data";
        public static final String COLUMN_DURATION = "duration";
    }
}
