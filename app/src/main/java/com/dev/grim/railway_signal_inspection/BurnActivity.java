package com.dev.grim.railway_signal_inspection;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

public class BurnActivity extends AppCompatActivity {
    long ClickTime = System.currentTimeMillis();
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    boolean writeEnable = false;
    String text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burn);
        android.support.v7.widget.Toolbar setting_toolbar = findViewById(R.id.burn_toolbar);
        //设置标题文字颜色
        setting_toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(setting_toolbar);
        TextView deviceID = findViewById(R.id.device_id);
        TextView deviceName = findViewById(R.id.device_name);
        TextView deviceType = findViewById(R.id.device_type);
        TextView deviceLocation = findViewById(R.id.device_location);
        Button buttonBurn = findViewById(R.id.button_burn);
        nfcAdapter = NfcAdapter.getDefaultAdapter(BurnActivity.this);
        pendingIntent = PendingIntent.getActivity(BurnActivity.this,0, new  Intent(BurnActivity.this,BurnActivity.class),0);
        buttonBurn.setOnClickListener(v -> {
            text = deviceID.getText().toString() +"#"+ deviceName.getText().toString()+"#"+ deviceType.getText().toString()+"#"+ deviceLocation.getText().toString();
            Toast.makeText(BurnActivity.this,"准备就绪，请刷标签",Toast.LENGTH_SHORT).show();
            writeEnable = true;
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(BurnActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(BurnActivity.this,pendingIntent,null,null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (writeEnable) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            byte[] languageBytes = Locale.CHINA.getLanguage().getBytes(Charset.forName("US-ASCII"));
            //将text文本编码转化为UTF-8并构建成byte[]数组
            Charset encoding = Charset.forName("UTF-8");
            byte[] textBytes = text.getBytes(encoding);
            //定义ndefData的byte[]数组大小
            byte[] ndefData = new byte[1 + languageBytes.length + textBytes.length];
            //为ndefData装载内容
            //第[0]字节，状态字节
            ndefData[0] = (byte) languageBytes.length;
            //第[1 - n]字节，编码方式，长度由状态字节的低6位指定
            System.arraycopy(languageBytes, 0, ndefData, 1, languageBytes.length);
            //第[n+1 - m]字节，text文本内容
            System.arraycopy(textBytes, 0, ndefData, 1 + languageBytes.length, textBytes.length);
            //生成NdefRecord
            NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], ndefData);
            //将NdefRecord封装进NdefMessage
            NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
            Ndef ndef = Ndef.get(tag);
            if(ndef!=null){
                try {
                    ndef.connect();
                    if(ndef.isWritable()){
                        if (ndef.getMaxSize()>=ndefMessage.toByteArray().length){
                            ndef.writeNdefMessage(ndefMessage);
                            Toast.makeText(BurnActivity.this,"标签写入成功",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(BurnActivity.this,"标签容量太小",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(BurnActivity.this,"标签拒绝写入",Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException | FormatException e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(BurnActivity.this,"标签不支持NDEF格式数据",Toast.LENGTH_SHORT).show();
            }
            writeEnable = false;
        }
    }

    @Override
    public void onBackPressed() {
        //双击back键退出
        if (System.currentTimeMillis() - ClickTime > 800) {
            Toast.makeText(BurnActivity.this,"再按一次返回或退出",Toast.LENGTH_SHORT).show();
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
