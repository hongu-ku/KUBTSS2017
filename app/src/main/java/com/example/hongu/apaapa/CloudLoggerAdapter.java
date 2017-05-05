package com.example.hongu.apaapa;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by takashi on 2017/03/16.
 */
public class CloudLoggerAdapter{
    private SetValueThread mSetValueThread;
    SensorAdapter mSensorAdapter;
    ReceivedDataAdapter mReceivedDataAdapter;
    CloudLoggerService mCloudLoggerService;
    private boolean running;

    public void setCount(int count) {
        Count = count;
    }

    private int Count;

    public void setPitchneu(float pitchneu) {
        Pitchneu = pitchneu;
    }

    private float Pitchneu;

    public CloudLoggerAdapter(SensorAdapter mmSenserAdapter,ReceivedDataAdapter mmReceivedAdapter,CloudLoggerService mmCloudLoggerService){
        mSensorAdapter = mmSenserAdapter;
        mReceivedDataAdapter = mmReceivedAdapter;
        mCloudLoggerService = mmCloudLoggerService;
        mSetValueThread = new SetValueThread(mSensorAdapter, mReceivedDataAdapter,mCloudLoggerService);
        mSetValueThread.start();
    }

    private class SetValueThread extends Thread {
        private LinkedList<String> data;
        SensorAdapter mSensorAdapter;
        ReceivedDataAdapter mReceivedadapter;
        CloudLoggerService mCloudLoggerService;
        Handler handler = new Handler();



        public SetValueThread(SensorAdapter mSensorAdapter, ReceivedDataAdapter mReceivedDataAdapter,CloudLoggerService mCloudLoggerService){
            this.mSensorAdapter = mSensorAdapter;
            this.mReceivedadapter = mReceivedDataAdapter;
            this.mCloudLoggerService = mCloudLoggerService;
            Count = 0;
            running = true;
        }
        public void start(){
            new Thread(this).start();
            Log.d("TAG", "thread start");
        }
        public void stopRunning() {
            running = false;
        }
        @Override
        public void run() {
            while (running) {
                while (Count != 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            data = new LinkedList<String>();

                            mCloudLoggerService.setCount(Count);
                            data.add(String.valueOf(mReceivedDataAdapter.getTime())); //mbed時間
                            data.add(String.valueOf(mSensorAdapter.getPitch()));
                            //System.out.println("Pitch:" + mSensorAdapter.getPitch());
                            data.add(String.valueOf(mSensorAdapter.getYaw()));
                            data.add(String.valueOf(mSensorAdapter.getRoll()));
                            data.add(String.valueOf(mSensorAdapter.getLatitude()));
                            data.add(String.valueOf(mSensorAdapter.getLongitude()));
                            data.add(String.valueOf(mSensorAdapter.getGpsCnt()));
                            data.add(String.valueOf(mSensorAdapter.getStraightDistance()));
                            data.add(String.valueOf(mSensorAdapter.getIntegralDistance()));
                            data.add(String.valueOf(mReceivedDataAdapter.getElevator()));//水平サーボ
                            data.add(String.valueOf(mReceivedDataAdapter.getRudder()));//垂直サーボ
                            data.add(String.valueOf(mReceivedDataAdapter.getTrim()));//水平トリム
                            data.add(String.valueOf(mReceivedDataAdapter.getAirspeed()));//気速
                            data.add(String.valueOf(mReceivedDataAdapter.getCadence()));//RPM足元回転数
                            data.add(String.valueOf(mReceivedDataAdapter.getUltsonic()));//超音波(cm)200cmくらいまでの精度
                            data.add(String.valueOf(mReceivedDataAdapter.getAtmpress()));//hPa
                            data.add(String.valueOf(mReceivedDataAdapter.getSelector()));
                            data.add(String.valueOf(mReceivedDataAdapter.getCadencevolt()));
                            data.add(String.valueOf(mReceivedDataAdapter.getUltsonicvolt()));
                            data.add(String.valueOf(mReceivedDataAdapter.getServovolt()));

                            ///////
                            mCloudLoggerService.bufferedWrite(data);
                            Log.d("TAG", "buffer write");
                        }
                    });
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        Log.e("TAG", "SetValueThread exception");
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.e("TAG", "SetValueThread exception");
                }
            }
        }

    }
    public void stoplogger() {
        Count = 0;
        running = false;
    }

}
