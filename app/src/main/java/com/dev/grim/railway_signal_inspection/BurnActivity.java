package com.dev.grim.railway_signal_inspection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BurnActivity extends AppCompatActivity {
    long ClickTime = System.currentTimeMillis();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burn);
        TextView deviceID = findViewById(R.id.device_id);
        TextView deviceName = findViewById(R.id.device_name);
        TextView deviceType = findViewById(R.id.device_type);
        TextView deviceLocation = findViewById(R.id.device_location);
        Button buttonBurn = findViewById(R.id.button_burn);
        buttonBurn.setOnClickListener(v -> {

        });
    }
    @Override
    public void onBackPressed() {
        //双击back键退出
        if (System.currentTimeMillis() - ClickTime > 800) {
            Toast.makeText(BurnActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
            ClickTime = System.currentTimeMillis();
        } else {
            new Thread(){
                @Override
                public void run() {
                    ((SuperSocket) getApplication()).MySocketClose();
                }
            };
            finish();
        }
    }
}
