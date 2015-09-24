package com.dji.sdkdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import dji.midware.data.manager.P3.ServiceManager;
import dji.sdk.api.DJIDrone;
import dji.sdk.api.DJIDroneTypeDef.DJIDroneType;
import dji.sdk.api.DJIError;
import dji.sdk.api.GroundStation.DJIGroundStationTask;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.DJIGroundStationFinishAction;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.DJIGroundStationMovingMode;
import dji.sdk.api.GroundStation.DJIGroundStationTypeDef.GroundStationResult;
import dji.sdk.api.GroundStation.DJIGroundStationWaypoint;
import dji.sdk.api.MainController.DJIMainControllerSystemState;
import dji.sdk.interfaces.DJICameraFileNameInfoCallBack;
import dji.sdk.interfaces.DJICameraPlayBackStateCallBack;
import dji.sdk.interfaces.DJIExecuteResultCallback;
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;


public class GSDemoActivity extends FragmentActivity implements OnClickListener, OnMapClickListener, OnMapReadyCallback {
	protected static final String TAG = "GSDemoActivity";
	
	private GoogleMap aMap;
	
	private Button locate, add, clear;
	private Button config, open, upload, start,pause, resume, stop;
    private Button mStartRecordingBtn;
    private Button mStopRecordingBtn;
	private ToggleButton tb;
    private TextView mConnectStateTextView;
	
	private DJIMcuUpdateStateCallBack mMcuUpdateStateCallBack = null;
	
	private boolean isAdd = false;
	
	private double droneLocationLat, droneLocationLng;
	private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
	private Marker droneMarker = null;
	private DJIGroundStationTask mGroundStationTask = null;
	
	private float altitude = 100.0f;
	private boolean repeatGSTask = false;
	private float speed = 10.0f;
	private DJIGroundStationFinishAction actionAfterFinishTask;
	private DJIGroundStationMovingMode heading;

//    private int DroneCode;
	private final int SHOWDIALOG = 1;
	private final int SHOWTOAST = 2;
	
	private Timer mTimer;
	private TimerTask mTask;

    private DjiGLSurfaceView mDjiGLSurfaceView;
    private DJIReceivedVideoDataCallBack mReceivedVideoDataCallBack = null;
    private DJICameraFileNameInfoCallBack mCameraFileNameInfoCallBack = null;
    private DJICameraPlayBackStateCallBack mCameraPlayBackStateCallBack = null;

    private ArrayList<Integer> heightList;
    private AlertDialog.Builder alertDialogBuilder;
    private View promptsView;
    private LatLng lastPos;

    class Task extends TimerTask {
        //int times = 1;

        @Override
        public void run()
        {
            //Log.d(TAG ,"==========>Task Run In!");
            checkConnectState();
            updateDroneLocation();
        }

    };

    private void checkConnectState(){

        GSDemoActivity.this.runOnUiThread(new Runnable(){

            @Override
        public void run()
            {
                if(DJIDrone.getDjiCamera() != null){
                    boolean bConnectState = DJIDrone.getDjiCamera().getCameraConnectIsOk();
                    if(bConnectState) {
                        mConnectStateTextView.setText(R.string.camera_connection_ok);
                    }
                    else{
                        mConnectStateTextView.setText(R.string.camera_connection_break);

                    }
                }
            }
        });
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
//               case SHOWDIALOG:
//                    showMessage(getString(R.string.demo_activation_message_title),(String)msg.obj);
                case SHOWTOAST:
                    setResultToToast((String)msg.obj);
                    break;
                default:
                    break;
            }
            return false;
        }
    });
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsdemo);

        mDjiGLSurfaceView = (DjiGLSurfaceView)findViewById(R.id.DjiSurfaceView);
//??? ??//

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        config = (Button) findViewById(R.id.config);
        open = (Button) findViewById(R.id.openopen);
        upload = (Button) findViewById(R.id.upload);
        start = (Button) findViewById(R.id.start);
        pause = (Button) findViewById(R.id.pause);
        resume = (Button) findViewById(R.id.resume);
        stop = (Button) findViewById(R.id.stop);
        tb = (ToggleButton) findViewById(R.id.tb);
        
        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        config.setOnClickListener(this);
        open.setOnClickListener(this);
        upload.setOnClickListener(this);
        start.setOnClickListener(this);
        pause.setOnClickListener(this);
        resume.setOnClickListener(this);
        stop.setOnClickListener(this);

        mStartRecordingBtn = (Button)findViewById(R.id.Start_Record);
        mStopRecordingBtn = (Button)findViewById(R.id.Stop_record);
        mStartRecordingBtn.setOnClickListener(this);
        mStopRecordingBtn.setOnClickListener(this);

        heightList = new ArrayList<Integer>();
