package com.dev.grim.railway_signal_inspection;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


public class SuperSocket extends Application {
    //资源打开和关闭在不同的方法中，需要资源共享
    Socket socket = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    InputStreamReader inputStreamReader = null;

    PrintWriter printWriter = null;
    //提供全局socket数据流写入方法，步骤：1.println()2.flush()
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    BufferedReader bufferedReader = null;
    //提供全局socket数据流读取方法，步骤：realine()
    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    //按下登陆按钮后，打开相关socket资源准备为各线程提供数据流支持
    boolean MySocketConnect() {
        try {
            //根据ip_config键值进行socket连接
            SharedPreferences sharedPreferences = getSharedPreferences("ip_config",MODE_PRIVATE);
            socket = new Socket();
            socket.connect(new InetSocketAddress(sharedPreferences.getString("address",""),sharedPreferences.getInt("port",0)),1000);
            if (socket.isConnected()) {
                outputStream = socket.getOutputStream();
                printWriter = new PrintWriter(outputStream);
                inputStream = socket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //应用退出时，提供一个关闭socket资源的方法
    void MySocketClose() {
        try {
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            printWriter.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //网络连接状态检查
    boolean InternetStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable();
        }
        return false;
    }
}
