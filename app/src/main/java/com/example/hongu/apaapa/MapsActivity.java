package com.example.hongu.apaapa;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.*;
import java.util.jar.Manifest;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks , OnConnectionFailedListener,
        LocationListener, SensorEventListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogeleApiClient;

    //    ここはKUBTSS2017_textの内容
    private static final String TAG = MapsActivity.class.getSimpleName();
    private ReceivedDataAdapter mReceivedDataAdapter;
    private SensorAdapter mSensorAdapter;
    private TextSensorViewThread mTextSensorViewThread;//テキスト形式のUI用スレッド
    private Sound sound;
    private double atmLapse, atmStandard;

    private String url;
    private CloudLoggerService mCloudLoggerService = null;
    private CloudLoggerAdapter mCloudLoggerAdapter;
    private InetAddress inetAddress;
    private CloudLoggerSendThread mCloudLoggerSendThread;


    boolean runflg = true;
    private static final int INTERVAL = 1000;
    private static final int FASTESTINTERVAL = 16;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STRAGE = 2;
    private static final int ADDRESSLOADER_ID = 0;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(INTERVAL)                      //位置情報の更新間隔をミリ秒で指定
            .setFastestInterval(FASTESTINTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);     //位置情報取得要求の優先順位
    private FusedLocationProviderApi mFusedLocationProviderApi = LocationServices.FusedLocationApi;
    private List<LatLng> mRunList = new ArrayList<LatLng>();
    private long mStartTimeMillis;
    private double mMeter = 0.0;
    private double StraightMeter = 0.0;
    private double mElapsedTime = 0.0;
    private double mSpeed = 0.0;
    //private DatabaseHelper mDbHelper;
    private boolean mStart = false;
    private boolean mFirst = false;
    private boolean mStop = false;
    private boolean mAsked = false;
    private Chronometer mChronometer;
    private int f = 0;
    private int i = 0;

    private double Kyotolat = 35.025874;
    private double Kyotolnt = 135.780865;
    SensorManager sensorManager;
    float[] rotationMatrix = new float[9];
    float[] gravity = new float[3];
    float[] geomagnetic = new float[3];
    float[] attitude = new float[3];
    TestView testView;
    LatLng latlng;


    double roll,switching,yaw,pitch,ultsonic;

    private SensorManager mSensorManager = null;
    private SensorEventListener mSensorEventListener = null;

    public float[] getfAttitude() {
        return fAttitude;
    }

    public void setfAttitude(float[] fAttitude) {
        this.fAttitude = fAttitude;
    }

    float[] fAttitude = new float[3];

    private float[] fAccell = null;
    private float[] fMagnetic = null;


    //SubThreadSample[] subThreadSample = new SubThreadSample[50];

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //メンバ変数が初期化されることへの対処
        outState.putBoolean("ASKED", mAsked);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAsked = savedInstanceState.getBoolean("ASKED");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogeleApiClient.connect();
        sensorManager.registerListener( this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener( this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
        String lapseStr = pref.getString(SettingPrefActivity.PREF_KEY_LAPSE, "0.12");
        String standardStr = pref.getString(SettingPrefActivity.PREF_KEY_STANDARD, "1013.25");
        atmLapse = Double.parseDouble(lapseStr);
        atmStandard = Double.parseDouble(standardStr);

//        mTextSensorViewThread.setPressureParam(atmStandard, atmLapse);
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("start");
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER ),
                SensorManager.SENSOR_DELAY_UI );
        mSensorManager.registerListener(
                mSensorEventListener,
                mSensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD ),
                SensorManager.SENSOR_DELAY_UI );
    }

    @Override
    protected void onStop() { // ⇔ onStart
        super.onStop();

        mSensorManager.unregisterListener( mSensorEventListener );
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("create");
        setContentView(R.layout.activity_maps);
        initSensor();
        testView = (TestView) findViewById(R.id.view5);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // スリープ抑制

        mGoogeleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();



        if (Build.VERSION.SDK_INT >= 19) {
            Log.i(TAG, "getExternalFilesDirを呼び出します");
            File[] extDirs = getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS);
            File extSdDir = extDirs[extDirs.length - 1];
            Logger.setExternalDir(extSdDir);
            Log.i(TAG, "getExternalFilesDirが返すパス: " + extSdDir.getAbsolutePath());
        }else{
            Log.e(TAG, "This SDK version is under 18.");
            finish();
        }

       mReceivedDataAdapter = new ReceivedDataAdapter(getBaseContext());

        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Configuration config = getResources().getConfiguration();
        mSensorAdapter = new SensorAdapter(sensorManager, mLocationManager, mReceivedDataAdapter, config);

        //  sound = new Sound(getApplicationContext(), R.drawable.warn05);

        mTextSensorViewThread = new TextSensorViewThread(mSensorAdapter, mReceivedDataAdapter);
        mTextSensorViewThread.start();
        Switch connectSwitch = (Switch) findViewById(R.id.reConnectSwitch);
        connectSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mReceivedDataAdapter.setReconnection(isChecked);
            }
        });

        if (mCloudLoggerService == null) {
            url = "http://quatronic.php.xdomain.jp/birdman/writer.php";
            mCloudLoggerService = new CloudLoggerService(url);
        }
        mCloudLoggerAdapter = new CloudLoggerAdapter(mSensorAdapter,mReceivedDataAdapter,mCloudLoggerService);
        mCloudLoggerSendThread = new CloudLoggerSendThread(mCloudLoggerService);
        mCloudLoggerSendThread.start();



        //   subThreadSample[0] = new SubThreadSample("a", 100, 100);

        System.out.println("ok");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button startbtn = (Button) findViewById(R.id.startbtn);