//??? ??//
        mConnectStateTextView = (TextView)findViewById(R.id.GSCN);
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

        
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// Use the satellite map
					aMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				}else{
					//Use the normal map
					aMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				}
				
			}
		});


//        DroneCode = 1; // Initiate Inspire 1's SDK in function onInitSDK
        mGroundStationTask = new DJIGroundStationTask(); // Initiate an object for GroundStationTask

//        onInitSDK(DroneCode);  // Initiate the SDK for Insprie 1
//        DJIDrone.connectToDrone(); // Connect to Drone

        new Thread(){
            public void run() {
                try {
                    DJIDrone.checkPermission(getApplicationContext(), new DJIGerneralListener() {

                        @Override
                        public void onGetPermissionResult(int result) {
                            // TODO Auto-generated method stub
                            if (result == 0) {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, DJIError.getCheckPermissionErrorDescription(result)));

                                updateDroneLocation(); // Obtain the drone's lat and lng from MCU.
                            } else {
                                handler.sendMessage(handler.obtainMessage(SHOWDIALOG, getString(R.string.demo_activation_error)+DJIError.getCheckPermissionErrorDescription(result)+"\n"+getString(R.string.demo_activation_error_code)+result));

                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        mTimer = new Timer();
        class Task extends TimerTask {
            //int times = 1;

            @Override
            public void run()
            {
                //Log.d(TAG ,"==========>Task Run In!");
                updateDroneLocation();
            }

        };
        mTask = new Task();




        
    }
    

    
    private void setUpMap() {
        aMap.setOnMapClickListener(this);// add the listener for click for amap object 

    }
    

      
    // Update the drone location based on states from MCU.
    private void updateDroneLocation(){
        // Set the McuUpdateSateCallBack
        mMcuUpdateStateCallBack = new DJIMcuUpdateStateCallBack(){

            @Override
            public void onResult(DJIMainControllerSystemState state) {
            	droneLocationLat = state.droneLocationLatitude;
            	droneLocationLng = state.droneLocationLongitude;
            	Log.e(TAG, "drone lat " + state.droneLocationLatitude);
            	Log.e(TAG, "drone lat " + state.homeLocationLatitude);
            	Log.e(TAG, "drone lat " + state.droneLocationLongitude);
            	Log.e(TAG, "drone lat " + state.homeLocationLongitude);
            }     
        };
        Log.e(TAG, "setMcuUpdateState");
        DJIDrone.getDjiMC().setMcuUpdateStateCallBack(mMcuUpdateStateCallBack);
        
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                droneMarker = aMap.addMarker(markerOptions);
            }
        });
    }
   
    
    @Override
    public void onMapClick(LatLng point) {
    	if (isAdd == true){
    		markWaypoint(point);
    		DJIGroundStationWaypoint mDJIGroundStationWaypoint = new DJIGroundStationWaypoint(point.latitude, point.longitude);
    		mGroundStationTask.addWaypoint(mDJIGroundStationWaypoint);
            InvokePopup();
    		//Add waypoints to Waypoint arraylist;
    	}else{
    		// Do not add waypoint;
    	}
    		
    }

    private void InvokePopup() {
        LayoutInflater li = LayoutInflater.from(this);
        promptsView = li.inflate(R.layout.prompts, null);

        alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);
        final EditText inputHeight = (EditText) promptsView.findViewById(R.id.editTextHeight);

        alertDialogBuilder.setCancelable(true).setPositiveButton("Enter",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int index = heightList.size();
                        int height = Integer.parseInt(inputHeight.getText().toString());
                        heightList.add(height);

                        // mGroundStationTask.getWaypointAtIndex(index).speed = speedGSTask;
                        //mGroundStationTask.getWaypointAtIndex(index).altitude = altitude;
                    }
                });

        alertDialogBuilder.show();
    }
    private void markWaypoint(LatLng point){
    	//Create MarkerOptions object
    	MarkerOptions markerOptions = new MarkerOptions();
    	markerOptions.position(point);
    	markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
    	Marker marker = aMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(),marker);
        if( lastPos != null ) {
            Polyline line = aMap.addPolyline(new PolylineOptions().add(lastPos, point).width(5).color(Color.RED));
        }
        lastPos = point;
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.Start_Record:
                DJIDrone.getDjiCamera().startRecord(new DJIExecuteResultCallback(){

                    @Override
                    public void onResult(DJIError mErr)
                    {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Start Recording errorCode = "+ mErr.errorCode);
                        Log.d(TAG, "Start Recording errorDescription = "+ mErr.errorDescription);
                        String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));

                    }

                });
                break;

            case R.id.Stop_record:
                DJIDrone.getDjiCamera().stopRecord(new DJIExecuteResultCallback(){

                    @Override
                    public void onResult(DJIError mErr)
                    {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "Stop Recording errorCode = "+ mErr.errorCode);
                        Log.d(TAG, "Stop Recording errorDescription = "+ mErr.errorDescription);
                        String result = "errorCode =" + mErr.errorCode + "\n"+"errorDescription =" + DJIError.getErrorDescriptionByErrcode(mErr.errorCode);
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, result));

                    }

                });
                break;
            case R.id.locate:{
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place                         	
                break;
            }
            case R.id.add:{
                enableDisableAdd(); 
                break;
            }
            case R.id.clear:{
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        aMap.clear();
                    }
                    
                });
                mGroundStationTask.RemoveAllWaypoint(); // Remove all the waypoints added to the task
                break;
            }
            case R.id.openopen:{
                DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack() {
                    @Override
                    public void onResult(GroundStationResult result) {
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));

                    }
                });
                break;
            }
            case R.id.config:{
                showSettingDialog();
                break;
            }
            case R.id.upload:{
                uploadGroundStationTask();
                break;
            }
            case R.id.start:{
                startGroundStationTask();
                break;
            }
            case R.id.pause:{
                DJIDrone.getDjiGroundStation().pauseGroundStationTask(new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                });
                break;
            }
            case R.id.resume:{
                DJIDrone.getDjiGroundStation().continueGroundStationTask(new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }
                });
                break;
            }
            case R.id.stop:{
                stopGroundStationTask();
                break;
            }
            default:
                break;
        }
    }
    
    private void cameraUpdate(){
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        CameraUpdate cu = CameraUpdateFactory.newLatLng(pos);
        aMap.moveCamera(cu);
        
    }
    
    private void enableDisableAdd(){
        if (isAdd == false) {
            isAdd = true; // the switch for enabling or disabling adding waypoint function
            add.setText("Exit");
        }else{
            isAdd = false;
            add.setText("Add");
        }
    }
    
    private void showSettingDialog(){
        LinearLayout wayPointSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);

        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        final Switch repeatEnable_SW = (Switch) wayPointSettings.findViewById(R.id.repeat);
        final TextView speed_RG = (TextView) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);
        
        repeatEnable_SW.setChecked(repeatGSTask);
        repeatEnable_SW.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked){
                    repeatGSTask = true;
                    repeatEnable_SW.setChecked(true);
                } else {
                    repeatGSTask = false;
                    repeatEnable_SW.setChecked(false);
                }
            }
            
        });
        
