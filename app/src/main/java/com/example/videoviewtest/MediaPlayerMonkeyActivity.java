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
import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Random;
import java.lang.Thread;
import android.os.Handler;
import android.os.Message;
import android.os.HandlerThread;
import android.os.Looper;

public class MediaPlayerMonkeyActivity extends Activity implements TextureView.SurfaceTextureListener{

    private static final String TAG = "MediaPlayerMonkey";
	public static final String EXTRA_KEY_FILE_PATH = "filePath"; 
	
    private enum LogPriority  {
    	VERBOSE,
    	DEBUG,
    	INFO,
    	WARNING,
    	ERROR
    }
	
    private MediaPlayer mMediaPlayer = null;
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture = null;    
    private Surface  mSurface = null;
    
    private String mUrls[] = null;
    
    private Random mRandom = new Random();    
    private Handler mPlayerHandler = null;
    private HandlerThread mHandlerThread = null;
    
    private TextView mLogTextView = null;
    private TextView mProgressTextView = null;
    private TextView mInfoTextView = null;
    private ScrollView mLogScrollView = null;

    private MediaPlayerState mPlayerState;

    private Handler mTextViewHandler = null;
    
    private enum MediaPlayerState {
        NONE,
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        STOPPED,
        PAUSED,
        PLAYBACKCOMPLETED,
        END,
        ERROR
    }   
    
    private class TextureViewPlayHandler extends Handler {
        public TextureViewPlayHandler() {

        }

        public TextureViewPlayHandler(Looper looper) {
            super(looper);
        }

        @Override  
        public void handleMessage(Message msg) {  
            int mseconds;
            
            if (msg.what == 0) {
        	  	release();
        	  	if (mHandlerThread != null) {
        	  	    mHandlerThread.quit();
        	  	    mHandlerThread = null;
        	  	    mPlayerHandler = null;
			    }
        	  	return;
            }
       
            switch(mPlayerState) 
            {
              case NONE:
                    mseconds= executeActionInNone();
                    break; 
                    
              case IDLE:
                    mseconds= executeActionInIdle();
                    break; 
                     
              case INITIALIZED:
                    mseconds= executeActionInInitialized();
                    break; 
                     
              case PREPARING:
                    mseconds= executeActionInPreparing();
                    break; 
                     
              case PREPARED:
                    mseconds= executeActionInPrepared();
                    break; 
                     
              case STARTED:
                    mseconds= executeActionInStarted();
                    break; 
                     
              case STOPPED:
                    mseconds= executeActionInStopped();
                    break; 
                     
              case PAUSED:
                    mseconds= executeActionInPaused();
                    break; 
                     
              case PLAYBACKCOMPLETED:
                    mseconds= executeActionInPlaybackCompleted();
                    break; 
                     
              case END:
                    mseconds= executeActionInEnd();
                    break; 
                     
              case ERROR:
                    mseconds= executeActionInError();
                    break; 
                                    
              default:
                    Log.e(TAG, "handleMessage()  mPlayerState:" + mPlayerState);
                    mseconds = 10;
                    break; 
            } 
            
            sendNextMessage(randomInt(mseconds));     
        };  
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.w(TAG, "Activity.onCreate()");  
        super.onCreate(savedInstanceState);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.mediaplayer_monkey_activity);
        
    	mUrls = new String[1];
    	mUrls[0] = getIntent().getStringExtra(EXTRA_KEY_FILE_PATH);

    	initTestUrls();
     
        startHandle();
        
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
        
