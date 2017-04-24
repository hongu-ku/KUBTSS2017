package com.example.hongu.apaapa;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class ReceivedDataAdapter {

    private static final String TAG = ReceivedDataAdapter.class.getSimpleName();

    private Context context;
    private BluetoothChatService mChatService = null;
    private StringBuilder recDataString = new StringBuilder(256);
    private String mConnectedDeviceName = null;
    private BluetoothDevice device;
    private ReConnectThread mReConnectThread;

   // final private static String ADDRESS = "00:06:66:6C:56:9E";// Bluetoothモジュールの MAC address
    final private static String ADDRESS = "00:06:66:80:C5:42";// Bluetoothモジュールの MAC address

    boolean connectButton = true;

    private Logger controllerLogger, sensor500Logger;

    private double elevator, rudder, airspeed, cadence, ultsonic, atmpress;
    private double cadencevolt,ultsonicvolt,servovolt;
    private int time, trim, Selector;

    private String[] dataString;
    private String timeString;


    public ReceivedDataAdapter(Context context){

        controllerLogger = new Logger("Controller","androidTime,mbedTime,elevator,rudder,trim");
        sensor500Logger  = new Logger("Sensor500ms","androidTime,mbedTime,airspeed,cadence,ultrasonic,atmpressure,cadencevolt,ultsonicvolt");


        this.context = context;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        device = btAdapter.getRemoteDevice(ADDRESS);


        if (!btAdapter.isEnabled()) {
            Toast.makeText(context, "Switch Bluetooth on!", Toast.LENGTH_LONG).show();
        } else if (mChatService == null) {
            mChatService = new BluetoothChatService(context, bluetoothIn);
        }


        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }

        mReConnectThread = new ReConnectThread();
        mReConnectThread.start();
    }


    private final Handler bluetoothIn = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            break;
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                case Constants.MESSAGE_READ:
                    recDataString.append(msg.obj);
                    if(recDataString.indexOf("#") > 0){
                        recDataString.delete(0, recDataString.indexOf("#"));
                    }
                    if(recDataString.indexOf("#") == 0 && recDataString.indexOf("\r") > 0){
                        dataString = recDataString.substring(0, recDataString.indexOf("\r") + 1).toString().split(",");
                        if(dataString.length > 10) {

                            timeString = dataString[0].substring(1);
                            try {
                                time = Integer.parseInt(timeString);
                                if (!dataString[1].equals("N")) {
                            //        controllerLogger.appendData(System.currentTimeMillis() + "," + timeString + "," + dataString[1] + "," + dataString[3] + "," + dataString[4]);
                                //    Selector = Integer.parseInt(dataString[1]);
                                    elevator = Double.parseDouble(dataString[1]);
                                    rudder = Double.parseDouble(dataString[2]);
                                    trim = Integer.parseInt(dataString[3]);
                                }
                                if (!dataString[4].equals("N")) {
                                    //      sensor500Logger.appendData(System.currentTimeMillis() + "," + timeString + "," + dataString[5] + "," + dataString[6] + "," + dataString[7] + "," + dataString[8]);
                                    airspeed = Double.parseDouble(dataString[4]);
                                    atmpress = Double.parseDouble(dataString[7]);
                                }
                                if(!dataString[5].equals("N")) {
                                    cadence = Double.parseDouble(dataString[5]);
                                }
                                if(!dataString[6].equals("N")) {
                                    ultsonic = Double.parseDouble(dataString[6]);
                                }
                                if(!dataString[8].equals("N")) {
                                    cadencevolt = Double.parseDouble(dataString[8]);
                                }
                                if(!dataString[9].equals("N")) {
                                    ultsonicvolt = Double.parseDouble(dataString[9]);
                                }
                                if(!dataString[10].equals("N")) {
                                    servovolt = Double.parseDouble(dataString[10]);
                                }
                                controllerLogger.appendData(System.currentTimeMillis() + "," + timeString + "," + dataString[1] + "," + dataString[2]);
                                sensor500Logger.appendData(System.currentTimeMillis() + "," + timeString + "," +dataString[4]+","+ dataString[5]+","+dataString[6]+","+dataString[7]+","+dataString[8]
                                        +","+dataString[9]);

                            }catch(NumberFormatException e){
                                Log.e(TAG, "NumberFormatException");
                            }
                        }
                        recDataString.delete(0, recDataString.indexOf("\r") + 1);
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(context, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(context, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            //Toast.makeText(getBaseContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "You are not connected to a device");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }

    public int    getTime(){return time;}
    public double getElevator(){return elevator;}
    public double getRudder(){return rudder;}
    public int    getTrim(){return trim;}
    public double getAirspeed(){return airspeed;}
    public double getCadence(){return cadence;}
    public double getUltsonic(){return ultsonic;}
    public double getAtmpress(){return atmpress;}
    public int    getSelector(){return Selector;}
    public double getCadencevolt(){return cadencevolt;}
    public double getUltsonicvolt(){return ultsonicvolt;}
    public double getServovolt(){return servovolt;}

    public int getState(){return mChatService.getState();}
    public void connect(){
        if(mChatService.getState() == BluetoothChatService.STATE_LISTEN) {
            mChatService.connect(device, true);
        }
    }
    public void setReconnection(boolean connectButton){
        this.connectButton = connectButton;
    }


    //接続が切れたとき（STATE_LISTENのとき）再接続を試みるスレッド
    private class ReConnectThread extends Thread {
        private boolean running = true;
        public void start(){
            new Thread(this).start();
            System.out.println("ReconnectThread start!");
        }
        @Override
        public void run(){
            System.out.println("ReconnectThread Run!");
            while(running){
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "ReConnectThread exception");
                }
                if(mChatService.getState() == BluetoothChatService.STATE_LISTEN && connectButton) {
                    mChatService.connect(device, true);
                }
            }
        }
        public void stopRunning(){
            running = false;
        }
    }


    public void stop(){
        mReConnectThread.stopRunning();

        if (mChatService != null) {
            mChatService.stop();
        }
    }
}
