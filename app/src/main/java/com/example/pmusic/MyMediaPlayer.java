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
    boolean isShuffleOn = false;
    int totalSongs;

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

    public SongModel shuffleSongs(SQLDatabase db) {
        totalSongs = db.getCount();

        if (totalSongs > 0) {
            int randomID = new Random().nextInt(totalSongs) + 1;
            return db.getSong(randomID);
        }
        return null;
    }

    public boolean shuffleState(){
        return isShuffleOn;
    }

    public void toggleShuffle(){
        isShuffleOn = !isShuffleOn;
    }

    public void setShuffleResources(ImageButton button){
        if(isShuffleOn){
            button.setImageResource(R.drawable.baseline_shuffle_24);
        }else{
            button.setImageResource(R.drawable.baseline_cached_24);
        }
    }

    public SongModel playNext(SQLDatabase db) {
        mediaPlayer.reset();
        totalSongs = db.getCount();

        SongModel currentSong = db.getSong(getCurrentSongId());
        SongModel nextSong;

        if (currentSong != null && currentSong.getId() != totalSongs) {
            nextSong = db.getSong(currentSong.getId() + 1);
        }else{
            nextSong = db.getSong(1);
        }
        return nextSong;
    }

    public SongModel playPrevious(SQLDatabase db) {
        mediaPlayer.reset();
        totalSongs = db.getCount();

        SongModel currentSong = db.getSong(getCurrentSongId());
        SongModel previousSong;

        if (currentSong != null && currentSong.getId() != 1) {
            previousSong = db.getSong(currentSong.getId() - 1);
        }else{
            previousSong = db.getSong(totalSongs);
        }
        return previousSong;
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