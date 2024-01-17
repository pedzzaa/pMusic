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
    private boolean isShuffleOn = false;
    private int totalSongs;

    public MyMediaPlayer(Context context) {
        if (context != null) {
            SQLDatabase db = new SQLDatabase(context);
            totalSongs = db.getCount();

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

    public int shuffleSongs() {
        return (totalSongs > 0) ? new Random().nextInt(totalSongs) + 1 : -1;
    }

    public boolean shuffleState(){
        return isShuffleOn;
    }

    public void toggleShuffle(){
        isShuffleOn = !isShuffleOn;
    }

    public int playNext() {
        mediaPlayer.reset();
        return (getCurrentSongId() != totalSongs) ? getCurrentSongId() + 1 : 1;
    }

    public int playPrevious() {
        mediaPlayer.reset();
        return (getCurrentSongId() != 1) ? getCurrentSongId() - 1 : totalSongs;
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