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
    private int count;

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

    public void setCount(int count) {
        this.count = count;
    }

    public void bufferedWrite(LinkedList<String> data) {
        dataList.add(data);
    }

    public void send() throws Exception {//mainActivityにてthread 管理

        while (!dataList.isEmpty()) {
            set();

            StringBuilder sb = new StringBuilder();

            int size = dataList.size();

            sb.append(count + ",");

            for (int i = 0; i < size; i ++) {
                for (Object str : dataList.get(i)) {
                    sb.append(str + ",");
                }
                sb.append("\n");
                sb.deleteCharAt(sb.lastIndexOf(","));
            }

             //http://yamato-java.blogspot.jp/2011/09/public-class-first-public-static-void.html

            printStream.print(sb.toString());
            printStream.flush();
            try {
                if (con.getResponseMessage().equals("OK")) {
                    for (int i = 0; i < size; i ++) {
                        Log.d("CloudLogger#send", "send " + dataList.get(0).get(0));
                        Log.d("CloudLogger#send", "remove " + dataList.get(0).get(0));
                        dataList.remove(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            close();
            Log.d("CloudLogger#send","---connection was disconnected---");
        }
    }

    public void close() {
        con.disconnect();
        System.out.println("スレッド終了");
        printStream.close();
    }
}
