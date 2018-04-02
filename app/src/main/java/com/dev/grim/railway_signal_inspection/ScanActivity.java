package com.dev.grim.railway_signal_inspection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class ScanActivity extends AppCompatActivity {
    long ClickTime = System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        TextView deviceSimpleIfo = findViewById(R.id.device_simple_info_text);
        Button buttonInfo = findViewById(R.id.button_info);
        buttonInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ScanActivity.this,InfoActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        //双击back键退出
        if (System.currentTimeMillis() - ClickTime > 800) {
            Toast.makeText(ScanActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
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
