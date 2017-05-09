package com.example.hongu.apaapa;


import android.content.res.Configuration;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Date;
import java.util.List;

public class SensorAdapter implements SensorEventListener, LocationListener {

    private static final String TAG = SensorAdapter.class.getSimpleName();

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private ReceivedDataAdapter mReceivedDataAdapter;

    private static final int MATRIX_SIZE = 9;
    /* 回転行列 */
    private float[]  inR = new float[MATRIX_SIZE];
    private float[] outR = new float[MATRIX_SIZE];
    private float[]    I = new float[MATRIX_SIZE];
    private float[] ininR = new float[MATRIX_SIZE];
    /* センサーの値 */
    private float[] orientationValues   = new float[3];
    private float[] magneticValues      = new float[3];
    private float[] accelerometerValues = new float[3];
    private float[] gyroscopeValues     = new float[3];
    /* betweenDistanceの戻り値 */
    float[] results = new float[3];

    private double latitude, longitude, bearing, speed, altitude, declination,
            prevLatitude, prevLongitude, integralDistance, platformLatitude, platformLongitude;
    private int gpsCnt, saveCount, testCnt;

    private Logger postureLogger, gpsLogger, allLogger;

    Configuration config;


    //アダプタのプロパティ
    public int getYaw(){
        int yaw = (int)(radianToDegree(orientationValues[0]) - this.declination);
        if(yaw >= 0){
            return yaw;
        }else{
            return 360 + yaw;
        }
    }
    public int getPitch() {return radianToDegree(orientationValues[1]);}
    public int getRoll()  {return radianToDegree(orientationValues[2]);}
    public float getGyroX() {return gyroscopeValues[0];}
    public float getGyroY() {return gyroscopeValues[1];}
    public float getGyroZ() {return gyroscopeValues[2];}
    public float getAccX()  {return accelerometerValues[0];}
    public float getAccY()  {return accelerometerValues[1];}
    public float getAccZ()  {return accelerometerValues[2];}
    public float getMagX()  {return magneticValues[0];}
    public float getMagY()  {return magneticValues[1];}
    public float getMagZ()  {return magneticValues[2];}

    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}
    public double getBearing(){return bearing;}
    public double getSpeed(){return speed;}
    public double getAltitude(){return altitude;}

    float deg;
    double rad;


    float sin;
    float cos;

    float[] rot = new float[9];

    public void setPitchneutral(float pitchneu) {
        Pitchneutral = pitchneu;
    }

    private float Pitchneutral = 0;

    public int getGpsCnt(){return gpsCnt;}
    public int getTestCnt(){
        testCnt++;
        return testCnt;
    }

    public double getStraightDistance(){
        if(this.latitude == 0){
            return -1;
        }
        Location.distanceBetween(platformLatitude, platformLongitude, this.latitude, this.longitude, results);
        return (double)results[0];
    }
    public double getIntegralDistance(){return integralDistance;}
    public void setPlatformPoint(double latitude, double longitude){
        this.platformLatitude = latitude;
        this.platformLongitude = longitude;
    }
    public void resetIntegralDistance(){
        this.integralDistance = 0;
    }


    public SensorAdapter(SensorManager mSensorManager, LocationManager mLocationManager, ReceivedDataAdapter mReceivedDataAdapter, Configuration config) {

        postureLogger = new Logger("Posture","androidTime,yaw,pitch,roll");
        gpsLogger     = new Logger("GPS","androidTime,latitude,longitude,bearing,speed,altitude,straightDist,integralDist");

        allLogger     = new Logger("ALL","androidTime,latitude,longitude,bearing,speed,altitude," +
                                    "elevator,rudder,trim,airspeed,cadence,ultrasonic,atmpressure");

        this.mSensorManager = mSensorManager;
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            if(sensor.getType() == Sensor.TYPE_ACCELEROMETER
                    || sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                    || sensor.getType() == Sensor.TYPE_GYROSCOPE)
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        }

        this.mLocationManager = mLocationManager;
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }

        this.mReceivedDataAdapter = mReceivedDataAdapter;
        this.config = config;

        resetIntegralDistance();
        setPlatformPoint(35.027578, 135.783206);//京都大学工学部電気総合館
    }

    public void stopSensor() {
        // センサー停止時のリスナ解除
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //この中はなるべく簡潔に．メモリ確保は外でやったほうがいい．
//        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroscopeValues = event.values.clone();
                break;
        }
        if (magneticValues != null && accelerometerValues != null) {

            rad = Pitchneutral;


            sin = (float) Math.sin(rad);
            cos = (float) Math.cos(rad);
            //System.out.println("rad="+sin);


            rot[0] = 1;
            rot[1] = 0;
            rot[2] = 0;
            rot[3] = 0;
            rot[4] = cos;
            rot[5] = -sin;
            rot[6] = 0;
            rot[7] = sin;
            rot[8] = cos;

            SensorManager.getRotationMatrix(inR, null, accelerometerValues, magneticValues);

            //remapCoordinateSystem(inR, deviceX, deviceY, outR); deviceXにworldX，deviceYにworldYを入れる
            /*
            if(config == null){
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, outR);//デフォルトは横表示
            }else if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, outR);//横表示
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);//縦表示
            }*/

            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);//前窓取付縦表示
            //SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_Z, outR);//右側面取付縦表示
            //SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_Y, SensorManager.AXIS_Z, outR);//左側面取付縦表示
            //SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Y, outR);//上ボート取付縦表示
            //SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);//下ボート取付縦表示



            ininR[0] = outR[0]*rot[0] + outR[1]*rot[3] + outR[2]*rot[6];
            ininR[1] = outR[0]*rot[1] + outR[1]*rot[4] + outR[2]*rot[7];
            ininR[2] = outR[0]*rot[2] + outR[1]*rot[5] + outR[2]*rot[8];
            ininR[3] = outR[3]*rot[0] + outR[4]*rot[3] + outR[5]*rot[6];
            ininR[4] = outR[3]*rot[1] + outR[4]*rot[4] + outR[5]*rot[7];
            ininR[5] = outR[3]*rot[2] + outR[4]*rot[5] + outR[5]*rot[8];
            ininR[6] = outR[6]*rot[0] + outR[7]*rot[3] + outR[8]*rot[6];
            ininR[7] = outR[6]*rot[1] + outR[7]*rot[4] + outR[8]*rot[7];
            ininR[8] = outR[6]*rot[2] + outR[7]*rot[5] + outR[8]*rot[8];

