package com.cwf.app.musicplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by n-240 on 2015/11/17.
 */
public class MusicService extends Service{

    public static String ACTION = "com.cwf.app.musicplay.MusicService";
    private String SPTAG = "play_id";
    private Context mContext;

    private  MediaPlayer mediaPlayer;
    private  ArrayList<MusicLoader.MusicInfo> list;
    private int playId;

    public MusicService(){

    }

    public MusicService(Context context, ArrayList<MusicLoader.MusicInfo> musicInfoArrayList){
        mContext = context;
        list = musicInfoArrayList;
    }

    public void playNext(){
        playMusic((++playId) % list.size());
    }

    public void playPrevious(){
        if (playId == 0)
            playId = list.size();
        playMusic((--playId) % list.size());
    }

    public void playMusic(int position){
        if(position > list.size())
            return;
        playId = position;
        SPUtils.put(mContext, SPTAG, position);
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        try {
            play(position);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MediaPlayer getMediaPlayer(){
        return mediaPlayer;
    }

    public void play(final int position) throws IOException {
        mediaPlayer.setDataSource(list.get(position).getUrl());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                EventBus.getDefault().postSticky(list.get(playId));
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playMusic((playId++) % list.size());
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                return false;
            }
        });
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mediaPlayer = new MediaPlayer();

        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }
}
