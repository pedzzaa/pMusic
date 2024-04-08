package com.application.pmusic.Service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import com.application.pmusic.Database.SQLDatabase;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MusicService extends Service {
    public MusicBinder binder = new MusicBinder();
    private final String TAG = "STARTED_SERVICE";
    private static int currentSongId = 0;
    private int totalSongs;
    public static boolean isLoading = false;
    private boolean isShuffleOn = false;
    private SQLDatabase db;
    private SongModel currentSong;
    private MediaPlayer mediaPlayer;

    public MusicService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Service has started...");
        db = new SQLDatabase(getApplicationContext());
        currentSong = null;

        if (ServiceHelper.isDatabaseEmpty(getApplicationContext())) {
            loadSongsIntoDatabase();
        }

        totalSongs = db.getCount();
        mediaPlayer = new MediaPlayer();
    }

    public void shuffleSongs(){
        mediaPlayer.reset();
        setCurrentSongId(totalSongs > 0 ? new Random().nextInt(totalSongs) + 1 : -1);
        playSong();
    }

    public void playNext(){
        mediaPlayer.reset();
        setCurrentSongId(currentSongId != totalSongs ? currentSongId + 1 : 1);
        playSong();
    }

    public void playPrevious(){
        mediaPlayer.reset();
        setCurrentSongId(currentSongId != 1 ? currentSongId - 1 : totalSongs);
        playSong();
    }

    public void pausePlay(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void playSong(){
        mediaPlayer.reset();
        currentSong = db.getSong(currentSongId);

        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                if(isShuffleOn){
                    shuffleSongs();
                } else {
                    playNext();
                }
            });
        } catch (IOException e) {
            Log.d(TAG, "Something went wrong :(");
        }
    }

    public void toggleShuffle(){
        isShuffleOn = !isShuffleOn;
    }

    public boolean shuffleState(){
        return isShuffleOn;
    }

    public void setFavorite(){
        db.toggleFavoriteSong(currentSongId);
    }

    public boolean isFavorite(){
        return db.getFavoriteStatus(currentSongId) == 1;
    }
    public void setCurrentSongId(int songId) {
        currentSongId = songId;
    }

    public static int getCurrentSongId() {
        return currentSongId;
    }

    public SongModel getCurrentSong(){
        return currentSong;
    }

    public MediaPlayer getPlayer(){
        return mediaPlayer;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service is destroyed...");
    }

    public void reloadSongsIntoDatabase(){
        db.resetTable();
        mediaPlayer.stop();
        mediaPlayer.reset();
        loadSongsIntoDatabase();
    }

    private void loadSongsIntoDatabase() {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            isLoading = true;
            Cursor cursor = getApplicationContext().getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    ServiceHelper.projection, ServiceHelper.selection, null,
                    MediaStore.Audio.Media.DATE_ADDED + " DESC"
            );

            assert cursor != null;
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                db.addSong(title, data, duration, null, 0);
            }
            cursor.close();
            isLoading = false;
        });
    }

    public class MusicBinder extends Binder {
        public MusicService getBoundService() {
            return MusicService.this;
        }
    }
}