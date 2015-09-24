package com.dji.sdkdemo;

import dji.midware.data.manager.P3.ServiceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.MotionEvent;

public class DemoBaseActivity extends Activity{
    private static final int INTERVAL_LOG = 300;
    private static long mLastTime = 0l;
    
    @Override
    protected void onResume() {
        super.onResume();

        ServiceManager.getInstance().pauseService(false);
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        ServiceManager.getInstance().pauseService(true);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            final long current = System.currentTimeMillis();
            if (current - mLastTime < INTERVAL_LOG) {
                Log.d("", "click double");
                mLastTime = 0;
            } else {
                mLastTime = current;
                Log.d("", "click single");
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    
}