//            for(int i=0; i<9; i++) {
//                System.out.println("outR["+i+"]: " + outR[i]);
//                System.out.println("ininR["+i+"]: " + ininR[i]);
//            }




            SensorManager.getOrientation(ininR, orientationValues);

            if(saveCount == 4) {
                postureLogger.appendData(
                        System.currentTimeMillis() + "," +
                                String.valueOf(getYaw()) + "," +
                                String.valueOf(getPitch()) + "," +
                                String.valueOf(getRoll()));
                saveCount = 0;
            }
            saveCount++;
        }
    }

    int radianToDegree(float rad){
        return (int) Math.toDegrees(rad);
    }



    @Override
    public void onLocationChanged(Location location) {
        //この中はなるべく簡潔に．メモリ確保は外でやったほうがいい．
        this.latitude  = location.getLatitude();
        this.longitude = location.getLongitude();
        this.bearing = location.getBearing();
        this.speed = location.getSpeed();
        this.altitude = location.getAltitude();
        this.gpsCnt++;

        GeomagneticField geomagnetic = new GeomagneticField(
                (float)this.latitude, (float)this.longitude, (float)this.altitude, new Date().getTime());
        this.declination = geomagnetic.getDeclination();

        if(gpsCnt == 10){//prevの値の初期化(0のままだとgpsCnt==20のときのresults[0]が巨大な値になる)
            this.prevLatitude = this.latitude;
            this.prevLongitude = this.longitude;
        }
        Location.distanceBetween(this.prevLatitude, this.prevLongitude, this.latitude, this.longitude, results);
        if(gpsCnt > 19 && gpsCnt%10 == 0) {//GPS立ち上がりの値無視 && チリツモ防止
            this.integralDistance += results[0];
            this.prevLatitude = this.latitude;
            this.prevLongitude = this.longitude;
        }

        gpsLogger.appendData(
                        System.currentTimeMillis() + "," +
                        String.valueOf(getLatitude()) + "," +
                        String.valueOf(getLongitude()) + "," +
                        String.valueOf(getBearing()) + "," +
                        String.valueOf(getSpeed()) + "," +
                        String.valueOf(getAltitude()) + "," +
                        String.valueOf(getStraightDistance()) + "," +
                        String.valueOf(getIntegralDistance()));

        allLogger.appendData(
                        System.currentTimeMillis() + "," +
                        String.valueOf(getLatitude()) + "," +
                        String.valueOf(getLongitude()) + "," +
                        String.valueOf(getBearing()) + "," +
                        String.valueOf(getSpeed()) + "," +
                        String.valueOf(getAltitude()) + "," +
                        String.valueOf(mReceivedDataAdapter.getElevator()) + "," +
                        String.valueOf(mReceivedDataAdapter.getRudder()) + "," +
                        String.valueOf(mReceivedDataAdapter.getTrim()) + "," +
                        String.valueOf(mReceivedDataAdapter.getAirspeed()) + "," +
                        String.valueOf(mReceivedDataAdapter.getCadence()) + "," +
                        String.valueOf(mReceivedDataAdapter.getUltsonic()) + "," +
                        String.valueOf(mReceivedDataAdapter.getAtmpress()) );
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

}