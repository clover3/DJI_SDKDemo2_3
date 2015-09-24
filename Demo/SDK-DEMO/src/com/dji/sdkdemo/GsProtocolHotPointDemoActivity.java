package com.dji.sdkdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.midware.data.manager.P3.ServiceManager;
import dji.sdk.api.Camera.DJICameraFileNamePushInfo;
import dji.sdk.api.Camera.DJICameraPlaybackState;
import dji.sdk.api.Camera.DJICameraSDCardInfo;
import dji.sdk.api.Camera.DJICameraSettingsTypeDef;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIError;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHotPointInterestDirection;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHotPointNavigationMode;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationHotPointSurroundDirection;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResult;
import dji.sdk.api.GroundStation.DJIGroundStationMissionPushInfo;
import dji.sdk.api.GroundStation.DJIHotPointInitializationInfo;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJICameraFileNameInfoCallBack;
import dji.sdk.interfaces.DJICameraPlayBackStateCallBack;
import dji.sdk.interfaces.DJICameraSdCardInfoCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIGroundStationMissionPushInfoCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.dji.sdkdemo.widget.PopupNumberPicker;


public class GsProtocolHotPointDemoActivity extends DemoBaseActivity implements OnClickListener
{
    private static final String TAG = "GsProtocolFollowMeDemoActivity";
    //   private static final int NAVI_MODE_HOT_POINT = 2;
    //   private static final int NAVI_MODE_ATTITUDE = 0;

    protected static final int SHOWTOAST = 1;

    private Button mSetParam;
    private Button mOpenGroundStation;
    private Button mStartHotPoint;
    private Button mPauseHotPoint;
    private Button mResumeHotPoint;
    private Button mCancelHotPoint;
    private Button mCloseGroundStation;

    private Button mStartRecordingBtn;
    private Button mStopRecordingBtn;

    private TextView mConnectStateTextView;
    private TextView mHotPointTextView;

    private Timer mTimer;


    private DJICameraFileNameInfoCallBack mCameraFileNameInfoCallBack = null;

    private int mSdcardRemainSize = 0;

    private boolean stopUpdate;
    private double latitude;
    private double longitude;
    private int height;
    private int radius;
    private int speed;
    private GroundStationHotPointInterestDirection direction;
    private GroundStationHotPointNavigationMode dirNavi;

    private boolean getHomePiontFlag = false;

    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;

    private String HpInfoString = "";

