package com.example.bttry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public boolean isOver = false;
    public ArrayAdapter adapter1;
    // 常量
    int REQUEST_CODE = 1;
    // 实例化蓝牙适配器类
    private BlueToothController mController = new BlueToothController();
    // 延时创建Toast类，
    private Toast mToast;
    // 请求列表
    private ArrayList<String> requestList = new ArrayList<>();
    //定义一个列表，存蓝牙设备的地址。
    public ArrayList<String> arrayList=new ArrayList<>();
    //定义一个列表，存蓝牙设备地址，用于显示。
    public ArrayList<String> deviceName=new ArrayList<>();
    // 常量
    private int REQ_PERMISSION_CODE = 1;
    // 蓝牙设备列表
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    // listview
    private ListView listView;
    // 消息
    private String TAG = "";
    private IntentFilter foundFilter;
    //  广播
    private BroadcastReceiver receiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state){
                case BluetoothAdapter.STATE_OFF:
                    Log.e(TAG, "onReceive: STATA_OFF");
                    showToast("STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.e(TAG, "onReceive: STATE_ON");
                    showToast("STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.e(TAG, "onReceive: STATE_TURNING_OFF");
                    showToast("STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.e(TAG, "onReceive: STATE_TURING_ON");
                    showToast("STATE_TURNING_ON");
                    break;
                default:
                    showToast("UnKnow STATE");
                    Log.e(TAG, "onReceive: UnKnow STATE");
                    unregisterReceiver(this);
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

        adapter1 = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, deviceName);
        listView.setAdapter(adapter1);
        foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);//发现蓝牙设备后的广播
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CharSequence content = ((TextView) view).getText();
                String con = content.toString();
                String[] conArray = con.split("\n");
                String rightStr = conArray[1].substring(5, conArray[1].length());
//                showToast("点击了第" + l + "行， position: " + i + "内容为：" + rightStr);
                BluetoothDevice device = mController.find_device(rightStr);
//                Log.e(TAG, "onItemClick: " + device.getName());
                if (device.getBondState() == 10) {
                    device.createBond();
                    String s = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：未配对"  + "\n";
                    deviceName.remove(s);
                    s = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：已配对"  + "\n";
                    deviceName.add(s);
                    adapter1.notifyDataSetChanged();
                    showToast("配对：" + device.getName());
                }
                else{
                    unpairDevice(device);
                    String s = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n";
                    String s2 = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：已配对"  + "\n";
                    if(deviceName.contains(s)) {
                        deviceName.remove(s);
                        adapter1.notifyDataSetChanged();
                    }
                    else if(deviceName.contains(s2)){
                        deviceName.remove(s2);
                        s2 = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：未配对"  + "\n";
                        deviceName.add(s2);
                        adapter1.notifyDataSetChanged();
                    }
                    showToast("取消配对：" + device.getName());
                }
            }
        });
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                String s;
//                Log.e(TAG, "onReceive: 发现新设备");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == 12) {
                    s = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：已配对"  + "\n";
                }
                else if (device.getBondState() == 10){
                    s = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：未配对"  + "\n";
                }else{
                    s = "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n" + "连接状态：未知"  + "\n";
                }
                if (!deviceName.contains(s)) {
                    deviceName.add(s);//将搜索到的蓝牙名称和地址添加到列表。
                    arrayList.add(device.getAddress());//将搜索到的蓝牙地址添加到列表。
                    adapter1.notifyDataSetChanged();//更新
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
//                Log.e(TAG, "onReceive: 搜索结束");
                showToast("搜索结束");
                isOver = false;
                unregisterReceiver(this);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
//                Log.e(TAG, "onReceive: 搜索开始");
            }
        }
    };

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
            requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            requestList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if(requestList.size() != 0){
            ActivityCompat.requestPermissions(this, requestList.toArray(new String[0]), REQ_PERMISSION_CODE);
        }
    }

    /**
     * 按钮点击事件，获取可连接的蓝牙设备地址
     * @param
     */
    public void refresh_BT(View view) {
        deviceName.clear();
        arrayList.clear();
        adapter1.notifyDataSetChanged();
        getPermision();
        bluetoothDevices = mController.getBondedDeviceList();
        for (int i = 0; i < bluetoothDevices.size(); i++){
            BluetoothDevice device = bluetoothDevices.get(i);
            arrayList.add(device.getAddress());
            deviceName.add("设备名："+device.getName()+"\n" +"设备地址："+device.getAddress() + "\n");//将搜索到的蓝牙名称和地址添加到列表。
            adapter1.notifyDataSetChanged();
        }
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
        showToast("蓝牙开启可见");
    }

    public void connect_BT(View view) {
        if (!isOver) {
            isOver = true;
            registerReceiver(bluetoothReceiver, foundFilter);
            arrayList.clear();
            deviceName.clear();
            adapter1.notifyDataSetChanged();
            getPermision();
            boolean ret = mController.findDevice();
            showToast("开始搜索");
        }
        else{
            showToast("搜索进行中");
        }
    }

    /**
     * 尝试取消配对
     * @param device
     */
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
