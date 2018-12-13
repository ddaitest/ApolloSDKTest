package com.example.videoviewtest;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.UCMobile.Apollo.MediaDownloader;

public class TestService extends Service {
    private static boolean DEBUG = true;
    private static String LOGTAG = "MediaDownloader";
    
    MediaDownloader mMediaDownloader;
    MediaDownloader mMediaDownloader2;
    
    public class LocalBinder extends Binder {
        TestService getService() {
                return TestService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG)
            Log.i(LOGTAG, "TestService.onCreate()");
        
        mMediaDownloader = MediaDownloader.create(this, "http://videos-cdn.mozilla.net/serv/webmademovies/Moz_Doc_0329_GetInvolved_ST.webm", null);
        mMediaDownloader.start();
        

        
        new Timer().schedule(new TimerTask(){ 
            public void run() {
                Looper.prepare();
                mMediaDownloader2 = MediaDownloader.create(TestService.this, "http://ips.ifeng.com/video19.ifeng.com/video09/2015/10/02/3582615-102-008-1758.mp4", null);
                mMediaDownloader2.start();
            }
        },6000);     
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.i(LOGTAG, "TestService.onDestroy()");
        mMediaDownloader.stop();
        if (mMediaDownloader2 != null)
            mMediaDownloader2.stop();
        
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    private final IBinder mBinder = new LocalBinder();
}