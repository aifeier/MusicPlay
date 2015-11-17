package com.cwf.app.musicplay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private String SPTAG = "play_id";
    private ListView listView;
    private ArrayList<MusicLoader.MusicInfo> musicInfos;
    private MediaPlayer mediaPlayer;
    private ImageView previous, play, next;
    private int playId;
    private SeekBar seekBar;
    private TextView playnow, playtime;
    private boolean isPlaying = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);
        playId = (int) SPUtils.get(this, SPTAG, 0);
        initList();
        initPlayView();
        TelephonyManager telephonyManger =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManger.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }


    private PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch(state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    System.out.println("挂断");
                    if(mediaPlayer!=null && isPlaying) {
                        mediaPlayer.start();
                        play.setImageResource(R.drawable.pause);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("接听");
                    if(mediaPlayer!=null &&mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPlaying = true;
                        play.setImageResource(R.drawable.start);
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    System.out.println("响铃:来电号码" + incomingNumber);
                    if(mediaPlayer!=null &&mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        isPlaying = true;
                        play.setImageResource(R.drawable.start);
                    }
                    //输出来电号码
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };


    private void initList(){
        musicInfos = new ArrayList<>();

        MusicLoader musicLoader =  MusicLoader.instance(this.getContentResolver());
        musicInfos = musicLoader.getMusicList();

        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return musicInfos.size();
            }

            @Override
            public MusicLoader.MusicInfo getItem(int position) {
                return musicInfos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_2, null);
                }
                MusicLoader.MusicInfo item = getItem(position);
                TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
                textView.setText(item.getTitle().replace(".mp3", "").replace(".flac", "").replace(".acc", ""));
                TextView textView1 = (TextView) convertView.findViewById(android.R.id.text2);
                textView1.setText(TimeUtils.intToString(item.getDuration() / 1000) + " | " +
                        item.getArtist());
                return convertView;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playId = position;
                playMusic(position);
            }
        });
    }


    private void initPlayView(){
        previous = (ImageView) findViewById(R.id.previous);
        play = (ImageView) findViewById(R.id.play);
        next = (ImageView) findViewById(R.id.next);
        playnow = (TextView) findViewById(R.id.playnow);
        playtime = (TextView) findViewById(R.id.playtime);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer!=null){
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic((++playId) % musicInfos.size());
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playId == 0)
                    playId = musicInfos.size();
                playMusic((--playId) % musicInfos.size());
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    play.setImageResource(R.drawable.start);
                    mediaPlayer.pause();
                    isPlaying = false;
                } else if (mediaPlayer != null) {
                    play.setImageResource(R.drawable.pause);
                    mediaPlayer.start();
                    isPlaying = true;
                } else {
                    play.setImageResource(R.drawable.pause);
                    playMusic(playId);
                }
            }
        });
    }


    private void playMusic(int position){
        if(position > musicInfos.size())
            return;
        listView.smoothScrollToPosition(position);
        SPUtils.put(this, SPTAG, position);
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

    private void play(final int position) throws IOException {
        seekBar.setEnabled(false);
        isPlaying = false;
        mediaPlayer.setDataSource(musicInfos.get(position).getUrl());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(false);
        mediaPlayer.prepareAsync();
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                playnow.setText(musicInfos.get(position).getTitle().replace(".mp3", "")
                        .replace(".flac", "").replace(".acc", ""));
                mp.start();
                isPlaying = true;
                play.setImageResource(R.drawable.pause);
                seekBar.setMax(mp.getDuration());
                seekBar.setEnabled(true);
                startProgressLoading();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playMusic((playId++) % musicInfos.size());
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

    private void startProgressLoading(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(mediaPlayer!=null&&mediaPlayer.isPlaying())
                    handleProgress.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    Handler handleProgress = new Handler() {
        public void handleMessage(Message msg) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            playtime.setText(TimeUtils.intToString(mediaPlayer.getCurrentPosition()/1000));
        };
    };

    @Override
    protected void onPause() {
//        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
//            isPlaying = true;
//            mediaPlayer.pause();
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(mediaPlayer!=null&&isPlaying)
//            mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        mediaPlayer.release();
        super.onDestroy();
    }

    /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
