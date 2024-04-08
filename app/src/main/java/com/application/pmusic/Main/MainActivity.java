package com.application.pmusic.Main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.application.pmusic.Adapters.VPAdapter;
import com.application.pmusic.Fragments.Recent;
import com.application.pmusic.Player.MusicPlayer;
import com.application.pmusic.R;
import com.application.pmusic.Service.MusicService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 102;
    private RelativeLayout miniPlayer;
    private VPAdapter vpAdapter;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private MusicService musicService;
    private ImageButton playPrevious, pausePlay, playNext;
    private TextView songTitle;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getBoundService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService.onDestroy();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabs);
        miniPlayer = findViewById(R.id.mini_player);
        viewPager2 = findViewById(R.id.view_pager);
        playPrevious = findViewById(R.id.play_previous);
        pausePlay = findViewById(R.id.pause);
        playNext = findViewById(R.id.play_next);
        songTitle = findViewById(R.id.song_name);
        songTitle.setSelected(true);

        vpAdapter = new VPAdapter(this);
        viewPager2.setAdapter(vpAdapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> tab.setText(vpAdapter.titles[position])).attach();

        if (!checkPermission()) {
            requestPermissions();
        }

        musicService = new MusicService();
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        initButtonClickListeners();
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

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Fragment currentFragment = vpAdapter.getFragment(viewPager2.getCurrentItem());
                if (currentFragment instanceof Recent) {
                    ((Recent) currentFragment).displaySongs(newText);
                }
                return true;
            }
        });
        return true;
    }

    private void displayReloadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Reload songs");
        builder.setMessage("This will go through process of reloading songs from phone and may take a while." +
                " Please don't interrupt the process!");
        builder.setPositiveButton("OK", (dialog, which) -> musicService.reloadSongsIntoDatabase());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
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
                CommonFunctions.showToast(MainActivity.this, "Permissions required!");
                requestPermissions();
            } else {
                new Handler().postDelayed(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Restart application for results");
                    builder.setMessage("All required permissions are granted. Please restart the application for the changes to take effect.");
                    builder.setPositiveButton("OK", (dialog, which) -> finish());
                    builder.show();
                }, 100);
            }
        }
    }

    private void initButtonClickListeners(){
        pausePlay.setOnClickListener(click -> musicService.pausePlay());

        playPrevious.setOnClickListener(click -> {
            musicService.playPrevious();
            songTitle.setText(musicService.getCurrentSong().getTitle());
            updateFragmentAdapter();
        });

        playNext.setOnClickListener(click -> {
            musicService.playNext();
            songTitle.setText(musicService.getCurrentSong().getTitle());
            updateFragmentAdapter();
        });

        miniPlayer.setOnClickListener(click -> {
            Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("songId", musicService.getCurrentSongId());
            startActivity(intent);
        });
    }

    private void updateFragmentAdapter(){
        vpAdapter.setCurrentFragmentPosition(viewPager2.getCurrentItem());
        Fragment currentFragment = vpAdapter.getFragment(viewPager2.getCurrentItem());
        if (currentFragment instanceof Recent) {
            currentFragment.onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(musicService.getCurrentSong() != null){
            songTitle.setText(musicService.getCurrentSong().getTitle());
        }
    }
}