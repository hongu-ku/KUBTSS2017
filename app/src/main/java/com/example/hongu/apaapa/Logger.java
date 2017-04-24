package com.example.hongu.apaapa;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger {
    private static final String TAG = Logger.class.getSimpleName();

//    private static final String EXTERNAL_STORAGE_PATH = "/Removable/MicroSD";//MemoPad8の外部ストレージパス
//    private static final String EXTERNAL_STORAGE_PATH = "/storage/emulated/0";//Nexus7の内部ストレージパス
//    private static final String EXTERNAL_STORAGE_PATH = "/storage/sdcard1";//Xperia Z1の場合の外部ストレージパスを指定。
/**
 * MainActivityのonCreate内で，次のコードを必ず加えること
 *
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
*/

    private static String EXTERNAL_STORAGE_PATH = null;
    private static final String PACKAGE_NAME = MapsActivity.class.getPackage().getName();//このパッケージ内でないと読み書きできない

    private File file;
    private File dirDataType;


    public static void setExternalDir(File file){
        EXTERNAL_STORAGE_PATH = file.getAbsolutePath().substring(0,file.getAbsolutePath().indexOf("/Android"));
        Log.d(TAG, EXTERNAL_STORAGE_PATH);
    }


    public Logger(String dataType){
        File dirPackage = new File(EXTERNAL_STORAGE_PATH + "/Android/data/" + PACKAGE_NAME);

        File dirDate = new File(dirPackage, getNowDate());
        if (!dirDate.exists()) dirDate.mkdirs();

        dirDataType = new File(dirDate, dataType);
        if (!dirDataType.exists()) dirDataType.mkdirs();

        makeNewFile();
    }

    public Logger(String dataType, String dataTitle){
            File dirPackage = new File(EXTERNAL_STORAGE_PATH + "/Android/data/" + PACKAGE_NAME);

            File dirDate = new File(dirPackage, getNowDate());
            if (!dirDate.exists()) dirDate.mkdirs();

            dirDataType = new File(dirDate, dataType);
            if (!dirDataType.exists()) dirDataType.mkdirs();

            makeNewFile();

            appendData(dataTitle);
    }



    public void makeNewFile(){
        file = new File(dirDataType, getNowTime() + ".csv");
    }

    public void appendData(String data) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (file.canWrite()) {
                FileWriter fw = new FileWriter(file, true);//第二引数trueで追加書き込み，falseで上書き
                fw.append(String.valueOf(data) + "\n");
                fw.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyyMMdd");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    private static String getNowTime(){
        final DateFormat df = new SimpleDateFormat("HHmmss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }


}
