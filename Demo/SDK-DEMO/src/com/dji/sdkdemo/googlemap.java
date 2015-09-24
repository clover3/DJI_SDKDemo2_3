package com.dji.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
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
import dji.sdk.interfaces.DJIGerneralListener;
import dji.sdk.interfaces.DJIGroundStationExecuteCallBack;
import dji.sdk.interfaces.DJIMcuUpdateStateCallBack;
import dji.sdk.interfaces.DJIReceivedVideoDataCallBack;
import dji.sdk.widget.DjiGLSurfaceView;
import dji.midware.data.manager.P3.ServiceManager;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class googlemap extends FragmentActivity implements GoogleMap.OnMapClickListener, OnMapReadyCallback {
    ArrayList<String> arraylist;
    ArrayList<String> arraylistNavi;
    Bundle extra;
    Intent intent;
    ToggleButton sate;
    private GoogleMap aMap;
    Marker marker;
    Button btnSubmit;

    EditText editRadius;
    EditText editHeight;
    EditText editSpeed;
    Spinner spinnerDirection;
    Spinner spinnerDirNavi;
    LatLng location;
    int speed;
    int height;
    int radius;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        speed = 1;
        height = 5;
        radius = 5;
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_googlemap);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        editHeight = (EditText) findViewById(R.id.editHeight);
        editRadius = (EditText) findViewById(R.id.editRadius);
        editSpeed = (EditText) findViewById(R.id.editSpeed);
        spinnerDirection = (Spinner) findViewById(R.id.spinner);
        spinnerDirNavi = (Spinner) findViewById(R.id.spinnerNavi);
        sate = (ToggleButton) findViewById(R.id.sate);

        arraylistNavi = new ArrayList<String>();
        arraylistNavi.add("SurroundHeadingAlongTheCircleLookingForward");
        arraylistNavi.add("SurroundHeadingTowardHotPoint");
        arraylistNavi.add("SurroundHeadingBackwardHotPoint");
        arraylistNavi.add("SurroundHeadingControlByRemoteController");
        arraylistNavi.add("SurroundHeadingAlongTheCircleLookingBackward");
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arraylistNavi);
        spinnerDirNavi.setPrompt("NaviDirection");
        spinnerDirNavi.setAdapter(adapter1);

        arraylist = new ArrayList<String>();
        arraylist.add("North");
        arraylist.add("South");
        arraylist.add("West");
        arraylist.add("East");
        arraylist.add("Nearest");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arraylist);
        spinnerDirection.setPrompt("MovDirection");
        spinnerDirection.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        sate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    // Use the satellite map
//                    aMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//                } else {
//                    //Use the normal map
//                    aMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                }
//            }
//        });

        extra = new Bundle();
        intent = new Intent(); //초기화 깜빡 했다간 NullPointerException이라는 짜증나는 놈이랑 대면하게 된다.
        marker = null;
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                height = Integer.parseInt(editHeight.getText().toString());
                speed = Integer.parseInt(editSpeed.getText().toString());
                radius = Integer.parseInt(editRadius.getText().toString());
                long sel = spinnerDirection.getSelectedItemId();
                long sel2 = spinnerDirNavi.getSelectedItemId();

                String str = location.latitude + ":"
                        + location.longitude + ":"
                        + height + ":"
                        + speed + ":"
                        + radius + ":"
                        + sel + ":"
                        + sel2;

                extra.putString("data", str);
                intent.putExtras(extra);
                setResult(RESULT_OK, intent); // 성공했다는 결과값을 보내면서 데이터 꾸러미를 지고 있는 intent를 함께 전달한다.
                finish();
            }
        });

        sate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if( sate.isChecked() )
                    aMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    aMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_googlemap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap){
        if( aMap == null )
        {
            aMap = googleMap;
        }
        aMap.setOnMapClickListener(this);

        aMap.setMyLocationEnabled(true);
        UiSettings setting = aMap.getUiSettings();
        setting.setZoomControlsEnabled(true);
        setting.setMyLocationButtonEnabled(true);

        LatLng initPos = new LatLng(36.34, 127.38);
        location = initPos;
        marker = aMap.addMarker(new MarkerOptions().position(initPos).title("selected position"));
        aMap.moveCamera(CameraUpdateFactory.newLatLng(initPos));

    }

    @Override
    public void onMapClick(LatLng point)
    {
        location = point;
        if( marker != null)
            marker.remove();
        marker = aMap.addMarker(new MarkerOptions().position(point).title("Changed position"));
        //marker.setPosition(point);
    }
}