//        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                // TODO Auto-generated method stub
//                if (checkedId == R.id.lowSpeed){
//                    speedGSTask = 1.0f;
//                } else if (checkedId == R.id.MidSpeed){
//                    speedGSTask = 3.0f;
//                } else if (checkedId == R.id.HighSpeed){
//                    speedGSTask = 5.0f;
//                }
//            }
//        });
        
        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if (checkedId == R.id.finishNone){
                    actionAfterFinishTask = DJIGroundStationFinishAction.None;
                } else if (checkedId == R.id.finishGoHome){
                    actionAfterFinishTask = DJIGroundStationFinishAction.Go_Home;
                } else if (checkedId == R.id.finishLanding){
                    actionAfterFinishTask = DJIGroundStationFinishAction.Land;
                } else if (checkedId == R.id.finishToFirst){
                    actionAfterFinishTask = DJIGroundStationFinishAction.Back_To_First_Way_Point;
                }
            }
        });
        
        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                if (checkedId == R.id.headingNext){
                    heading = DJIGroundStationMovingMode.GSHeadingTowardNextWaypoint;
                } else if (checkedId == R.id.headingInitDirec){
                    heading = DJIGroundStationMovingMode.GSHeadingUsingInitialDirection;
                } else if (checkedId == R.id.headingRC){
                    heading = DJIGroundStationMovingMode.GSHeadingControlByRemoteController;
                } else if (checkedId == R.id.headingWP){
                    heading = DJIGroundStationMovingMode.GSHeadingUsingWaypointHeading;
                }
            }
        });
        
        
        new AlertDialog.Builder(this)
        .setTitle("")
        .setView(wayPointSettings)
        .setPositiveButton("Finish",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                altitude = Integer.parseInt(wpAltitude_TV.getText().toString());
                speed = Integer.parseInt(speed_RG.getText().toString());
                Log.e(TAG, "altitude " + altitude);
                Log.e(TAG, "repeat " + repeatGSTask);
                Log.e(TAG, "speed " + speed);
                Log.e(TAG, "actionAfterFinishTask " + actionAfterFinishTask);
                Log.e(TAG, "heading " + heading);
                configGroundStationTask();
            }
            
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
            
        })
        .create()
        .show();
    }
    
    private void configGroundStationTask(){
        mGroundStationTask.isLoop = repeatGSTask;
        mGroundStationTask.finishAction=actionAfterFinishTask;
        mGroundStationTask.movingMode = heading;
            for (int i=0; i<mGroundStationTask.wayPointCount; i++){
            mGroundStationTask.getWaypointAtIndex(i).altitude = heightList.get(i);
        }
    }
    
    private void uploadGroundStationTask(){
//        DJIDrone.getDjiGroundStation().openGroundStation(new DJIGroundStationExecuteCallBack(){

//            @Override
//            public void onResult(GroundStationResult result) {
//                // TODO Auto-generated method stub
//                String ResultsString = "return code =" + result.toString();
//                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
///              if (result == GroundStationResult.GS_Result_Success) {
                    DJIDrone.getDjiGroundStation().uploadGroundStationTask(mGroundStationTask, new DJIGroundStationExecuteCallBack(){
    
                        @Override
                        public void onResult(GroundStationResult result) {
                            // TODO Auto-generated method stub
                            String ResultsString = "return code =" + result.toString();
                            handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                        }
                        
                    });
                }