//        tb.setChecked(false);

        //ボタンが押された時の動き
        startbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (f == 0) {
                    startChronometer();
                    //   subThreadSample[i].start();
                    mStart = true;
                    mFirst = true;
                    mStop = false;
                    mMeter = 0.0;
                    mRunList.clear();
                    f++;
                    i++;
                    mCloudLoggerAdapter.setCount(i);
                    startbtn.setText("STOP");
                } else if (f == 1) {
                    stopChronometer();
                    mStop = true;
                    mStart = false;
                    // [i].stopRunning();
                    f++;
                    mCloudLoggerAdapter.setCount(0);
                    startbtn.setText("RESET");
                } else {
                    // subThreadSample[i] = new SubThreadSample("a", 10, 10);
                    f = 0;
                    startbtn.setText("START");
                    TextView disText = (TextView) findViewById(R.id.textview);
                    TextView straightText = (TextView) findViewById(R.id.textview1);
                    disText.setTextColor(Color.RED);
                    straightText.setTextColor(Color.RED);
                    disText.setText("Distance:");
                    straightText.setText("Straight:");
                }

            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE );

        mSensorEventListener = new SensorEventListener()
        {
            public void onSensorChanged (SensorEvent event) {
                // センサの取得値をそれぞれ保存しておく
                switch( event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        fAccell = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        fMagnetic = event.values.clone();
                        break;
                }

                // fAccell と fMagnetic から傾きと方位角を計算する
                if( fAccell != null && fMagnetic != null ) {
                    // 回転行列を得る
                    float[] inR = new float[9];

                    SensorManager.getRotationMatrix(
                            inR,
                            null,
                            fAccell,
                            fMagnetic );
                    // ワールド座標とデバイス座標のマッピングを変換する
                    float[] outR = new float[9];
                    SensorManager.remapCoordinateSystem(
                            inR,
                            SensorManager.AXIS_X,  // デバイスx軸が地球の何軸になるか
                            SensorManager.AXIS_Z,  // デバイスy軸が地球の何軸になるか
                            outR );
                    // 姿勢を得る
                    // TODO: 回転行列(>_<)

                    SensorManager.getOrientation(
                            outR,
                            fAttitude );

//                    String buf =
//                            "---------- Orientation --------\n" +
//                                    String.format( "方位角\n\t%f\n", rad2deg( fAttitude[0] )) +
//                                    String.format( "前後の傾斜\n\t%f\n", rad2deg( fAttitude[1] )) +
//                                    String.format( "左右の傾斜\n\t%f\n", rad2deg( fAttitude[2] ));
//
//                    String buf2 =
//                            "---------- fAccell --------\n" +
//                                    String.format( "方位角\n\t%f\n", rad2deg( fAccell[0] )) +
//                                    String.format( "前後の傾斜\n\t%f\n", rad2deg( fAccell[1] )) +
//                                    String.format( "左右の傾斜\n\t%f\n", rad2deg( fAccell[2] ));
//
//                    String buf3 =
//                            "---------- fMagnetic --------\n" +
//                                    String.format( "方位角\n\t%f\n", rad2deg( fMagnetic[0] )) +
//                                    String.format( "前後の傾斜\n\t%f\n", rad2deg( fMagnetic[1] )) +
//                                    String.format( "左右の傾斜\n\t%f\n", rad2deg( fMagnetic[2] ));

//                    setValue(mSensorAdapter.getYaw(), mSensorAdapter.getRoll(), mSensorAdapter.getPitch());
//                    String bush ="---------- Orientation --------\n" +
//                            String.format( "Yaw:\n\t%f\n", yaw) +
//                            String.format( "Roll:\n\t%f\n", roll) +
//                            String.format( "Pitch:\n\t%f\n", pitch);


                    //TextView t = (TextView) findViewById( R.id.textview);
//                    TextView Accell = (TextView) findViewById(R.id.textview1);
//                    TextView Magnetic = (TextView) findViewById(R.id.textview2);
                    //t.setText( buf );
//                    Accell.setText(buf2);
//                    Magnetic.setText(buf3);
                    //float Yaw = fAttitude[0];

                    // 正面に置く場合
                    testView.setYaw1(rad2deg( fAttitude[2] ));
                    testView.setPitch1(rad2deg( fAttitude[1] ));

//                    // 左に置く場合
//                    testView.Yaw = -rad2deg( fAttitude[1] );
//                    testView.Pitch = rad2deg( fAttitude[2] );

//                    System.out.println("testviewはok?");
                    // 再描画
                    testView.invalidate();
                }
            }
            public void onAccuracyChanged (Sensor sensor, int accuracy) {}
        };

        //トグルボタンが押された時の動き
