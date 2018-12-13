package com.example.videoviewtest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import com.UCMobile.Apollo.MediaPlayer;
import com.UCMobile.Apollo.MediaPlayer.OnCompletionListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class MusicActivity extends ListActivity {
    //播放对象
    private MediaPlayer myMediaPlayer;
    //播放列表
    private List<String> myMusicList = new ArrayList<String>();
    //当前播放歌曲的索引
    private int currentListItem = 0;
    //音乐的路径
    private static final String MUSIC_PATH = new String("/sdcard/");

    private ViewHolder mViewHolder;

    private int mState;

    private final int Idle = 0, Initialized = 1, Prepared = 2, Started = 3, Paused = 4, Stopped = 5, PlaybackCompleted = 6, Error = 7, End = 8;

    public static final String TAG = "MusicActivity";

    private TextView mCurTimeView;
    private TextView mDurationView;
    private Timer _curPosTimer = new Timer();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_activity);

        mState = Idle;
        myMediaPlayer = new MediaPlayer(MusicActivity.this);

        // Set URL edit
        EditText urlEditText = (EditText)findViewById(R.id.url_field);

        urlEditText.setText("http://100.84.35.173:8080/t1Test/audio/Banno-BrijeshShandllya.mp3");


        urlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ( actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE ) {
                    String url = v.getText().toString();
                    if ( url.trim().length() > 0 ) {
                        playMusic(url.trim());
                    }
                }
                return true;
            }
        });

        mCurTimeView = (TextView)findViewById(R.id.music_time);
        mDurationView = (TextView)findViewById(R.id.music_dur);
        _curPosTimer.schedule(new CurPosTimerTask(), 0, 1000);

        findView();
        musicList();
        listener();
    }


    //绑定音乐
    void musicList() {
        File home = new File(MUSIC_PATH);
        if (home.listFiles(new MusicFilter()).length > 0) {
            for (File file : home.listFiles(new MusicFilter())) {
                myMusicList.add(file.getName());
            }
            ArrayAdapter<String> musicList = new ArrayAdapter<String>
                    (MusicActivity.this, R.layout.music_item, myMusicList);
            setListAdapter(musicList);
        }
    }

    //获取按钮
    void findView() {
        mViewHolder = new ViewHolder();
        
        mViewHolder.start = (Button) findViewById(R.id.start);
        mViewHolder.stop = (Button) findViewById(R.id.stop);
        mViewHolder.next = (Button) findViewById(R.id.next);
        mViewHolder.last = (Button) findViewById(R.id.last);
    }


    //监听事件
    void listener() {
        //停止
        mViewHolder.stop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mState = Stopped;
                myMediaPlayer.stop();
                Log.d(TAG, "STOP");
                mViewHolder.start.setBackgroundResource(R.drawable.start);
            }
        });
        //开始
        mViewHolder.start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mState == Started) {
                    myMediaPlayer.pause();
                    Log.d(TAG, "PPAUSE");
                    mState = Paused;
                    mViewHolder.start.setBackgroundResource(R.drawable.start);
                }
                else if (mState == Paused) {
                    myMediaPlayer.start();
                    Log.d(TAG, "START");
                    mState = Started;
                    mViewHolder.start.setBackgroundResource(R.drawable.pause);
                }
                else {
                    playMusic(MUSIC_PATH + myMusicList.get(currentListItem));
                }
            }
        });
        //快进 5s
        mViewHolder.next.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int pos = myMediaPlayer.getCurrentPosition();
                pos = Math.min(pos + 5000, myMediaPlayer.getDuration());
                myMediaPlayer.seekTo(pos);
                Log.d(TAG, "SEEKTO:" + pos);
            }
        });
        //倒退 5s
        mViewHolder.last.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //lastMusic();
                int pos = myMediaPlayer.getCurrentPosition();
                pos = Math.max(pos - 5000, 0);
                myMediaPlayer.seekTo(pos);
                Log.d(TAG, "SEEKTO:" + pos);
            }
        });

    }

    //播放音乐
    void playMusic(String path) {
        try {
            if (null != myMediaPlayer) {
                myMediaPlayer.stop();
                Log.d(TAG, "STOP");
                myMediaPlayer.release();
                Log.d(TAG, "RELEASE");
                myMediaPlayer = null;
            }

            myMediaPlayer = new MediaPlayer(MusicActivity.this);
            myMediaPlayer.setDataSource(path);
            Log.d(TAG, "SETDATASOURCE:" + path);
            myMediaPlayer.prepareAsync();
            Log.d(TAG, "PREPAREASYNC");
            myMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "ONCOMPLETE");
                    nextMusic();
                    Log.d(TAG, "nextMusic end");
                }
            });
            myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mState = Prepared;
                    Log.d(TAG, "ONPREPARED");
                    myMediaPlayer.start();
                    Log.d(TAG, "START: duration=" + mediaPlayer.getDuration());
                    mState = Started;
                    mViewHolder.start.setBackgroundResource(R.drawable.pause);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下一首
    void nextMusic() {
        if (++currentListItem >= myMusicList.size()) {
            currentListItem = 0;
        } else {
            playMusic(MUSIC_PATH + myMusicList.get(currentListItem));
        }
    }

    //上一首
    void lastMusic() {
        if (currentListItem != 0) {
            if (--currentListItem >= 0) {
                currentListItem = myMusicList.size();
            } else {
                playMusic(MUSIC_PATH + myMusicList.get(currentListItem));
            }
        } else {
            playMusic(MUSIC_PATH + myMusicList.get(currentListItem));
        }
    }

    //当用户返回时结束音乐并释放音乐对象
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (_curPosTimer != null) {
                _curPosTimer.cancel();
                _curPosTimer = null;
            }
            if (_curPosTimerHandler != null)
                _curPosTimerHandler = null;

            myMediaPlayer.stop();
            Log.d(TAG, "STOP");
            myMediaPlayer.release();
            Log.d(TAG, "RELEASE");
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //当选择列表项时播放音乐
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        currentListItem = position;
        playMusic(MUSIC_PATH + myMusicList.get(currentListItem));
    }

    public class ViewHolder {
        public Button start;
        public Button stop;
        public Button next;
        public Button last;
    }


    public class MusicFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String filename) {
            return (filename.endsWith(".mp3"));
        }
    }

    class CurPosTimerHandler extends Handler {

        private MusicActivity _musicActivity = null;

        public CurPosTimerHandler(MusicActivity activity) {
            _musicActivity = activity;
        }

        public void handleMessage(Message msg) {
            _musicActivity._updateCurPos();
        }
    }

    private CurPosTimerHandler _curPosTimerHandler = new CurPosTimerHandler(this);

    class CurPosTimerTask extends TimerTask {

        @Override
        public void run()
        {
            Message msg = MusicActivity.this._curPosTimerHandler.obtainMessage();
            MusicActivity.this._curPosTimerHandler.sendMessage(msg);
        }
    }

    private void _updateCurPos()
    {
        if (myMediaPlayer != null) {
            int curPos = myMediaPlayer.getCurrentPosition();
            int min = curPos / 60000, msc = (curPos/1000)%60;
            mCurTimeView.setText((min >= 10 ? min : "0"+min) + ":" + (msc >= 10 ? msc : "0"+msc));

            int dur = myMediaPlayer.getDuration();
            min = dur / 60000; msc = (dur/1000)%60;
            mDurationView.setText((min >= 10 ? min : "0"+min) + ":" + (msc >= 10 ? msc : "0"+msc));
        }
    }
}