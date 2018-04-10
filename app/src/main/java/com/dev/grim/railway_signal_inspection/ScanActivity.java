package com.dev.grim.railway_signal_inspection;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class ScanActivity extends AppCompatActivity {
    long ClickTime = System.currentTimeMillis();
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    String text;
    String[] splitText;
    TextView deviceSimpleInfo;
    boolean scanStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        android.support.v7.widget.Toolbar setting_toolbar = findViewById(R.id.scan_toolbar);
        //设置标题文字颜色
        setting_toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(setting_toolbar);
        nfcAdapter = NfcAdapter.getDefaultAdapter(ScanActivity.this);
        pendingIntent = PendingIntent.getActivity(ScanActivity.this,0, new  Intent(ScanActivity.this,ScanActivity.class),0);
        deviceSimpleInfo = findViewById(R.id.device_simple_info_text);
        TextView device_status = findViewById(R.id.device_status);
        Button buttonInfo = findViewById(R.id.button_info);
        buttonInfo.setOnClickListener(v -> {
            if (scanStatus) {
                if (device_status.getText().toString().equals("")) {
                    Toast.makeText(ScanActivity.this, "状态信息不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            ((SuperApplication) getApplication()).getPrintWriter().println("1#" + splitText[0] + "#" + device_status.getText());
                            ((SuperApplication) getApplication()).getPrintWriter().flush();
                            try {
                                if (((SuperApplication) getApplication()).bufferedReader.readLine().equals("10")) {
                                    runOnUiThread(() -> Toast.makeText(ScanActivity.this, "更新状态成功", Toast.LENGTH_SHORT).show());
                                    runOnUiThread(() -> {
                                        deviceSimpleInfo.setText("");
                                        device_status.setText("");
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(ScanActivity.this, "更新状态失败", Toast.LENGTH_SHORT).show());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            scanStatus = false;
                        }
                    }.start();
                }
            }else {
                Toast.makeText(ScanActivity.this, "请扫描标签后再试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(ScanActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(ScanActivity.this,pendingIntent,null,null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        NdefMessage ndefMessage;
        try {
            ndef.connect();
            ndefMessage = ndef.getNdefMessage();
            byte[] ndefData = ndefMessage.getRecords()[0].getPayload();
            int languageBytesLength = ndefData[0];
            text = new String(ndefData,1+languageBytesLength,ndefData.length -1-languageBytesLength,"UTF-8");
            splitText = text.split("#");
            text = "设备编号：" + splitText[0] + "\n设备名称：" + splitText[1] + "\n设备型号：" + splitText[2] + "\n设备位置：" + splitText[3];
            deviceSimpleInfo.setText(text);
            scanStatus = true;
        } catch (IOException | FormatException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        //管理员模式单击返回，用户模式双击back键退出
        if (getSharedPreferences("user_info", MODE_PRIVATE).getBoolean("isAdmin",false)) {
            super.onBackPressed();
        }else if (System.currentTimeMillis() - ClickTime > 800) {
            Toast.makeText(ScanActivity.this,"再按一次返回或退出",Toast.LENGTH_SHORT).show();
            ClickTime = System.currentTimeMillis();
        }else {
            new Thread(){
                @Override
                public void run() {
                    ((SuperApplication) getApplication()).SocketClose();
                }
            }.start();
            finish();
        }
    }
}
