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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import static javax.net.ssl.SSLContext.getInstance;

/*
* 单例类实现所有Activity可使用的对象和方法
* */
public class SuperApplication extends Application {

    //资源打开和关闭在不同的方法中，需要资源共享
    SSLSocket sslSocket = null;
    OutputStream outputStream = null;
    InputStream inputStream = null;
    InputStreamReader inputStreamReader = null;

    PrintWriter printWriter = null;

    //提供全局socket数据流写入方法，步骤：1.println()2.flush()
    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    BufferedReader bufferedReader = null;

    //提供全局socket数据流读取方法，步骤：readLine()
    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    //按下登陆按钮后，打开相关socket资源准备为各线程提供数据流支持
    boolean SocketConnect() {
        try {
            //根据ip_config配置进行socket连接
            SharedPreferences sharedPreferences = getSharedPreferences("ip_config", MODE_PRIVATE);

            //实例化SSL上下文对象，使用TLS协议(SSLv1-v3已经不安全被弃用，现阶段使用TLSv1.0-v1.2，API 19及以下不支持TLSv1.1和v1.2)
            SSLContext sslContext = getInstance("TLS");
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            //设置服务器信任的秘钥库管理器，X509标准
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
//            //装载服务器秘钥库
//            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            keyStore.load(getResources().openRawResource(R.raw.client), "client".toCharArray());
            //装载服务器信任的秘钥库
            KeyStore trustKeyStore = KeyStore.getInstance("BKS");
            trustKeyStore.load(getResources().openRawResource(R.raw.server_trust), "server".toCharArray());
//            keyManagerFactory.init(keyStore, "server".toCharArray());
            //初始化服务端信任的秘钥库管理器
            trustManagerFactory.init(trustKeyStore);
            //初始化SSL上下文对象，密钥库设置为空（双向验证时使用），指定安全的随机数作为加密数
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            //由SSLContext对象获取Socket工厂并实例化SSLSocket
            sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket();
            //连接Socket
            sslSocket.connect(new InetSocketAddress(sharedPreferences.getString("address", ""), sharedPreferences.getInt("port", 0)), 1000);
            //连接成功后设置相关IO流及缓冲区
            if (sslSocket.isConnected()) {
                outputStream = sslSocket.getOutputStream();
                printWriter = new PrintWriter(outputStream);
                inputStream = sslSocket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                return true;
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
        }
        return false;
    }

    //应用退出时，提供一个关闭socket资源的方法
    void SocketClose() {
        if (sslSocket.isConnected()) {
            try {
                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
                printWriter.close();
                outputStream.close();
                sslSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
