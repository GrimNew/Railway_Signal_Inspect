package com.dev.grim.railway_signal_inspection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;


public class SelectActivity extends AppCompatActivity {
    long ClickTime = System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        android.support.v7.widget.Toolbar setting_toolbar = findViewById(R.id.select_toolbar);
        //设置标题文字颜色
        setting_toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(setting_toolbar);
        Button select_button_read = findViewById(R.id.button_read);
        Button select_button_write = findViewById(R.id.button_write);
        select_button_read.setOnClickListener(v -> {
            Intent intent = new Intent(SelectActivity.this, ScanActivity.class);
            startActivity(intent);
        });
        select_button_write.setOnClickListener(v -> {
            Intent intent = new Intent(SelectActivity.this, BurnActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        //双击back键退出
        if (System.currentTimeMillis() - ClickTime > 800) {
            Toast.makeText(SelectActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
            ClickTime = System.currentTimeMillis();
        } else {
            new Thread(){
                @Override
                public void run() {
                    ((SuperApplication) getApplication()).SocketClose();
                }
            };
            finish();
        }
    }
}
