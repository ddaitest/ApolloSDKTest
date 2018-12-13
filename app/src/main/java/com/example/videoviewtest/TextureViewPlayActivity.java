/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.videoviewtest;

//import com.android.cts.media.R;

import java.io.IOException;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import com.UCMobile.Apollo.MediaPlayer;
//import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class TextureViewPlayActivity extends Activity implements TextureView.SurfaceTextureListener{

    private static final String TAG = "TextureViewPlayActivity";
	public static final String EXTRA_KEY_FILE_PATH = "filePath";
    private static final String EXTRA_KEY_MEDIACODEC = "MediaCodec"; // enable mediacodec
	
    private MediaPlayer mMediaPlayer = null;
    private TextureView mTextureView;
    private String mFileUrl = null;
    private SurfaceTexture mSurfaceTexture = null;    
    private Surface  mSurface = null;
    private boolean mEnableMediaCodec = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate()");  
        super.onCreate(savedInstanceState);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.textureviewvideo_activity);
        
    	mFileUrl = getIntent().getStringExtra(EXTRA_KEY_FILE_PATH);
        mEnableMediaCodec = getIntent().getBooleanExtra(EXTRA_KEY_MEDIACODEC, true);

        initPlayVideo();        

        initView();
    }

    private void initView() {
        mTextureView =  new TextureView(this);         
        mTextureView.setSurfaceTextureListener(this);
        
        LinearLayout parentView = (LinearLayout) findViewById(R.id.mediaplayer_framelayout); 
        
        //android.view.ViewGroup.LayoutParams params = (android.view.ViewGroup.LayoutParams)mTextureView.getLayoutParams();
        //params.height = 1200;
        //params.width =  android.view.ViewGroup.LayoutParams.MATCH_PARENT;
        android.view.ViewGroup.LayoutParams params = new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1200);
        mTextureView.setLayoutParams(params);

        parentView.addView(mTextureView, params);

        //setContentView(mTextureView);
    }    

    @Override
    protected void onDestroy () {        
    	Log.i(TAG, "onDestroy()");  
		
        super.onDestroy();
    }
            
    @Override
    protected void onPause () {    
    	Log.i(TAG, "onPause()");    
		if (mMediaPlayer != null) {
		    mMediaPlayer.stop();	
		    if (mSurface != null)
		    	mSurface.release(); 
		    mSurface = null;
		    mMediaPlayer.release();
		    mMediaPlayer = null;
		}
		
        super.onPause();
    }
    
    @Override
    protected void onResume () {
    	Log.i(TAG, "onResume()");  

    	if (mMediaPlayer == null) {            
            initPlayVideo();
            startPlayVideo();     
        }

        super.onResume();
    }
    
    public void initPlayVideo() {
        mMediaPlayer = new MediaPlayer(this);
        mMediaPlayer.setApolloSetting("rw.instance.ap_hwa_enable", mEnableMediaCodec?"1":"0");
        //mMediaPlayer = new MediaPlayer();        
                   
        try {            
           mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mFileUrl));  
        } catch (Exception e) {
           Log.e(TAG, e.getMessage(), e);
        }             
    }

    public void startPlayVideo() {
    	Log.i(TAG, "startPlayVideo()");
    	mSurface = new Surface(mSurfaceTexture);
        mMediaPlayer.setSurface(mSurface);
        //surface.release();

        try {
            mMediaPlayer.prepareAsync();	            
            
            // Play video when the media source is ready for playback.
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                	boolean isPlaying  = mediaPlayer.isPlaying();
                	Log.i(TAG, String.format("onPrePared()  isPlaying:%d", isPlaying ? 1 : 0));
                    mediaPlayer.start();
                    isPlaying  = mediaPlayer.isPlaying();
                	Log.i(TAG, String.format("start()  isPlaying:%d", isPlaying ? 1 : 0));
                }
            });

            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {                
                	//Log.i("TextureViewPlayActivity", String.format("onBufferingUpdate() percent:  %d", percent));
                	
                	//if ( mp.getDuration() > 0)
                	//	Log.d("TextureViewPlayActivity", String.format("MediaPlayer Position Percent: %d", mp.getCurrentPosition()*100/mp.getDuration()));
                	//else
                    //	Log.d("TextureViewPlayActivity", String.format("MediaPlayer Position Percent: 0"));
                }
            });
            
            mMediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                	//Log.i("TextureViewPlayActivity", String.format("onInfo()  what:%d, extra:%d", what, extra));
                	return true;
                }
            });
	    } catch (IllegalArgumentException e) {
	        Log.d(TAG, e.getMessage());
	    } catch (SecurityException e) {
	        Log.d(TAG, e.getMessage());
	    } catch (IllegalStateException e) {
	        Log.d(TAG, e.getMessage());
	    }    
    }   

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
    	Log.i(TAG, "onSurfaceTextureAvailable()");
    	mSurfaceTexture = surfaceTexture;

    	startPlayVideo();
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {   
		if (mMediaPlayer != null) {
		    mMediaPlayer.stop();
		    mMediaPlayer.release();
		    mMediaPlayer = null;
		}
		
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Invoked every time there's a new Camera preview frame
    }

}
