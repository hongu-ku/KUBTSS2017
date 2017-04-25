package com.example.hongu.apaapa;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by takashi on 2017/03/16.
 */
public class CloudLoggerService {
    private HttpURLConnection con;
    private PrintStream printStream;
    private List<LinkedList<String>> dataList;
    private URL Url;

    public CloudLoggerService(String url) {
        dataList = new LinkedList<LinkedList<String>>();
        try {
            Url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.d("CloudLogger#CloudLogger","Create CloudLogger");
        //Set();
    }

    public void set () throws Exception {
        try {
            con = (HttpURLConnection) Url.openConnection();

            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoInput(true);
            con.setDoOutput(true);

            con.setRequestMethod("POST");

            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "text/plain");

            printStream = new PrintStream(con.getOutputStream());
            Log.d("CloudLogger#Set", "---connection was created---");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bufferedWrite(LinkedList<String> data) {
        dataList.add(data);
    }

    public void send() throws Exception {//mainActivityにてthread 管理

        while (!dataList.isEmpty()) {
            set();

            StringBuilder sb = new StringBuilder();
            sb.append("data=");

            for (String str : dataList.get(0)) {
                sb.append(str + ",");
            }
            int index = sb.lastIndexOf(",");
            sb.deleteCharAt(index); //http://yamato-java.blogspot.jp/2011/09/public-class-first-public-static-void.html

            printStream.print(sb.toString());
            printStream.close();
            try {
                if (con.getResponseMessage().equals("OK")) {
                    Log.d("CloudLogger#send","send " + dataList.get(0).get(0));
                    Log.d("CloudLogger#send","remove " + dataList.get(0).get(0));
                    dataList.remove(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            con.disconnect();
            Log.d("CloudLogger#send","---connection was disconnected---");
        }
        //printStream.flush();
        //con.disconnect();
    }

    public void close() {
        con.disconnect();
        printStream.close();
    }
}
