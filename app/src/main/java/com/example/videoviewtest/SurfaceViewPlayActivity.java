package com.example.videoviewtest;

import android.content.res.AssetFileDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.MediaController;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.UCMobile.Apollo.MediaPlayer;
//import android.media.MediaPlayer; //Use Android System Player

import com.UCMobile.Apollo.MediaDownloader;

public class SurfaceViewPlayActivity extends Activity {

	// Log tag.
	private static final String TAG = SurfaceViewPlayActivity.class.getName();
	public static String EXTRA_KEY_FILE_PATH = "filePath"; // Play. string
	private static final String EXTRA_KEY_MEDIACODEC = "MediaCodec"; // enable mediacodec

	private  String fileUrl = "";

	// MediaPlayer instance to control playback of video file.
	private MediaPlayer mMediaPlayer;
    
    private Button mPlayButton; 
    private OnClickListener mPlayButtonOnClickListener;
    
    private Button mScaleButton; 
    private OnClickListener mScaleButtonOnClickListener;
    
    private SurfaceView mSurfaceView; 
    
    private int mScaleMode = -1;

	private boolean mEnableMediaCodec = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surfaceviewvideo_activity);
	
		fileUrl = getIntent().getStringExtra(EXTRA_KEY_FILE_PATH);
		mEnableMediaCodec = getIntent().getBooleanExtra(EXTRA_KEY_MEDIACODEC, true);
				   
        initView();
    }

    private void initView() {

        mPlayButton = (Button)findViewById(R.id.play_button); 
        mPlayButton.setEnabled(false);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);     
        mSurfaceView.getHolder().addCallback(new Callback() {          
            @Override  
            public void surfaceDestroyed(SurfaceHolder holder) {            
            }  

            @Override  
            public void surfaceCreated(SurfaceHolder holder) {   
                mPlayButton.setEnabled(true);
            }             

            @Override  
            public void surfaceChanged(SurfaceHolder holder, int format, int width,  
                    int height) {  
            }
        });
        
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);    
        //mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  //Use Android System Player  
        mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

        mScaleButton = (Button)findViewById(R.id.scale_button); 
        mScaleButton.setEnabled(false);
                        
        mPlayButtonOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {

		        try {
		            mMediaPlayer = new MediaPlayer(SurfaceViewPlayActivity.this);
					mMediaPlayer.setApolloSetting("rw.instance.ap_hwa_enable", mEnableMediaCodec?"1":"0");
		            //mMediaPlayer = new MediaPlayer();//Use Android System Player
		            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(fileUrl));
		            mMediaPlayer.setDisplay(mSurfaceView.getHolder());  
		            //mMediaPlayer.setLooping(true);
		            
		            mMediaPlayer.prepareAsync();	            
		            
		            // Play video when the media source is ready for playback.
		            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
		                @Override
		                public void onPrepared(MediaPlayer mediaPlayer) {
		                    mediaPlayer.start();
		                }
		            });

		        } catch (IllegalArgumentException e) {
		            Log.d(TAG, e.getMessage());
		        } catch (SecurityException e) {
		            Log.d(TAG, e.getMessage());
		        } catch (IllegalStateException e) {
		            Log.d(TAG, e.getMessage());
		        } catch (IOException e) {
		            Log.d(TAG, e.getMessage());
		        }
		        
		        mPlayButton.setEnabled(false);
		        mScaleButton.setEnabled(false);
			}         	
        };
        
        mScaleButtonOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				mScaleMode++;
				if (mScaleMode > 3)
					mScaleMode = 0;
				
				mScaleButton.setText("Scale " + String.valueOf(mScaleMode));

	            mMediaPlayer.setVideoScalingMode(mScaleMode); 
                //Use Android System Player
			}         	
        };

        mPlayButton.setOnClickListener(mPlayButtonOnClickListener); 
        mScaleButton.setOnClickListener(mScaleButtonOnClickListener);     
    }

    @Override
    protected void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
		    mMediaPlayer.stop();
		    mMediaPlayer.release();
		    mMediaPlayer = null;
		}
    }
}