//        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    startChronometer();
//                    subThreadSample.start();
//                    mStart = true;
//                    mFirst = true;
//                    mStop = false;
//                    mMeter = 0.0;
//                    mRunList.clear();
//                } else {
//                    stopChronometer();
//                    mStop = true;
//                    mStart = false;
//                    subThreadSample.stopRunning();
//                }
//            }
//        });


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(MapsActivity.this, SettingPrefActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    private class TextSensorViewThread extends Thread {
        SensorAdapter mSensorAdapter;
        ReceivedDataAdapter mReceivedDataAdapter;
        Handler handler = new Handler(Looper.getMainLooper());

        //        private TextView txtYaw, txtPitch, txtRoll, txtLati, txtLong, txtCnt, txtStraight, txtIntegral;
        private TextView txtStatus, txtSelector;
        //        private TextView txtTime, txtRud, txtEle, txtTrim, txtAirspeed, txtCadence, txtUltsonic, txtAtmpress, txtAltitude;
//        private TextView txtCadencevolt, txtUltsonicvolt, txtServovolt;
        //private GraphView airspeed, speed, rpm, ultsonic;
        GraphView airspeed = (GraphView) findViewById(R.id.air);
        GraphView speed = (GraphView) findViewById(R.id.speed);
        //        GraphView graphView2 = (GraphView) findViewById(R.id.view2);
        GraphView rpm = (GraphView) findViewById(R.id.rpm);
        GraphView ult = (GraphView) findViewById(R.id.ult);
        TextView elevator = (TextView) findViewById(R.id.elevator);
        TextView rudder = (TextView) findViewById(R.id.rudder);
        TextView trim = (TextView) findViewById(R.id.trim);


        private boolean running = true;

        private double atmStandard, atmLapse;

        public TextSensorViewThread(SensorAdapter mSensorAdapter, ReceivedDataAdapter mReceivedDataAdapter) {
            this.mSensorAdapter = mSensorAdapter;
            this.mReceivedDataAdapter = mReceivedDataAdapter;

//            txtYaw = (TextView) findViewById(R.id.textViewYaw);
//            txtPitch = (TextView) findViewById(R.id.textViewPitch);
//            txtRoll = (TextView) findViewById(R.id.textViewRoll);
//            txtLati = (TextView) findViewById(R.id.textViewLati);
//            txtLong = (TextView) findViewById(R.id.textViewLong);
//            txtCnt = (TextView) findViewById(R.id.textViewCnt);
//            txtStraight = (TextView) findViewById(R.id.textViewStraight);
//            txtIntegral = (TextView) findViewById(R.id.textViewIntegral);
//
            txtStatus = (TextView) findViewById(R.id.textViewStatus);
//            txtSelector = (TextView) findViewById(R.id.textViewSelector);
//            txtTime = (TextView) findViewById(R.id.textViewTime);
//            txtRud = (TextView) findViewById(R.id.textViewRud);
//            txtEle = (TextView) findViewById(R.id.textViewEle);
//            txtTrim = (TextView) findViewById(R.id.textViewTrim);
//            txtAirspeed = (TextView) findViewById(R.id.textViewAirspeed);
//            txtCadence = (TextView) findViewById(R.id.textViewCadence);
//            txtUltsonic = (TextView) findViewById(R.id.textViewUltsonic);
//            txtAtmpress = (TextView) findViewById(R.id.textViewAtmpress);
//            txtAltitude = (TextView) findViewById(R.id.textViewAltitude);
//            txtCadencevolt = (TextView) findViewById(R.id.textViewCadencevolt);
//            txtUltsonicvolt = (TextView) findViewById(R.id.textViewUltsonicvolt);
//            txtServovolt = (TextView) findViewById(R.id.textViewServovolt);

        }

        public void start() {
            new Thread(this).start();
            System.out.println("aaaaaaああああ");
        }

        public void stopRunning() {
            running = false;
        }

        public void setPressureParam(double atmStandard, double atmLapse) {
            this.atmStandard = atmStandard;
            this.atmLapse = atmLapse;
        }

        @Override
        public void run() {
            System.out.println("TextSensorViewthread Start");
            try {
                Thread.sleep(100);
                System.out.println("1234567890");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            while (running) {
//                System.out.println("ikeru?");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        System.out.println("いぁ");

                        speed.setV(mReceivedDataAdapter.getAirspeed());
                        airspeed.setV(mReceivedDataAdapter.getAirspeed());
                        rpm.setV(mReceivedDataAdapter.getCadence());
                        ult.setV(mReceivedDataAdapter.getUltsonic());
                        speed.invalidate();
                        airspeed.invalidate();
                        rpm.invalidate();
                        ult.invalidate();
//                        txtYaw.setText(String.valueOf(mSensorAdapter.getYaw()));
//                        txtPitch.setText(String.valueOf(mSensorAdapter.getPitch()));
//                        txtRoll.setText(String.valueOf(mSensorAdapter.getRoll()));
//                        txtLati.setText(String.valueOf(mSensorAdapter.getLatitude()));
//                        txtLong.setText(String.valueOf(mSensorAdapter.getLongitude()));
//                        txtCnt.setText(String.valueOf(mSensorAdapter.getGpsCnt()));
//                        txtStraight.setText(String.format("%.0f", mSensorAdapter.getStraightDistance()));
//                        txtIntegral.setText(String.format("%.0f", mSensorAdapter.getIntegralDistance()));
//
//                        txtTime.setText(String.valueOf(mReceivedDataAdapter.getTime())); //mbed時間
                        elevator.setText("Elevator: " + String.format("%.2f", mReceivedDataAdapter.getElevator()));//水平サーボの舵角
                        rudder.setText("Rudder: " +String.format("%.2f", mReceivedDataAdapter.getRudder()));//垂直サーボの舵角
                        trim.setText("Trim: " +String.valueOf(mReceivedDataAdapter.getTrim()));//elevatorの舵角（ボタン）
//                        txtAirspeed.setTextSize(100.0f);
//                〇      txtAirspeed.setText(String.format("%.2f", mReceivedDataAdapter.getAirspeed()) + "m/s");//気速
//                        txtCadence.setTextSize(100.0f);
//                〇        txtCadence.setText(String.format("%.2f", mReceivedDataAdapter.getCadence()) + "RPM");//足元回転数
//                〇        txtUltsonic.setText(String.format("%.2f", mReceivedDataAdapter.getUltsonic()));//超音波(200cmまで)
//                        txtAtmpress.setText(String.format("%.2f", mReceivedDataAdapter.getAtmpress()));//気圧(hPa)
                        double altitude = -(mReceivedDataAdapter.getAtmpress() - atmStandard) / atmLapse;
//                        txtAltitude.setText(String.format("%.2f", altitude));
                        switch (mReceivedDataAdapter.getState()) {
                            case BluetoothChatService.STATE_CONNECTED:
                                txtStatus.setText("Connected");
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                txtStatus.setText("Connecting...");
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                                txtStatus.setText("Listen");
                                break;
                            case BluetoothChatService.STATE_NONE:
                                txtStatus.setText("None");
                                break;
                        }
//                        txtSelector.setText(String.valueOf(mReceivedDataAdapter.getSelector()));
//                        txtCadencevolt.setText(String.valueOf(mReceivedDataAdapter.getCadencevolt()));
//                        txtUltsonicvolt.setText(String.valueOf(mReceivedDataAdapter.getUltsonicvolt()));
//                        txtServovolt.setText(String.valueOf(mReceivedDataAdapter.getServovolt()));


                        //sound.set(mSensorAdapter.getRoll(), 40, 60);
                    }
                });
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e(TAG, "ReConnectThread exception");
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    private class CloudLoggerSendThread extends Thread{
        CloudLoggerService mCloudLoggerService;
        Handler handler = new Handler();
        private boolean running = true;
        public CloudLoggerSendThread(CloudLoggerService mCloudLoggerService){
            this.mCloudLoggerService = mCloudLoggerService;
        }
        public void start(){
            new Thread(this).start();
        }
        public void stopRunning() {
            running = false;
        }
        @Override
        public void run(){
            while(running) {
                while (i != 0) {
                    // TODO: EXEPTION !!!

                    try {
                        mCloudLoggerService.send();//モバイル通信できないときはコメントアウト必須
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "CloudLoggerSendThread exception");
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "CloudLoggerSendThread exception");
                }
            }
        }

    }

    private float rad2deg( float rad ) {
        return rad * (float) 180.0 / (float) Math.PI;
    }

    protected void initSensor(){
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onDestroy() {
        // TODO: Destroy時にアプリが落ちないようにする
        super.onDestroy();
        System.out.println("スレッド終了");
        mTextSensorViewThread.stopRunning();
        mCloudLoggerSendThread.stopRunning();
        //mCloudLoggerAdapter.stoplogger();
        sound.release();
        mReceivedDataAdapter.stop();
        mSensorAdapter.stopSensor();
        mCloudLoggerService.close();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void startChronometer() {
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mStartTimeMillis = System.currentTimeMillis();
    }

    private void stopChronometer() {
        mChronometer.stop();
        //ミリ秒
        mElapsedTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 長押しのリスナーをセット
        // TODO: マーカーが表示される前に長押しして落ちないようにする
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng longpushLocation) {
                try {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latlng).zoom(15).bearing(0).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } catch (Exception e) {
                    // do nothing
                }
            }
        });

        // MyLocationレイヤーを有効に
        mMap.setMyLocationEnabled(true);
        // MyLocationButtonを有効に
        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);