//            }
//
//        });
//    }
    
    private void startGroundStationTask(){
        DJIDrone.getDjiGroundStation().startGroundStationTask(new DJIGroundStationExecuteCallBack() {

            @Override
            public void onResult(GroundStationResult result) {
                // TODO Auto-generated method stub
                String ResultsString = "return code =" + result.toString();
                handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
            }
        });
    }
    
    private void stopGroundStationTask(){
        DJIDrone.getDjiGroundStation().closeGroundStation(new DJIGroundStationExecuteCallBack(){

                    @Override
                    public void onResult(GroundStationResult result) {
                        // TODO Auto-generated method stub
                        String ResultsString = "return code =" + result.toString();
                        handler.sendMessage(handler.obtainMessage(SHOWTOAST, ResultsString));
                    }

                });mGroundStationTask.RemoveAllWaypoint();
    }


    
    public void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void setResultToToast(String result){
        Toast.makeText(GSDemoActivity.this, result, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onResume(){
        mTimer = new Timer();
        Task task = new Task();
        mTimer.schedule(task, 0, 500);
        DJIDrone.getDjiMC().startUpdateTimer(1000);

        super.onResume();
        ServiceManager.getInstance().pauseService(false);
            }
    
    @Override
    protected void onPause(){
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer=null;
        }
        super.onPause();
        DJIDrone.getDjiMC().stopUpdateTimer();
        ServiceManager.getInstance().pauseService(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onDestroy(){
        if(DJIDrone.getDjiCamera() != null)
            DJIDrone.getDjiCamera().setReceivedVideoDataCallBack(null);
        mDjiGLSurfaceView.destroy();
        super.onDestroy();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // TODO Auto-generated method stub
        // Initializing Amap object
        if (aMap == null) {
            aMap = googleMap;
            setUpMap();
        }
        aMap.setOnMapClickListener(this);

        aMap.setMyLocationEnabled(true);
        UiSettings setting = aMap.getUiSettings();
        setting.setZoomControlsEnabled(true);
        setting.setMyLocationButtonEnabled(true);
        
        LatLng initPos = new LatLng(36.330808, 127.374084);
        googleMap.addMarker(new MarkerOptions().position(initPos).title("SoftSangchoo"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(initPos));
    }

}
