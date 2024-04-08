package com.application.pmusic.Player;

import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.os.IBinder;
import android.widget.SeekBar;
import android.content.Intent;
import android.content.Context;
import android.widget.TextView;
import android.widget.ImageView;
import com.application.pmusic.R;
import android.widget.ImageButton;
import android.content.ComponentName;
import android.animation.ObjectAnimator;
import android.content.ServiceConnection;
import androidx.appcompat.app.AppCompatActivity;
import com.application.pmusic.Main.CommonFunctions;
import com.application.pmusic.Service.MusicService;

public class MusicPlayer extends AppCompatActivity implements MusicPlayerCallback {
    private MusicService musicService;
    private TextView titleTv, currentTimeTv, totalTimeTv;
    private SeekBar seekBar;
    private ImageView musicIcon;
    private ImageButton pausePlay, next, previous, chain_shuffle, like;
    private ObjectAnimator rotationAnimator;

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

        titleTv.setSelected(true);
        rotationAnimator = MusicPlayerHelper.setupRotationAnimator(musicIcon);

        musicService = new MusicService();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        int songId = getIntent().getIntExtra("songId", -1);
        if(songId != 0){
            onMusicPlayerReady(songId);
        }

        initButtonClickListeners();
        new Handler().postDelayed(() -> {
                uiUpdateHandler.post(uiUpdateRunnable);
                MusicPlayerHelper.seekBarChange(seekBar, musicService.getPlayer(), musicIcon);
                setTitleAndDuration();
            }, 100);
    }

    @Override
    public void onMusicPlayerReady(int songId) {
        new Handler().postDelayed(() -> {
            if(MusicService.getCurrentSongId() == songId){
                return;
            }
            musicService.setCurrentSongId(songId);
            musicService.playSong();
        }, 50);
    }

    private final Handler uiUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable uiUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if(musicService.getCurrentSong() != null){
                currentTimeTv.setText(MusicPlayerHelper.convertToMMS(String.valueOf(musicService.getPlayer().getCurrentPosition())));
                setShuffleAndLikeIcon();

                seekBar.setMax(musicService.getPlayer().getDuration());
                seekBar.setProgress(musicService.getPlayer().getCurrentPosition());

                if (musicService.getPlayer().isPlaying()) {
                    pausePlay.setImageResource(R.drawable.pause);
                    MusicPlayerHelper.vinylAnimation(rotationAnimator, true);
                } else {
                    pausePlay.setImageResource(R.drawable.play);
                    MusicPlayerHelper.vinylAnimation(rotationAnimator, false);
                }
                uiUpdateHandler.postDelayed(this, 50);
            } else {
                CommonFunctions.showToast(MusicPlayer.this, "Something went wrong :(");
            }
        }
    };

    private void initButtonClickListeners(){
        pausePlay.setOnClickListener(function -> musicService.pausePlay());

        next.setOnClickListener(function -> {
            if (musicService.shuffleState()) {
                musicService.shuffleSongs();
            } else {
                musicService.playNext();
            }
            setTitleAndDuration();
        });

        previous.setOnClickListener(function -> {
            musicService.playPrevious();
            setTitleAndDuration();
        });

        like.setOnClickListener(click -> {
            musicService.setFavorite();
            setShuffleAndLikeIcon();
        });

        chain_shuffle.setOnClickListener(function -> {
            musicService.toggleShuffle();
            CommonFunctions.showToast(MusicPlayer.this, musicService.shuffleState() ? "Shuffle is on" : "Shuffle is off");
            setShuffleAndLikeIcon();
        });
    }

    private void setShuffleAndLikeIcon(){
        chain_shuffle.setImageResource(musicService.shuffleState() ? R.drawable.shuffle_songs : R.drawable.continues);
        like.setImageResource(musicService.isFavorite() ? R.drawable.favorite_checked : R.drawable.favorite_unchecked);
    }

    private void setTitleAndDuration(){
        if(musicService.getCurrentSong() != null){
            titleTv.setText(musicService.getCurrentSong().getTitle());
            totalTimeTv.setText(MusicPlayerHelper.convertToMMS(musicService.getCurrentSong().getDuration()));
        }
    }

    @Override
    protected void onDestroy() {
        uiUpdateHandler.removeCallbacksAndMessages(null);
        next.setOnClickListener(null);
        pausePlay.setOnClickListener(null);
        previous.setOnClickListener(null);
        chain_shuffle.setOnClickListener(null);
        super.onDestroy();
    }
}