        mLogTextView  = (TextView) findViewById(R.id.monkeylogView); 
        mInfoTextView  = (TextView) findViewById(R.id.monkeyinfoView); 
        mProgressTextView  = (TextView) findViewById(R.id.monkeyprogressView);     
        mLogScrollView  = (ScrollView) findViewById(R.id.monkeylogscrollView);
    }

    @Override
    protected void onDestroy () {        
    	Log.w(TAG, "Activity.onDestroy()");  
		
    	sendForce();
        super.onDestroy();
    }
            
    @Override
    protected void onPause () {    
    	Log.w(TAG, "Activity.onPause()");    
		
    	sendForce();
        super.onPause();
    }
    
    @Override
    protected void onResume () {
    	Log.w(TAG, "Activity.onResume()");  
    	
        super.onResume();
    }

    private void initTestUrls() {        
		WebConfiguration testUrlsConfig = new WebConfiguration();
		if (testUrlsConfig.isValid()) {
            mUrls = testUrlsConfig.getLines();
        }
    }

    private void startHandle() {        
        mHandlerThread = new HandlerThread("TextureViewPlayThread");
        mHandlerThread.start();
        mPlayerHandler = new TextureViewPlayHandler(mHandlerThread.getLooper());
        
        mTextViewHandler = new Handler() {
            @Override  
            public void handleMessage(Message msg) {
            	if (msg.what == 0) {            	
	            	String log = (String)msg.obj;
	            	if (mLogTextView.getLineCount() <= 256) {
		            	mLogTextView.append(log + "\n");
	            	} else {
		            	mLogTextView.setText(log + "\n");	            	
	            	}
	            	mLogScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            	} else if (msg.what == 1) {  
	            	String url = (String)msg.obj;
	            	mInfoTextView.setText(url);
	        	} else if (msg.what == 2) {  
	            	String state = (String)msg.obj;
	            	mProgressTextView.setText(state);
	        	}
            }
        };
    }

    private int randomInt(int n) {
        int random = 1;
        try {
            random = mRandom.nextInt(n);
        } catch (Exception e) {
            Log.e(TAG, "randomInt() msg:" + e.getMessage() + "  cause:" + e.getCause());
        }

        return random;
    }

    private void sleep(int second) {        
        try {
           Thread.sleep(second);
        } catch (Exception e) {
            Log.e(TAG, "sleep() msg:" + e.getMessage() + "  cause:" + e.getCause());
        }
    }

    private void sendNextMessage(int ms) {
        Message msg = Message.obtain();  
        msg.what = 1;  
        //message.what = DOWNLOAD_IMG;  
        if(mPlayerHandler != null)
            mPlayerHandler.sendMessageDelayed(msg, ms); 
    }
    
    private void sendForce() {
        Message msg = Message.obtain();  
        msg.what = 0;   
        if(mPlayerHandler != null)
            mPlayerHandler.sendMessage(msg);     	
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
    	Log.i(TAG, "onSurfaceTextureAvailable()");
    	mSurfaceTexture = surfaceTexture;

    	setPlayerState(MediaPlayerState.NONE);
        sendNextMessage(randomInt(100));
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    	Log.i(TAG, "onSurfaceTextureSizeChanged()");
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {   
    	Log.i(TAG, "onSurfaceTextureDestroyed()");
    	
    	mSurfaceTexture = null;
        sendForce();		    	
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    	//Log.i(TAG, "onSurfaceTextureUpdated()");
    }
    
    private void printMediaPlayerLog(LogPriority priority, String log) {
    	if (priority == LogPriority.DEBUG) {
    		Log.d(TAG, log);
    	} else if (priority == LogPriority.INFO) {
    		Log.i(TAG, log);
    	} else if (priority == LogPriority.WARNING) {
    		Log.w(TAG, log);
    	} else if (priority == LogPriority.ERROR) {
    		Log.e(TAG, log);
    	} else if (priority == LogPriority.VERBOSE) {
    		Log.v(TAG, log);
    	}
    	
    	mTextViewHandler.sendMessage(Message.obtain(mTextViewHandler, 0, log));
    }
    
    private void setPlayerState (MediaPlayerState state) {
		mPlayerState = state;

        String strState = "";

        switch(state) {
            case NONE:
                strState = "none";
                break;
                
            case IDLE:
                strState = "idle";
                break;
                
            case INITIALIZED:
                strState = "initialized";
                break;
            
            case PREPARING:
                strState = "preparing";
                break;
                
            case PREPARED:
                strState = "prepared";
                break;
                
            case STARTED:
                strState = "started";
                break;
                
            case STOPPED:
                strState = "stopped";
                break;
                
            case PAUSED:
                strState = "paused";
                break;
                
            case PLAYBACKCOMPLETED:
                strState = "PlaybackCompleted";
                break;
                
            case END:
                strState = "end";
                break;
                
            case ERROR:
                strState = "error";
                break;

            default:
                break;
        }
        		
		mTextViewHandler.sendMessage(Message.obtain(mTextViewHandler, 2, strState));
    	Log.i(TAG, "setPlayerState: " + state);
    }        
    
    private void initMediaPlayer() {
        if (mMediaPlayer == null) {            
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.init()"); 
            mMediaPlayer = new MediaPlayer(this);       
            //mMediaPlayer = new MediaPlayer();          
		    setPlayerState(MediaPlayerState.IDLE);
		    
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {            
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    printMediaPlayerLog(LogPriority.WARNING, String.format("MediaPlayer.OnErrorListener.onError()  what:%d", what));
                    setPlayerState(MediaPlayerState.ERROR);	            
                    sendNextMessage(randomInt(3000));
                	return true;
                }
            });
            
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {            
                @Override 
                public void onCompletion(MediaPlayer mp) {
                    printMediaPlayerLog(LogPriority.WARNING, "MediaPlayer.OnErrorListener.onCompletion()");
                    setPlayerState(MediaPlayerState.PLAYBACKCOMPLETED);	           
                    sendNextMessage(randomInt(3000));
                }
            });          
            
            mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                	
                }
            });
            

            setSurface();
        }                        
    }   

    private int getDuration() {
        int dur = mMediaPlayer.getDuration();
        //Log.d(TAG, "MediaPlayer.getDuration()  " + dur + "ms"); 
        return dur;
    }
    
    private void setSurface() {     
        if(mMediaPlayer == null)
            return;

        if (mSurface == null && mSurfaceTexture != null) {
        	mSurface = new Surface(mSurfaceTexture);    	
            mMediaPlayer.setSurface(mSurface);
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.setSurface()" );
        } else {
        	//mSurface = new Surface(mSurfaceTexture);  
            //mMediaPlayer.setSurface(mSurface);
        }        
    }
    
    private void setDataSource() {    
        try {            
           String url = mUrls[randomInt(mUrls.length)];
           mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(url)); 
           setPlayerState(MediaPlayerState.INITIALIZED);
           printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.setDataSource()  " + url); 
       	   mTextViewHandler.sendMessage(Message.obtain(mTextViewHandler, 1, url));
        } catch (Exception e) {
            printMediaPlayerLog(LogPriority.ERROR, "Exception: MediaPlayer.setDataSource()  msg:" + e.getMessage() + "  cause:" + e.getCause());
        }             
    }    

    private void prepareAsync() {
    	printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.prepareAsync");

        try {
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    printMediaPlayerLog(LogPriority.WARNING, "MediaPlayer.OnPreparedListener.onPrepared()");
		            setPlayerState(MediaPlayerState.PREPARED);		            
                    sendNextMessage(randomInt(10));
                }
            });    
            mMediaPlayer.prepareAsync();    
		    setPlayerState(MediaPlayerState.PREPARING);
	    } catch (Exception e) {
            printMediaPlayerLog(LogPriority.ERROR, "Exception: MediaPlayer.prepareAsync()  msg:" + e.getMessage() + "  cause:" + e.getCause());
	    }
    }   

    private void prepare() {
    	printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.prepare");

        try {
            mMediaPlayer.prepare();	
    		setPlayerState(MediaPlayerState.PREPARED);
	    } catch (Exception e) {
            printMediaPlayerLog(LogPriority.ERROR, "Exception: MediaPlayer.prepare()  msg:" + e.getMessage() + "  cause:" + e.getCause());
	    }
    }   
    
    private void start() {
    	printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.start()");
    	
        mMediaPlayer.start();
		setPlayerState(MediaPlayerState.STARTED);
    }   
    
    private void seekTo(int msec) {
		if (mMediaPlayer != null) {
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.seekTo()  " + msec + " ms"); 
		    mMediaPlayer.seekTo(msec);
		}
    }
    
    private void pause() {
		if (mMediaPlayer != null) {
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.pause()"); 
		    mMediaPlayer.pause();
		    setPlayerState(MediaPlayerState.PAUSED);
		}
    }

    private void stop() {
		if (mMediaPlayer != null) {
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.stop()"); 
		    mMediaPlayer.stop();		              
		    setPlayerState(MediaPlayerState.STOPPED);
		}
    }

    private void reset() {
		if (mMediaPlayer != null) {
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.reset()"); 
		    mMediaPlayer.reset();
		    setPlayerState(MediaPlayerState.IDLE);
		}
    }
    
    private void release() {
		if (mMediaPlayer != null) {
            printMediaPlayerLog(LogPriority.DEBUG, "MediaPlayer.release()"); 
		    mMediaPlayer.release();
		    setPlayerState(MediaPlayerState.END);
		    mMediaPlayer = null;
		    mSurface = null;
		}
    }
    
    private int executeActionInNone() {
        Log.i(TAG, "executeActionInNone()"); 
        initMediaPlayer();
        return 1000;
    }
    
    private int executeActionInIdle() {
        Log.i(TAG, "executeActionInIdle()"); 
        int randomAction = randomInt(100);

        if (randomAction < 96) {            
            setDataSource();
        } else if (randomAction < 98) {            
            reset();
        } else {
            release();
        }

        return 1000;
    }
    
    private int executeActionInInitialized() {    
        Log.i(TAG, "executeActionInInitialized()");   
        int randomAction = randomInt(100);
        
        if (randomAction < 25) {    
            prepare();
            return 1000;
        } else if (randomAction < 96) {
            prepareAsync();
            return 16000;
        } else if (randomAction < 98) {
            reset();
            return 1500;
        } else {
            release();
            return 1500;
        }
    }
    
    private int executeActionInPreparing() {   
        Log.i(TAG, "executeActionInPreparing()");      
        int randomAction = randomInt(100);
        
        if (randomAction < 96) {            
            return 20000;        
        } else if (randomAction < 98) {
            reset();
        } else {
            release();
        }
        
        return 1000;
    }
    
    private int executeActionInPrepared() {    
        Log.i(TAG, "executeActionInPrepared()");   
        int randomAction = randomInt(100);
        
        if (randomAction < 96) {    
            start();        
            return 20000;    
        } else if (randomAction < 98) {
            reset();        
            return 1000;    
        } else {
            release();   
            return 1000;  
        }    
    }
    
    private int executeActionInStarted() {
        Log.i(TAG, "executeActionInStarted()");   
        int randomAction = randomInt(100);
        
        if (randomAction < 85) {    
            seekTo(randomInt(getDuration()));
            return 10000;   
        } else if (randomAction < 90) {
            pause();
            return 5000;  
        } else if (randomAction < 94) {
            stop();
        } else if (randomAction < 96) {
            start();
            return 10000;     
        } else if (randomAction < 98) {
            reset();
        } else {
            release();
        }     
        
        return 1000;        
    }
        
    private int executeActionInStopped() {
        Log.i(TAG, "executeActionInStopped()");   
        int randomAction = randomInt(100);
        
        if (randomAction < 70) {    
            prepareAsync();
        	return 16000;   
        } else if (randomAction < 90) {
            prepare();  
        } else if (randomAction < 96) {
            stop();
        } else if (randomAction < 98) {
            reset();
        } else {
            release();
        }     
        
        return 1000;       
    }
    
    private int executeActionInPaused () {
        Log.i(TAG, "executeActionInPaused()");  
        int randomAction = randomInt(100);
        
        if (randomAction < 50) {    
            seekTo(randomInt(getDuration()));
            return 10000;   
        } else if (randomAction < 55) {
            stop();
        } else if (randomAction < 59) {
            pause();
        } else if (randomAction < 96) {
            start();
            return 10000;     
        } else if (randomAction < 98) {
            reset();
        } else {
            release();
        }     
           
        return 1000;       
    }
    
    private int executeActionInPlaybackCompleted() {
        Log.i(TAG, "executeActionInPlaybackCompleted()");  
        int randomAction = randomInt(100);
        
        if (randomAction < 30) {    
            seekTo(randomInt(getDuration()));
            return 10000;   
        } else if (randomAction < 60) {
            stop();
        } else if (randomAction < 96) {
            start();
            return 5000;     
        } else if (randomAction < 98) {
            reset();
        } else {
            release();
        }     
        
        return 2000;       
    }
    
    private int executeActionInError() {
        Log.i(TAG, "executeActionInError()");  
        int randomAction = randomInt(100);
        
        if (randomAction < 50) {   
            reset();
        } else {
            release();
        }
        
        return 2000;     
    }
    
    private int executeActionInEnd() {
        Log.i(TAG, "executeActionInEnd()");  
        initMediaPlayer();
        
        return 2000;
    }
}