    DJIGroundStationMissionPushInfoCallBack mGroundStationMissionPushInfoCallBack;

    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
        }

    };

    private void checkConnectState(){

        GsProtocolHotPointDemoActivity.this.runOnUiThread(new Runnable(){

            @Override
            public void run()
            {
                if(DJIDrone.getDjiCamera() != null){
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if(bConnectState){
                        mConnectStateTextView.setText(R.string.camera_connection_ok);
                    }
                    else{
                        mConnectStateTextView.setText(R.string.camera_connection_break);
                    }
                }
            }
        });

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        height = 30;
        radius = 5;
        speed = 5;
        stopUpdate = false;
        direction = GroundStationHotPointInterestDirection.South;
        dirNavi = GroundStationHotPointNavigationMode.SurroundHeadingTowardHotPoint;

        setContentView(R.layout.activity_gs_protocol_hotpoint_demo);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView_gs);

        mSetParam = (Button)findViewById(R.id.ParamBtn);
        mOpenGroundStation = (Button)findViewById(R.id.OpenGsButton);
        mStartHotPoint = (Button)findViewById(R.id.StartHotPointBtn);
        mPauseHotPoint = (Button)findViewById(R.id.PauseHotPoint);
        mResumeHotPoint = (Button)findViewById(R.id.ResumeHotPoint);
        mCancelHotPoint = (Button)findViewById(R.id.CancelHotPoint);
        mCloseGroundStation = (Button)findViewById(R.id.CloseGroundStation);

        mHotPointTextView = (TextView) findViewById(R.id.HotPointInfoTV);

        mStartRecordingBtn = (Button)findViewById(R.id.StartRecordingButton);
        mStopRecordingBtn = (Button)findViewById(R.id.StopRecordingButton);

        mStartRecordingBtn.setOnClickListener(this);
        mStopRecordingBtn.setOnClickListener(this);

        mConnectStateTextView = (TextView)findViewById(R.id.ConnectStateGsTextView);

        mSetParam.setOnClickListener(this);
        mOpenGroundStation.setOnClickListener(this);
        mStartHotPoint.setOnClickListener(this);
        mPauseHotPoint.setOnClickListener(this);
        mResumeHotPoint.setOnClickListener(this);
        mCancelHotPoint.setOnClickListener(this);
        mCloseGroundStation.setOnClickListener(this);

        mDjiGLSurfaceView.start();

        mReceivedVideoDataCallBack = new DJIReceivedVideoDataCallBack(){

            @Override
            public void onResult(byte[] videoBuffer, int size)
            {
                // TODO Auto-generated method stub
                mDjiGLSurfaceView.setDataToDecoder(videoBuffer, size);
            }


        };

        DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(mReceivedVideoDataCallBack);

        mCameraFileNameInfoCallBack = new DJICameraFileNameInfoCallBack(){

            @Override
            public void onResult(final DJICameraFileNamePushInfo mInfo) {
                // TODO Auto-generated method stub
                Log.d(TAG, "camera file info type = "+mInfo.type.toString());
                Log.d(TAG, "camera file info filePath = "+mInfo.filePath);
                Log.d(TAG, "camera file info fileName = " + mInfo.fileName);
            }
        };

        DJIDrone.getDjiCamera().setDjiCameraFileNameInfoCallBack(mCameraFileNameInfoCallBack);


        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(new DJIMcuUpdateStateCallBack() {

            @Override
            public void onResult(DJIMainControllerSystemState state) {
                // TODO Auto-generated method stubx
                if (!stopUpdate) {
                    latitude = state.homeLocationLatitude;
                    longitude = state.homeLocationLongitude;
                }

                if (latitude != -1 && longitude != -1 && latitude != 0 && longitude != 0) {
                    getHomePiontFlag = true;
                } else {
                    getHomePiontFlag = false;
                }

            }

        });


        mGroundStationMissionPushInfoCallBack = new DJIGroundStationMissionPushInfoCallBack() {

            @Override
            public void onResult(DJIGroundStationMissionPushInfo info) {
                // TODO Auto-generated method stub
                StringBuffer sb = new StringBuffer();
                switch(info.missionType) {
                    case Navi_Mode_Hotpoint : {
                        sb.append("Mission Type : " + info.missionType.toString()).append("\n");
                        sb.append("Mission Status : " + info.hotPointMissionStatus).append("\n");
                        sb.append("Hot Point Radius : " + info.hotPointRadius/100d).append("\n");
                        sb.append("Hot Point Reason : " + info.hotPointReason).append("\n");
                        break;
                    }

                    case Navi_Mode_Attitude : {
                        sb.append("Mission Type : " + info.missionType.toString()).append("\n");
                        sb.append("Mission Reserve : " + info.reserved).append("\n");
                        break;
                    }

                    default :
                        sb.append("Wrong Selection").append("\n");
                }

                HpInfoString = sb.toString();

                GsProtocolHotPointDemoActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mHotPointTextView.setText(HpInfoString);
                    }

                });
            }

        };

        DJIDrone.getDjiGroundStation().setGroundStationMissionPushInfoCallBack(mGroundStationMissionPushInfoCallBack);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);

        DJIDrone.getDjiMC().startUpdateTimer(1000);
        super.onResume();
        ServiceManager.getInstance().pauseService(false);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }

        super.onPause();
        DJIDrone.getDjiMC().stopUpdateTimer();
        ServiceManager.getInstance().pauseService(true);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        if(DJIDrone.getDjiCamera() != null){
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        }
        mDjiGLSurfaceView.destroy();

        super.onDestroy();
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOWTOAST:
                    setResultToToast((String)msg.obj);
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public void onReturn(View view){
        this.finish();
    }

    private void setResultToToast(String result){
        Toast.makeText(GsProtocolHotPointDemoActivity.this, result, Toast.LENGTH_SHORT).show();
    }

    private boolean checkGetHomePoint(){
        if(!getHomePiontFlag){
            setResultToToast(getString(R.string.gs_not_get_home_point));
        }
        return getHomePiontFlag;
    }
    /**
     * @param v
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {

            case R.id.ParamBtn : {
                Intent retIntent = new Intent(this, googlemap.class);
                startActivityForResult(retIntent, 0);
                break;
            }

            case R.id.OpenGsButton : {

                if(!checkGetHomePoint()) return;

                DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });

                break;
            }

            case R.id.StartHotPointBtn : {
                DJIHotPointInitializationInfo info = new DJIHotPointInitializationInfo();
                info.latitude = this.latitude;
                info.longitude = this.longitude;
                info.altitude = this.height;
                info.radius = this.radius;
                info.velocity = this.speed;
                info.surroundDirection = GroundStationHotPointSurroundDirection.Anit_Clockwise;
                info.interestDirection = direction;
                info.navigationMode = dirNavi;

                DJIDrone.getDjiGroundStation().startHotPoint(info, new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;
            }

            case R.id.PauseHotPoint : {
                DJIDrone.getDjiGroundStation().pauseHotPoint(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;
            }

            case R.id.ResumeHotPoint : {
                DJIDrone.getDjiGroundStation().resumeHotPoint(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;
            }

            case R.id.CancelHotPoint : {
                DJIDrone.getDjiGroundStation().cancelHotPoint(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;
            }

            case R.id.CloseGroundStation : {

                DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecuteCallBack() {

                    @Override
                    public void onResult(GroundStationResult result)
                    {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });
                break;
            }



            case R.id.StartRecordingButton:
                DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback() {

                    @Override
                    public void onResult(DJIError mErr) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Start Recording errorCode = " + mErr.errorCode);
                        Log.d(TAG, "Start Recording errorDescription = " + mErr.errorDescription);
                        String result = "errorCode =" + mErr.errorCode + "\n" + "errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));

                    }

                });
                break;

            case R.id.StopRecordingButton:
                DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback() {

                    @Override
                    public void onResult(DJIError mErr) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Stop Recording errorCode = " + mErr.errorCode);
                        Log.d(TAG, "Stop Recording errorDescription = " + mErr.errorDescription);
                        String result = "errorCode =" + mErr.errorCode + "\n" + "errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));

                    }

                });
                break;


            default : {
                break;
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode,resultCode,intent);
        Bundle extraBundle;
        if( resultCode == RESULT_OK)
        {
            extraBundle = intent.getExtras();
            String str = extraBundle.getString("data");
            String[] astr = str.split(":");
            latitude = Double.parseDouble(astr[0]);
            longitude = Double.parseDouble(astr[1]);
            height = Integer.parseInt(astr[2]);
            speed = Integer.parseInt(astr[3]);
            radius = Integer.parseInt(astr[4]);
            int dir = Integer.parseInt(astr[5]);
            int dir2 = Integer.parseInt(astr[6]);
            direction = GroundStationHotPointInterestDirection.find(dir);
            dirNavi = GroundStationHotPointNavigationMode.find(dir2);
            stopUpdate = true;
        }
    }

}
