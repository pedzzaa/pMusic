package com.example.pmusic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import java.util.Random;

public class MyMediaPlayer {
    private static MyMediaPlayer instance;
    private static MediaPlayer mediaPlayer;
    private SharedPreferences preferences;
    private static final String PREF_CURRENT_SONG_ID = "current_song_id";

    public MyMediaPlayer(Context context) {
        if (context != null) {
            preferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            mediaPlayer = new MediaPlayer();
        }
    }

    public static MyMediaPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new MyMediaPlayer(context);
        }
        return instance;
    }

    public MediaPlayer getPlayer() {
        return mediaPlayer;
    }

    public void setCurrentSongId(int songId) {
        preferences.edit().putInt(PREF_CURRENT_SONG_ID, songId).apply();
    }

    public int getCurrentSongId() {
        return preferences.getInt(PREF_CURRENT_SONG_ID, -1);
    }

    public void shuffleSongs(SQLDatabase db) {
        int totalSongs = db.getCount();
        if (totalSongs > 0) {
            int randomID = new Random().nextInt(totalSongs) + 1;
            SongModel randomSong = db.getSong(randomID);
            if (randomSong != null) {
                setCurrentSongId(randomSong.getId());
            }
        }
    }

    public SongModel playNext(SQLDatabase db) {
        mediaPlayer.reset();
        SongModel currentSong = db.getSong(getCurrentSongId());
        if (currentSong != null) {
            SongModel nextSong = db.getSong(currentSong.getId() + 1);
            if (nextSong != null) {
                setCurrentSongId(nextSong.getId());
            }
            return nextSong;
        }
        return null;
    }

    public SongModel playPrevious(SQLDatabase db) {
        mediaPlayer.reset();
        SongModel currentSong = db.getSong(getCurrentSongId());
        if (currentSong != null) {
            SongModel previousSong = db.getSong(currentSong.getId() - 1);
            if (previousSong != null) {
                setCurrentSongId(previousSong.getId());
            }
            return previousSong;
        }
        return null;
    }

    public void pausePlay(ImageButton button) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            button.setImageResource(R.drawable.baseline_play_arrow_24);
        } else {
            mediaPlayer.start();
            button.setImageResource(R.drawable.baseline_pause_24);
        }
    }
}