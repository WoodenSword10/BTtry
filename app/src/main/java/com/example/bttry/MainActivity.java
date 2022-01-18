package com.example.bttry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // 常量
    int REQUEST_CODE = 1;
    // 实例化蓝牙适配器类
    private BlueToothController mController = new BlueToothController();
    // 延时创建Toast类，
    private Toast mToast;
    // 请求列表
    private ArrayList<String> requestList = new ArrayList<String>();
    // 常量
    private int REQ_PERMISSION_CODE = 1;
    // 蓝牙设备列表
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<BluetoothDevice>();
    // listview
    private ListView listView;
    // 消息
    private String TAG = "";
    //
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    //
    private ArrayList<BluetoothDevice> deviceAdapter = new ArrayList<>();
    //
    private DeviceAdapter mAdapter;
    //
    private List<BluetoothDevice> mDeviceList = new ArrayList<>();
    // 广播
    private BroadcastReceiver receiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    showToast("STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    showToast("STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    showToast("STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    showToast("STATE_TURNING_ON");
                    break;
                default:
                    showToast("UnKnow STATE");
            }
        }
    };

    // 广播2
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                //setProgressBarIndeterminateVisibility(true);
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                //setProgressBarIndeterminateVisibility(false);
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //找到一个添加一个
                mDeviceList.add(device);
                mAdapter.notifyDataSetChanged();

            } else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {  //此处作用待细查
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
                if(scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    setProgressBarIndeterminateVisibility(true);
                } else {
                    setProgressBarIndeterminateVisibility(false);
                }

            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(remoteDevice == null) {
                    showToast("无设备");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                if(status == BluetoothDevice.BOND_BONDED) {
                    showToast("已绑定" + remoteDevice.getName());
                } else if(status == BluetoothDevice.BOND_BONDING) {
                    showToast("正在绑定" + remoteDevice.getName());
                } else if(status == BluetoothDevice.BOND_NONE) {
                    showToast("未绑定" + remoteDevice.getName());
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
        listView = (ListView) findViewById(R.id.listView);

        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        //开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //结束查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //查找设备
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        //设备扫描模式改变
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        //绑定状态
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        // 注册广播接收器，接收并处理搜索结果
        registerReceiver(mReceiver, intentFilter);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        listView.setAdapter(mAdapter);
    }

    /**
     * 按钮点击事件1，判断是否支持蓝牙
     * @param view 显示
     */
    public void isSupportBlueTooth(View view) {
        boolean ret = mController.isSupportBlueTooth();
        showToast("support BlueTooth? " + ret);
    }

    /**
     * 按钮点击事件，判断蓝牙是否打开
     * @param view 显示
     */
    public void isOpenBlueTooth(View view) {
        boolean ret = mController.getBlueToothStatus();
        showToast("BlueTooth enable? " + ret);
    }

    /**
     * 按钮点击事件，打开蓝牙
     * @param view
     */
    public void turnOnBlueTooth(View view) {
        getPermision();
        mController.turnOnBlueTooth(this, REQUEST_CODE);
    }

    public void turnOffBlueTooth(View view) {
        getPermision();
        mController.turnOffBlueTooth();
    }

    /**
     * Toast弹窗显示
     * @param text  显示文本
     */
    public void showToast(String text){
        if( mToast == null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        else{
            mToast.setText(text);
        }
        mToast.show();
    }

    // 安卓12破更新，需要动态申请权限
    public void getPermision(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if(requestList.size() != 0){
            ActivityCompat.requestPermissions(this, requestList.toArray(new String[0]), REQ_PERMISSION_CODE);
        }
    }

    /**
     * 按钮点击事件，获取可连接的蓝牙设备地址
     * @param view
     */
    public void refresh_BT(View view) {
        listView.setAdapter(null);
        getPermision();
        bluetoothDevices = mController.getBondedDeviceList();
        ArrayAdapter myadapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, bluetoothDevices);
        listView.setAdapter(myadapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            showToast("open successfully");
        }
        else{
            showToast("open unsuccessfully");
        }
    }

    /**
     * 按钮点击事件，使本地蓝牙可视化
     * @param view
     */
    public void make_Visible(View view) {
        getPermision();
        mController.enableVisibly(this);
    }

    public void connect_BT(View view) {
        getPermision();
        mAdapter.refresh(mDeviceList);
        mController.findDevice();
    }
}
