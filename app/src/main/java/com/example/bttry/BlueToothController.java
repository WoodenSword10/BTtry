package com.example.bttry;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙适配器
 */
public class BlueToothController {
    // 成员变量
    private BluetoothAdapter mAdapter;

    /**
     * 构造函数
     */
    public BlueToothController(){
        // 获取本地的蓝牙适配器
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 是否支持蓝牙
     * @return true支持，false不支持
     */
    public boolean isSupportBlueTooth(){
        // 若支持蓝牙，则本地适配器不为null
        if(mAdapter != null){
            return true;
        }
        // 否则不支持
        else{
            return false;
        }
    }

    /**
     * 判断当前蓝牙状态
     * @return true为打开，false为关闭
     */
    public boolean getBlueToothStatus(){
        // 断言？为了避免mAdapter为null导致return出错
        assert (mAdapter != null);
        // 蓝牙状态
        return mAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     */
    public void turnOnBlueTooth(Activity activity, int requestCode){
        if(!mAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public void turnOffBlueTooth() {
        if(mAdapter.isEnabled()) {
            mAdapter.disable();
        }
    }

    /**
     * 打开蓝牙可见性
     * @param context
     */
    public void enableVisibly(Context context){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(discoverableIntent);
    }

    /**
     * 查找设备
     */
    public void findDevice(){
        assert(mAdapter!=null);
        mAdapter.startDiscovery();
    }

    /**
     * 获取绑定设备
     * @return
     */
    public ArrayList<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<BluetoothDevice>(mAdapter.getBondedDevices());
    }

}
