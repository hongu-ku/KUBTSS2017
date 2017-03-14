package com.example.hongu.apaapa;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
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

import java.util.ArrayList;
import java.util.List;
import java.util.jar.*;
import java.util.jar.Manifest;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks , OnConnectionFailedListener,
LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogeleApiClient;
    private Toast mLongToast;
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


    SubThreadSample[] subThreadSample = new SubThreadSample[50];

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // 画面をスリープにしない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mGoogeleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        subThreadSample[0] = new SubThreadSample("a", 100, 100);

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
                    subThreadSample[i].start();
                    mStart = true;
                    mFirst = true;
                    mStop = false;
                    mMeter = 0.0;
                    mRunList.clear();
                    f++;
                    startbtn.setText("STOP");
                } else if (f == 1) {
                    stopChronometer();
                    mStop = true;
                    mStart = false;
                    subThreadSample[i].stopRunning();
                    f++;
                    i++;
                    startbtn.setText("RESET");
                } else {
                    subThreadSample[i] = new SubThreadSample("a", 10, 10);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //subThreadSample.stopRunning();
        runflg = false;
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
                    showToastShort("GPS機能が使えないので地図は動きません");
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
                    showToastShort("外部へのファイルの保存が許可されなかったので、きろくできません");
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
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15)
                .bearing(0).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // マーカー設定
        mMap.clear();
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions();
        options.position(latlng);
        // ランチャーアイコン
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
        options.icon(icon);
        mMap.addMarker(options);
        if (latlng.latitude == 0 || latlng.longitude == 0) {
            GPS.setText("NO GPS!!");
        } else {
            GPS.setText("");
        }

        if (mStart) {
            if (mFirst) {
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
            polyOptions.width(3);
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
        DisText.setText(String.format("Distance: " + "%.2f" + " m", mMeter));
        StraightText.setText(String.format("Straight: " + "%.2f" + " m", StraightMeter));
    }

    public class SubThreadSample extends Thread {
        private String myName;    //名前
        private long mySpan;      //周期
        private int myloopCount;  //ループ回数
        //GraphView graphView; //ビュー
        //float v = 0;

        final Handler handler = new Handler();
        GraphView graphView = (GraphView) findViewById(R.id.view);
        GraphView graphView1 = (GraphView) findViewById(R.id.view1);
        GraphView graphView2 = (GraphView) findViewById(R.id.view2);
        GraphView graphView3 = (GraphView) findViewById(R.id.view3);
        GraphView graphView4 = (GraphView) findViewById(R.id.view4);

        //コンストラクタ
        public SubThreadSample(String name, long span, int loopCount) {
            myName = name;
            mySpan = span;
            myloopCount = loopCount;
            runflg = true;
            graphView.v = 0;
            graphView1.v = 0;
            graphView2.v = 0;
            graphView3.v = 0;
            graphView4.v = 0;
            //graphView = graphView1;
            //graphView.x0 = x0;
            //graphView.y0 = y0;
            //graphView.vmin = vmin;
            //graphView.vmax = vmax;
            //graphView.unit = unit;
            //graphView.title = title;
            //graphView.invalidate();
        }

        //SubThreadSample.java
        public void run() {
            while (runflg) {

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //int v = 0;
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        graphView.v++;
                        graphView.invalidate();
                        //System.out.println("debug" + graphView.v);

                        graphView1.v += 5;
                        graphView1.invalidate();
                        //System.out.println("debug" + graphView1.v);

                        graphView2.v += 3;
                        graphView2.invalidate();
                        //System.out.println("debug" + graphView2.v);

                        graphView3.v += 4;
                        graphView3.invalidate();
                        //System.out.println("debug" + graphView3.v);

                        graphView4.v += 10;
                        graphView4.invalidate();
                        //System.out.println("debug" + graphView4.v);

                    }
                });

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (runflg == false) {
                    break;
                }
            }

            runflg = false;
        }


        public void stopRunning() {
            runflg = false;
        }
    /*public void setValue(float v){
        this.v = v;
    }*/
    }
    private void showToastShort(String textToShow) {
        mLongToast.setText(textToShow);
        mLongToast.show();
    }
}