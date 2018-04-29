package com.example.android.spotifytestapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;

import android.widget.MediaController;

/**
 * this class will handle the media playback UI
 */

public class MediaPlayBack implements MediaController.MediaPlayerControl {

    private MediaPlayer mMediaPlayer;
    private MediaController mMediaController;
    private Handler mHandler = new Handler();

    public MediaPlayBack(Context context, MediaPlayer mediaPlayer , View anchorView){

        //pass in our player
        mMediaPlayer = mediaPlayer;

        //initialize mediaControl
        mMediaController = new MediaController(context);

        //set the media control
        mMediaController.setMediaPlayer(this);

        //attach a player UI to the view
        mMediaController.setAnchorView(anchorView);


        //need this to show the player in the UI
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mHandler.post(new Runnable() {
                    public void run() {
                        mMediaController.show(100000000);

                    }
                });
            }
        });

    }





    //when press play start the player
    @Override
    public void start() {
        mMediaPlayer.start();
    }
    //when pressed pause pause the player
    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    //get duration and set it
    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();

    }

    //get current position
    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    //set the seek
    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);

    }

    //control play/pause UI change
    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    //get loading value
    @Override
    public int getBufferPercentage() {
        //calulation for showing the percentage completed
        return (mMediaPlayer.getCurrentPosition() * 100) / mMediaPlayer.getDuration();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //TODO create a method to the playListActivity to show the controller
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        mMediaController.show();
//
//        return false;
//    }
}
