package com.example.hongu.apaapa;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by Mikuma on 15/12/25.
 */
public class Sound {
    private MediaPlayer mediaPlayer;
    public Sound(Context context, int resid){
        mediaPlayer = MediaPlayer.create(context, resid);
    }

    public void set(double value, double min, double max){
        if(min < value && value < max){
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }else{
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    public void release(){
        mediaPlayer.release();
    }
}
