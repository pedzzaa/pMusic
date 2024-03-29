package com.example.pmusic;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class MusicPlayer extends AppCompatActivity implements Music {
    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView musicIcon;
    ImageButton pausePlay, next, previous, chain_shuffle, like;
    SongModel currentSong;
    SharedPreferences preferences;
    MyMediaPlayer myMediaPlayer;
    SQLDatabase db;
    ObjectAnimator rotationAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        next = findViewById(R.id.next);
        chain_shuffle = findViewById(R.id.shuffle);
        like = findViewById(R.id.like);
        previous = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.vinyl);
        db = new SQLDatabase(MusicPlayer.this);
        myMediaPlayer = MyMediaPlayer.getInstance(this);

        titleTv.setSelected(true);
        preferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        rotationAnimator = UtilsPlayer.setupRotationAnimator(musicIcon);
        currentSong = db.getSong(getIntent().getIntExtra("currentSong", -1));

        setResourcesWithMusic(currentSong.getId());
        uiUpdateHandler.post(uiUpdateRunnable); // Update UI
        UtilsPlayer.seekBarChange(seekBar, myMediaPlayer.getPlayer(), musicIcon);

        initButtonClickListeners();
    }

    private final Handler uiUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable uiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (myMediaPlayer != null) {
                currentTimeTv.setText(UtilsPlayer.convertToMMS(String.valueOf(myMediaPlayer.getPlayer().getCurrentPosition())));
                setShuffleIcon();
                seekBar.setMax(myMediaPlayer.getPlayer().getDuration());
                seekBar.setProgress(myMediaPlayer.getPlayer().getCurrentPosition());

                if (myMediaPlayer.getPlayer().isPlaying()) {
                    pausePlay.setImageResource(R.drawable.baseline_pause_24);
                    UtilsPlayer.vinylAnimation(rotationAnimator, true);
                } else {
                    pausePlay.setImageResource(R.drawable.baseline_play_arrow_24);
                    UtilsPlayer.vinylAnimation(rotationAnimator, false);
                }
                uiUpdateHandler.postDelayed(this, 50);
            }
        }
    };

    @Override
    public void setResourcesWithMusic(int songID) {
        currentSong = db.getSong(songID);

        if (currentSong == null){
            UtilsMain.showToast(MusicPlayer.this, "Couldn't play the song :(");
            return;
        }

        runOnUiThread(() -> {
            titleTv.setText(currentSong.getTitle());
            totalTimeTv.setText(UtilsPlayer.convertToMMS(currentSong.getDuration()));
        });

        if (myMediaPlayer.getPlayer().isPlaying() && songID == myMediaPlayer.getCurrentSongId()) {
            return;
        }

        myMediaPlayer.setCurrentSongId(songID);
        playMusic();
    }

    @Override
    public void playMusic() {
        myMediaPlayer.getPlayer().reset();

        try {
            myMediaPlayer.getPlayer().setDataSource(currentSong.getPath());
            myMediaPlayer.getPlayer().prepare();
            myMediaPlayer.getPlayer().start();
            seekBar.setProgress(0);
            seekBar.setMax(myMediaPlayer.getPlayer().getDuration());

            myMediaPlayer.getPlayer().setOnCompletionListener(mp -> setResourcesWithMusic(myMediaPlayer.shuffleState()
                    ? myMediaPlayer.shuffleSongs() : myMediaPlayer.playNext()));

        } catch (IOException e) {
            UtilsMain.showToast(MusicPlayer.this, "Something went wrong :(");
        }
    }

    private void initButtonClickListeners(){
        pausePlay.setOnClickListener(function -> myMediaPlayer.pausePlay(pausePlay));
        next.setOnClickListener(function -> setResourcesWithMusic(myMediaPlayer.shuffleState()
                ? myMediaPlayer.shuffleSongs() : myMediaPlayer.playNext()));

        previous.setOnClickListener(function -> setResourcesWithMusic(myMediaPlayer.shuffleState()
                ? myMediaPlayer.shuffleSongs() : myMediaPlayer.playPrevious()));

        chain_shuffle.setOnClickListener(function -> {
            myMediaPlayer.toggleShuffle();
            UtilsMain.showToast(MusicPlayer.this, myMediaPlayer.shuffleState() ? "Shuffle is on" : "Shuffle is off");
            setShuffleIcon();
        });
    }

    private void setShuffleIcon(){
        chain_shuffle.setImageResource(myMediaPlayer.shuffleState() ? R.drawable.baseline_shuffle_24 : R.drawable.baseline_cached_24);
    }

    @Override
    protected void onDestroy() {
        uiUpdateHandler.removeCallbacksAndMessages(null);
        next.setOnClickListener(null);
        pausePlay.setOnClickListener(null);
        previous.setOnClickListener(null);
        chain_shuffle.setOnClickListener(null);
        finish();
        super.onDestroy();
    }
}