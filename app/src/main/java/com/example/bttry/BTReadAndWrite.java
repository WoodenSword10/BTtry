package com.example.bttry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BTReadAndWrite extends AppCompatActivity {

    private ArrayList<String> requestList = new ArrayList<>();
    private int REQ_PERMISSION_CODE = 1;
    private BluetoothSocket bluetoothSocket;
    // 延时创建Toast类，
    private Toast mToast;
    private BTclient bTclient = new BTclient();
    // 实例化蓝牙适配器类
    public BlueToothController mController = new BlueToothController();
    public Byte[] mmbuffer;
    public ArrayList<String> msglist = new ArrayList<>();
    public ListView listView;
    public ArrayAdapter adapter1;
    public readThread readthread = new readThread();
    public EditText editText;
    public Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btread_and_write);
        listView = (ListView) findViewById(R.id.listView);
        editText = (EditText) findViewById(R.id.editTextTextPersonName);

        adapter1 = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, msglist);
        listView.setAdapter(adapter1);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        bTclient.connectDevice(mController.find_device(bundle.getString("deviceAddr")));
        bTclient.start();

        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        String s = msg.obj.toString();
                        msglist.add("接收数据：" + s);
                        adapter1.notifyDataSetChanged();
                }
            }
        };
    }

    public void sead_msg(View view) {
        String s = editText.getText().toString();
        if (s.length() != 0){
            sendMessageHandle(s);
            msglist.add("发送数据：" + s + "\n");
            adapter1.notifyDataSetChanged();
        }
    }

    private class BTclient extends Thread{
        private void connectDevice(BluetoothDevice device){
            try {
                getPermision();
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();
                readthread.start();
                showToast("蓝牙连接成功");
            } catch (IOException e) {
                e.printStackTrace();
                showToast("蓝牙连接失败");
            }
        }
    }

    public void getPermision(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
            requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            requestList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestList.add(Manifest.permission.BLUETOOTH);
        }
        if(requestList.size() != 0){
            ActivityCompat.requestPermissions(this, requestList.toArray(new String[0]), REQ_PERMISSION_CODE);
        }
    }

    public void showToast(String text){
        if( mToast == null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        else{
            mToast.setText(text);
        }
        mToast.show();
    }

    //发送数据
    public void sendMessageHandle(String msg)
    {
        getPermision();
        if (bluetoothSocket == null)
        {
            showToast("没有连接");
            return;
        }

        try {
            OutputStream os = bluetoothSocket.getOutputStream();
            os.write(msg.getBytes()); //发送出去的值为：msg
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //读取数据
    private class readThread extends Thread {
        private static final String TAG = "";

        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream mmInStream = null;

            try {
                mmInStream = bluetoothSocket.getInputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (true) {
                try {
                    // Read from the InputStream
                    if( (bytes = mmInStream.read(buffer)) > 0 )
                    {
                        byte[] buf_data = new byte[bytes];
                        for(int i=0; i<bytes; i++)
                        {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);//接收的值inputstream 为 s
                        Log.e(TAG, "run: " + s);
                        Message message = Message.obtain();
                        message.what = 1;
                        message.obj = s;
                        mHandler.sendMessage(message);

                        if(s.equalsIgnoreCase("o")){ //o表示opend!
                            showToast("open");
                        }
                        else if(s.equalsIgnoreCase("c")){  //c表示closed!
                            showToast("closed");
                        }


                    }
                } catch (IOException e) {
                    try {
                        mmInStream.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}