//        UiSettings settings = mMap.getUiSettings();
//
//        settings.setCompassEnabled(true);
//        //ズームイン・アウトボタンの有効化
//        settings.setZoomControlsEnabled(true);
//        //回転ジェスチャーの有効化
//        settings.setRotateGesturesEnabled(true);
//        //スクロールジェスチャーの有効化
//        settings.setScrollGesturesEnabled(true);
//        //Tlitジェスチャーの有効化
//        settings.setTiltGesturesEnabled(true);
//        //ズームジェスチャーの有効化
//        settings.setZoomGesturesEnabled(true);
//
//        //マップの種類
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // DangerousなPermissionはリクエストして許可をもらわないと使えない(Android6以降？)
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permisson.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//                // 一度拒否された時のダイアログ
//                new AlertDialog.Builder(this)
//                        .setTitle("許可が必要です")
//                        .setMessage("許可して")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                showToastShort("GPS使えへんがな");
//                            }
//                        })
//                        .show();
//            } else {
//                // まだ許可を求める前の時、許可を求めるダイアログを表示する
//                //requestAccessFineLocation();
//            }
//        }
    }
//    private void requestAccessFineLocation() {
//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // ユーザーが許可したとき
                // 許可が必要な機能を改めて実行する
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //
                } else {
                    // ユーザーが許可しなかったとき
                    // 許可されなかったため機能が実行できないことを表示する
                    //showToastShort("GPS機能が使えないので地図は動きません");
                    // 以下は、java.lang.RuntimeExceptionになる
                    // mMap.setMyLocationEnabled(true)
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STRAGE: {
                // userが許可
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // saveConfirmDialog();
                } else {
                    // userが許可しない
                    //showToastShort("外部へのファイルの保存が許可されなかったので、きろくできません");
                }
                return;
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mFusedLocationProviderApi.requestLocationUpdates(mGoogeleApiClient, REQUEST, (LocationListener) this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        // do nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // do nothing
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView GPS = (TextView) findViewById(R.id.GPS);
        // Stop後は動かさない
        if (mStop) {
            return;
        }


        // マーカー設定
        mMap.clear();
        latlng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions();
        options.position(latlng);
        // ランチャーアイコン
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.navi);
        options.icon(icon);
        mMap.addMarker(options
                .anchor(0.5f, 0.5f)
                .rotation(rad2deg( fAttitude[0] ) + 90));

        if (latlng.latitude == 0 || latlng.longitude == 0) {
            GPS.setText("NO GPS!!");
        } else {
            GPS.setText("goodGPS");
        }

        if (mStart) {
            if (mFirst) {
                CameraPosition cameraposition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
                        .bearing(0).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraposition));
                Bundle args = new Bundle();
                args.putDouble("lat", location.getLatitude());
                args.putDouble("lon", location.getLongitude());
                System.out.println("debug");
                System.out.println(location.getLatitude());

                //getLoaderManager().restartLoader(ADDRESSLOADER_ID, args, this);
                mFirst = !mFirst;
            } else {
                //移動線を描画
                drawTrace(latlng);
                //走行距離を累積
                sumDistance();
            }
        }
    }

    private void drawTrace(LatLng latlng) {
        mRunList.add(latlng);
        if (mRunList.size() > 1) {
            PolylineOptions polyOptions = new PolylineOptions();
            for (LatLng polyLatLng : mRunList) {
                polyOptions.add(polyLatLng);
            }
            polyOptions.color(Color.BLUE);
            polyOptions.width(3.5f);
            polyOptions.geodesic(false);
            mMap.addPolyline(polyOptions);
        }
    }

    private void sumDistance() {
        if (mRunList.size() < 2) {
            return;
        }
        // 累計距離
        mMeter = 0.0;
        float[] results = new float[3];
        // Straight distance
        StraightMeter = 0.0;
        float[] straight = new float[3];
        int i = 1;
        while (i < mRunList.size()) {
            results[0] = 0;
            Location.distanceBetween(mRunList.get(i - 1).latitude, mRunList.get(i - 1).longitude,
                    mRunList.get(i).latitude, mRunList.get(i).longitude, results);
            Location.distanceBetween(mRunList.get(0).latitude, mRunList.get(0).longitude, mRunList.get(i).latitude, mRunList.get(i).longitude, straight);
            mMeter += results[0];
            StraightMeter = straight[0];
            //
            i++;
        }
        //distanceBetweenの距離はメートル単位
        //double disMeter = mMeter / 1000;
        TextView DisText = (TextView) findViewById(R.id.textview);
        TextView StraightText = (TextView) findViewById(R.id.textview1);
        DisText.setTextColor(Color.RED);
        StraightText.setTextColor(Color.RED);
        DisText.setText(String.format("Distance: " + "%.1f" + " m", mMeter));
        StraightText.setText(String.format("Straight: " + "%.1f" + " m", StraightMeter));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagnetic = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                gravity = event.values.clone();
                break;
        }
        if(geomagnetic != null && gravity != null){
            SensorManager.getRotationMatrix( rotationMatrix, null, gravity, geomagnetic);
            SensorManager.getOrientation( rotationMatrix, attitude);
//            YawText.setText(Integer.toString( (int)(attitude[0] * RAD2DEG)));
//            PitchText.setText(Integer.toString( (int)(attitude[1] * RAD2DEG)));
//            RollText.setText(Integer.toString( (int)(attitude[2] * RAD2DEG)));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // do nothing
    }

