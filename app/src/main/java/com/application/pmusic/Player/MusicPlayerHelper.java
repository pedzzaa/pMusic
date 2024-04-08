package com.application.pmusic.Player;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import java.util.concurrent.TimeUnit;

public class MusicPlayerHelper {
    public static void seekBarChange(SeekBar seekBar, MediaPlayer mediaPlayer, ImageView image){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress);
                    image.setRotation((float) (progress * 1080 / mediaPlayer.getDuration()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.start();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public static String convertToMMS(String duration){
        long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static void vinylAnimation(ObjectAnimator animator, boolean running) {
        if (running) {
            if (!animator.isStarted()) {
                animator.start();
            } else if (animator.isPaused()) {
                animator.resume();
            }
        } else {
            if (animator.isStarted() && !animator.isPaused()) {
                animator.pause();
            }
        }
    }

    public static ObjectAnimator setupRotationAnimator(ImageView icon) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(icon, "rotation", 0f, 360f);
        animator.setDuration(5000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        return animator;
    }
}