package com.example.videoviewtest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class TestServiceClient {
    private TestService mBoundService;
    private boolean mIsBound;
    private Context mContext;
    
    public TestServiceClient(Context context) {
        mContext = context;
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = null;
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    void doBindService() {
        mContext.bindService(new Intent(mContext, TestService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {

        if (mIsBound) {
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
}
