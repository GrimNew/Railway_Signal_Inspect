package com.dev.grim.railway_signal_inspection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //绑定setting布局文件
        setContentView(R.layout.activity_setting);
        final TextView address = findViewById(R.id.address_text);
        final TextView port = findViewById(R.id.port_text);
        Button setting_commit = findViewById(R.id.button_setting_commit);
        Button setting_cancel = findViewById(R.id.button_setting_cancle);
        android.support.v7.widget.Toolbar setting_toolbar = findViewById(R.id.setting_toolbar);

        //设置标题文字颜色
        setting_toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(setting_toolbar);

        //显示设置默认值
        SharedPreferences sharedPreferences = getSharedPreferences("ip_config", MODE_PRIVATE);
        address.setText(sharedPreferences.getString("address", "192.168.1.101"));
        port.setText(String.valueOf(sharedPreferences.getInt("port", 60001)));

        Intent intent = new Intent(SettingActivity.this, MainActivity.class);

        //确认按钮点击事件，设置参数保存在ip_config，并跳转回登录页
        setting_commit.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("address", address.getText().toString());
            editor.putInt("port", Integer.valueOf(port.getText().toString()));
            editor.apply();
            Toast.makeText(SettingActivity.this, "设置成功！", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });
        //取消按钮点击事件，跳转到登录页
        setting_cancel.setOnClickListener(v -> startActivity(intent));
    }
}
