package com.dev.grim.railwaysignalinspect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private String usernameText;
    private String passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView username = findViewById(R.id.username_text);
        final TextView password = findViewById(R.id.password_text);
        //绑定登陆按钮，并监听按钮的点击事件触发登陆
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //实例化SocketClient对象并调用sendMessage()方法
                SocketClient socketClient = new SocketClient();
                socketClient.sendMessage();
                //获取登陆用户名密码
                usernameText = username.getText().toString();
                passwordText = password.getText().toString();

            }
        });
    }

    public String getUsernameText() {
        return usernameText;
    }

    public String getPasswordText() {
        return passwordText;
    }

    class SocketClient {

        public void sendMessage() {
            //启动Runnable新线程处理网络连接
            SocketClientThread socketClientThread = new SocketClientThread();
            Thread thread = new Thread(socketClientThread);
            thread.start();
        }

        //按照Android开发规范，子线程处理网络连接
        class SocketClientThread implements Runnable{
            @Override
            public void run() {
                try (Socket socket = new Socket("192.168.0.133",60001);
                     OutputStream outputStream = socket.getOutputStream();
                     PrintWriter printWriter = new PrintWriter(outputStream)){
                    Thread.sleep(1000);
                    printWriter.println("ABC"+getUsernameText()+getPasswordText());
                    printWriter.flush();
                    //调用Activity的runOnUiThread()方法匿名内部类实现非主线程更新UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,"login...",Toast.LENGTH_SHORT).show();
                        }
                    });
                    Thread.sleep(3000);
                    socket.shutdownOutput();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