//    public class SubThreadSample extends Thread {
//        private String myName;    //名前
//        private long mySpan;      //周期
//        private int myloopCount;  //ループ回数
//        //GraphView graphView; //ビュー
//        //float v = 0;
//
//        final Handler handler = new Handler();
//        GraphView graphView = (GraphView) findViewById(R.id.view);
//        GraphView graphView1 = (GraphView) findViewById(R.id.view1);
//        //        GraphView graphView2 = (GraphView) findViewById(R.id.view2);
//        GraphView graphView3 = (GraphView) findViewById(R.id.view3);
//        GraphView graphView4 = (GraphView) findViewById(R.id.view4);
//
//        //コンストラクタ
//        public SubThreadSample(String name, long span, int loopCount) {
//            myName = name;
//            mySpan = span;
//            myloopCount = loopCount;
//            runflg = true;
//            graphView.v = 0;
//            graphView1.v = 0;
////            graphView2.v = 0;
//            graphView3.v = 0;
//            graphView4.v = 0;
//            //graphView = graphView1;
//            //graphView.x0 = x0;
//            //graphView.y0 = y0;
//            //graphView.vmin = vmin;
//            //graphView.vmax = vmax;
//            //graphView.unit = unit;
//            //graphView.title = title;
//            //graphView.invalidate();
//        }
//
//        //SubThreadSample.java
//        public void run() {
//            while (runflg) {
//
//                try {
//                    Thread.sleep(100);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                //int v = 0;
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//
//                        graphView.v++;
//                        graphView.invalidate();
//                        //System.out.println("debug" + graphView.v);
//
//                        graphView1.v += 5;
//                        graphView1.invalidate();
//                        //System.out.println("debug" + graphView1.v);
//
////                        graphView2.v += 3;
////                        graphView2.invalidate();
//                        //System.out.println("debug" + graphView2.v);
//
//                        graphView3.v += 4;
//                        graphView3.invalidate();
//                        //System.out.println("debug" + graphView3.v);
//
//                        graphView4.v += 10;
//                        graphView4.invalidate();
//                        //System.out.println("debug" + graphView4.v);
//
//                    }
//                });
//
//                try {
//                    Thread.sleep(100);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (runflg == false) {
//                    break;
//                }
//            }
//
//            runflg = false;
//        }
//
//
//        public void stopRunning() {
//            runflg = false;
//        }
//    /*public void setValue(float v){
//        this.v = v;
//    }*/
//    }
//    private void showToastShort(String textToShow) {
//        mLongToast.setText(textToShow);
//        mLongToast.show();
//    }
}