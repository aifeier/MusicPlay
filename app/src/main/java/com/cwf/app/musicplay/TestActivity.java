package com.cwf.app.musicplay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by n-240 on 2015/11/17.
 */
public class TestActivity extends AppCompatActivity{

    private String SPTAG = "play_id";
    private ImageView previous, play, next;
    private SeekBar seekBar;
    private MusicService musicService;
    private TextView playnow, playtime;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
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

        musicService = new MusicService(this, MusicLoader.instance(getContentResolver()).getMusicList());
        Intent i = new Intent();
        i.setAction(MusicService.ACTION);
        Intent eintent = new Intent(getExplicitIntent(this,i));
        bindService(eintent, serviceConnection, 0);
        stopService(eintent);
        initPlayView();
        initList();
    }

    /*修复android5.0报Service Intent must be explicit错误*/
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    private ServiceConnection serviceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void initList(){
        ArrayList<MusicLoader.MusicInfo> musicInfos = new ArrayList<>();

        MusicLoader musicLoader =  MusicLoader.instance(this.getContentResolver());
        musicInfos = musicLoader.getMusicList();

        listView = (ListView) findViewById(R.id.listview);
        final ArrayList<MusicLoader.MusicInfo> finalMusicInfos = musicInfos;
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return finalMusicInfos.size();
            }

            @Override
            public MusicLoader.MusicInfo getItem(int position) {
                return finalMusicInfos.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(TestActivity.this).inflate(android.R.layout.simple_list_item_2, null);
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
                musicService.playMusic(position);
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
                if ( getMediaPlayer()!= null) {
                    getMediaPlayer().seekTo(seekBar.getProgress());
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.playNext();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               musicService.playPrevious();
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getMediaPlayer() != null && getMediaPlayer().isPlaying()) {
                    play.setImageResource(R.drawable.start);
                    getMediaPlayer().pause();
                } else if (getMediaPlayer() != null) {
                    play.setImageResource(R.drawable.pause);
                    getMediaPlayer().start();
                } else {
                    play.setImageResource(R.drawable.pause);
                    musicService.playMusic((int) SPUtils.get(TestActivity.this, SPTAG, 0));
                }
            }
        });
    }

    @Subscribe
    public void onEventMainThread(MusicLoader.MusicInfo musicInfo){
        playnow.setText(musicInfo.getTitle().replace(".mp3", "")
                .replace(".flac", "").replace(".acc", ""));
        play.setImageResource(R.drawable.pause);
        seekBar.setMax(getMediaPlayer().getDuration());
        seekBar.setEnabled(true);
        startProgressLoading();
    }

    private void startProgressLoading(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getMediaPlayer() != null && getMediaPlayer().isPlaying())
                    handleProgress.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    Handler handleProgress = new Handler() {
        public void handleMessage(Message msg) {
            seekBar.setProgress(getMediaPlayer().getCurrentPosition());
            playtime.setText(TimeUtils.intToString(getMediaPlayer().getCurrentPosition()/1000));
        };
    };

    private MediaPlayer getMediaPlayer(){
        return musicService.getMediaPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getMediaPlayer().release();
        EventBus.getDefault().unregister(this);
        stopService(new Intent(MusicService.ACTION));
        unbindService(serviceConnection);
    }
}
