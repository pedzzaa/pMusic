package com.example.pmusic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MusicUI {
    private static final int PERMISSION_REQUEST_CODE = 1;
    RecyclerView recyclerView;
    TextView noMusicTextView, songTitle;
    ImageButton playPrevious, playNext, pausePlay;
    MyMediaPlayer myMediaPlayer;
    //Button recent, albums, favorites, online;
    SongModel currentSong;
    SQLDatabase myDB;
    Cursor cursor;
    ArrayList<SongModel> songsList;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_txt);
        songTitle = findViewById(R.id.song_name);

        /*
         * recent = findViewById(R.id.recent_page);
         * albums = findViewById(R.id.albums_page);
         * favorites = findViewById(R.id.favorites_page);
         * online = findViewById(R.id.online_page);
        */

        playPrevious = findViewById(R.id.play_previous);
        playNext = findViewById(R.id.play_next);
        pausePlay = findViewById(R.id.pause);

        myMediaPlayer = MyMediaPlayer.getInstance(this);
        myDB = new SQLDatabase(MainActivity.this);
        songTitle.setSelected(true);
        songsList = new ArrayList<>();

        if (!checkPermission()) {
            requestPermissions();
        } else {
            loadSongs("");
        }
    }

    @Override
    public void setResourcesWithMusic(int songID) {
        currentSong = myDB.getSong(songID);

        if(currentSong != null){
            songTitle.setText(currentSong.getTitle());

            pausePlay.setOnClickListener(v -> myMediaPlayer.pausePlay(pausePlay));

            playNext.setOnClickListener(v -> {
                currentSong = myMediaPlayer.isShuffleOn ? myMediaPlayer.shuffleSongs(myDB) : myMediaPlayer.playNext(myDB);
                setResourcesWithMusic(currentSong.getId());
                onResume();
            });

            playPrevious.setOnClickListener(v -> {
                currentSong = myMediaPlayer.isShuffleOn ? myMediaPlayer.shuffleSongs(myDB) : myMediaPlayer.playPrevious(myDB);
                setResourcesWithMusic(currentSong.getId());
                onResume();
            });

            if(!myMediaPlayer.getPlayer().isPlaying()){
                playMusic();
            }

        } else {
            UtilsMain.showToast(MainActivity.this, "Couldn't play the song :(");
        }
    }

    @Override
    public void playMusic() {
        myMediaPlayer.getPlayer().reset();

        try {
            myMediaPlayer.getPlayer().setDataSource(currentSong.getPath());
            myMediaPlayer.getPlayer().prepare();
            myMediaPlayer.getPlayer().start();

            myMediaPlayer.getPlayer().setOnCompletionListener(mp -> {
                currentSong = myMediaPlayer.isShuffleOn ? myMediaPlayer.shuffleSongs(myDB) : myMediaPlayer.playNext(myDB);
                setResourcesWithMusic(currentSong.getId());
                onResume();
            });

        } catch (IOException e) {
            UtilsMain.showToast(MainActivity.this, "Something went wrong :(");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem recreateDB = menu.findItem(R.id.recreate_db);
        recreateDB.setOnMenuItemClickListener(item -> {
            displayReloadDialog();
            return true;
        });

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadSongs(newText);
                return true;
            }
        });
        return true;
    }

    private void loadSongs(String searchQuery) {
        cursor = (searchQuery.isEmpty()) ? myDB.readAllMainTableData() : myDB.searchSongs(searchQuery);
        storeDataInArray(cursor);

        if (UtilsMain.isDatabaseEmpty(MainActivity.this)) {
            UtilsMain.storeSongs(MainActivity.this);
            cursor = (searchQuery.isEmpty()) ? myDB.readAllMainTableData() : myDB.searchSongs(searchQuery);
            storeDataInArray(cursor);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(new MainAdapter(MainActivity.this, cursor, myMediaPlayer));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void displayReloadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Reload songs?");
        builder.setPositiveButton("OK", (dialog, which) -> reloadSongs());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void reloadSongs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please wait while songs are reloading...");
        AlertDialog progressDialog = builder.show();

        new Handler().post(() -> {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                myDB.resetTable();
                UtilsMain.storeSongs(MainActivity.this);
                myMediaPlayer.getPlayer().stop();
                myMediaPlayer.getPlayer().reset();
                cursor.close();

                runOnUiThread(() -> {
                    loadSongs("");
                    progressDialog.dismiss();
                });
            });
        });
    }

    void storeDataInArray(Cursor cursor) {
        Executor executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            if (cursor.getCount() == 0) {
                runOnUiThread(() -> noMusicTextView.setVisibility(View.VISIBLE));
            } else {
                runOnUiThread(() -> noMusicTextView.setVisibility(View.GONE));
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_TITLE));
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_DATA));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(AudioContract.AudioEntry.COLUMN_DURATION));

                    songsList.add(new SongModel(id, path, title, duration));
                }
            }
            runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean permissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false;
                    break;
                }
            }

            if (!permissionsGranted) {
                UtilsMain.showToast(MainActivity.this, "Permissions required!");
                requestPermissions();
            } else {
                new Handler().postDelayed(() -> UtilsMain.showAlertDialog(MainActivity.this, MainActivity.this), 100);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        int selectedPosition = preferences.getInt("selectedPosition", -1);
        if (selectedPosition != -1) {
            recyclerView.scrollToPosition(selectedPosition);
            runOnUiThread(() -> recyclerView.setAdapter(new MainAdapter(MainActivity.this, cursor, myMediaPlayer)));
        }
    }
}