package com.dev.grim.railway_signal_inspection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //共享资源
    private String usernameText;
    private String passwordText;
    long ClickTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定Layout布局
        setContentView(R.layout.activity_main);

        //绑定toolbar对象
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        //使Toolbar具有ActionBar的监听功能
        setSupportActionBar(toolbar);
        //设置Toolbar的Title字体颜色
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);

        final EditText username = findViewById(R.id.username_text);
        final EditText password = findViewById(R.id.password_text);

        CheckBox checkBox = findViewById(R.id.save_check);

        //判断曾经是否保存过账号密码，若保存过，则自动填充TextView并勾选CheckBox
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("saved", false)) {
            username.setText(sharedPreferences.getString("username", ""));
            password.setText(sharedPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }

        //绑定登陆按钮，并监听按钮的点击事件触发登陆
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> {
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
            //检查NFC硬件支持
            if(nfcAdapter == null){
                Toast.makeText(MainActivity.this, "该设备不支持NFC", Toast.LENGTH_SHORT).show();
            }else if(!nfcAdapter.isEnabled()){
                Toast.makeText(MainActivity.this, "请打开NFC开关", Toast.LENGTH_SHORT).show();
            }else{
                //检查网络状态，网络未连接则Toast提示
                if (((SuperApplication) getApplication()).InternetStatus(MainActivity.this)) {
                    //获取登陆用户名密码
                    usernameText = username.getText().toString();
                    passwordText = password.getText().toString();

                    if (usernameText.equals("") || passwordText.equals("")) {
                        Toast.makeText(MainActivity.this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        //启动线程执行登陆
                        new Thread(new Login()).start();
                        //CheckBox勾选则更新账户名密码，未勾选则放弃保存，则下次不再自动填充TextView和勾选CheckBox
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (checkBox.isChecked()) {
                            editor.putString("username", usernameText);
                            editor.putString("password", passwordText);
                            editor.putBoolean("saved", true);
                            editor.apply();
                        } else {
                            editor.putBoolean("saved", false);
                            editor.apply();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override//创建菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        //与menu布局文件绑定
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override//监听menu的select事件
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting://跳转到setting页面
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_logout://退出应用
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getUsernameText() {
        return usernameText;
    }

    public String getPasswordText() {
        return passwordText;
    }

    //按照Android开发规范，子线程处理网络连接
    class Login implements Runnable {
        @Override
        public void run() {
            try {
                //连接Socket，发送账号密码校验
                if (((SuperApplication) getApplication()).SocketConnect()) {
                    ((SuperApplication) getApplication()).getPrintWriter().println("0#" + getUsernameText() + "#" + getPasswordText());
                    ((SuperApplication) getApplication()).getPrintWriter().flush();
                    //根据登陆用户权限不同，跳转到不同页面进行不同操作
                    switch ((((SuperApplication) getApplication()).getBufferedReader().readLine())) {
                        //跳转到烧录界面（管理员模式）
                        case "00": {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "登陆成功", Toast.LENGTH_SHORT).show());
                            Intent intent = new Intent(MainActivity.this, SelectActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        }
                        //跳转到扫描界面（用户模式）
                        case "01": {
                            //调用Activity的runOnUiThread()方法匿名内部类实现非主线程更新UI
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "登陆成功", Toast.LENGTH_SHORT).show());
                            Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        }
                        //登陆失败，不进行页面跳转
                        default:
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "账号或密码错误，请重新登陆", Toast.LENGTH_SHORT).show());
                            ((SuperApplication) getApplication()).SocketClose();
                            break;
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "连接超时，请检查网络环境", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //双击back键退出
        if (System.currentTimeMillis() - ClickTime > 800) {
            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            ClickTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
