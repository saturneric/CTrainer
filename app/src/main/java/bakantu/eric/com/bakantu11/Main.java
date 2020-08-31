package bakantu.eric.com.bakantu11;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

//import java.util.ArrayList;

//import java.util.ArrayList;


public class Main extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private BluetoothDevice _devices;
    //要搜寻的蓝牙设备地址
    private String address = "C4:BE:84:05:80:58";

    private static final int REQUEST_ENABLE = 0x1;
    private static final int REQUEST_DISCOVERABLE = 0x2;
    //获得蓝牙适配器
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

    private  BluetoothGatt gatt_connect;
    private TextView textView;


    //打开蓝牙
    public void open_buttons (View view){

        Toast.makeText(getApplicationContext(), "Opening Buletooth...",
                Toast.LENGTH_SHORT).show();
        //判断蓝牙开启状态
        if (!_bluetooth.isEnabled()) {
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
        }

    }
    //打开可搜索模式
    public void discoverable_buttons (View view){

        Toast.makeText(getApplicationContext(), "Set discover-able...",
                Toast.LENGTH_SHORT).show();
        Intent discorver = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discorver.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        startActivityForResult(discorver, REQUEST_DISCOVERABLE);
    }

    //搜索周边蓝牙设备
    public void discover_buttons (View view){
        Toast.makeText(getApplicationContext(), "Discovering...",
                Toast.LENGTH_SHORT).show();
        _bluetooth.startLeScan(mLeScanCallback);
    }

    //搜索周边蓝牙设备回调函数
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //提示发现设备的信息
                    Toast.makeText(getApplicationContext(), "Found:" + device.getName() + "\n" + device.getAddress(),
                            Toast.LENGTH_SHORT).show();
                    //根据蓝牙地址筛选需要连接的设备
                    if (device.getAddress().equals(address)) {
                        //找到需要设备的提示
                        Toast.makeText(getApplicationContext(), "Found: Bakantu!",
                                Toast.LENGTH_SHORT).show();
                        //记录设备的信息以备后面的连接
                        _devices = device;
                        //停止搜索
                        _bluetooth.stopLeScan(mLeScanCallback);
                    }
                }
            });


        }
    };

    //连接蓝牙设备
    public void connect_buttons (View view){
        Toast.makeText(getApplicationContext(), "Connecting...",
                Toast.LENGTH_SHORT).show();
        gatt_connect = _devices.connectGatt(this,false,mGattCallbacks);
    }

    //关于UUID不必深入了解,如果不知道,就去问做蓝牙设备的人.

    //蓝牙设备服务的UUID
    public static final UUID TRANSFER_SERVICE = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB");
    //用于读数据的Characteristic的UUID
    public static final UUID TRANSFER_CHARACTERISTIC_READ = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");
    //用于写数据的Characteristic的UUID
    public static final UUID TRANSFER_CHARACTERISTIC_WRITE = UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB");
    //用于监听的Characteristic的UUID
    public static final UUID TRANSFER_CHARACTERISTIC_NOTIFY = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB");

    private BluetoothGattCharacteristic ReadCharacteristric = null;
    private BluetoothGattCharacteristic WriteCharacteristric = null;
    private BluetoothGattCharacteristic NotifyCharacteristric = null;
    private BluetoothGattService GattService;

    //蓝牙连接回调函数
    public BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            textView = (TextView)findViewById(R.id.textView);
            //连接状态改变时触发
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    String string = "Connected";
                    textView.setText(string);
                    //gatt_connect.discoverServices();
                } else if (status == BluetoothProfile.STATE_DISCONNECTED) {
                    String string = "Disconnected";
                    textView.setText(string);
                }
            }
        }

        //服务搜索完成时触发
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            textView = (TextView)findViewById(R.id.textView);
            //判断是否成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (gatt_connect.getService(TRANSFER_SERVICE) != null){
                    GattService = gatt_connect.getService(TRANSFER_SERVICE);
                    String string = "Succeed In Get Server";
                    textView.setText(string);

                }
            }
        }

        //读取数据时触发
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            //判断是否成功
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacterisricValue(characteristic);
            }
        }

        //写数据时触发
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt,characteristic,status);
            textView = (TextView)findViewById(R.id.textView3);
            String string = null;
            //判断是否成功
            if (status == BluetoothGatt.GATT_SUCCESS){
                string = "Successful!";
            }
            else{
                string = "Fail";
            }
            textView.setText(string);
        }

        //监听的数据改变时触发
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt,characteristic);
            NofiyCharacterisricValue(characteristic);
        }

    };

    //关闭连接
    public void connect_cancle(View view){
       gatt_connect.disconnect();
    }

    //读取数据
    public void readCharacterisricValue(BluetoothGattCharacteristic characteristic){
        textView = (TextView)findViewById(R.id.textView2);
        byte[] R_data = characteristic.getValue();
        String utf8_string = new String(R_data);
        String string = "Read Data:" + utf8_string;
        textView.setText(string);

    }
    //把监听接口收到的数据显示在屏幕上
    public void  NofiyCharacterisricValue(BluetoothGattCharacteristic characteristic){
        textView = (TextView)findViewById(R.id.textView4);
        byte[] N_data = characteristic.getValue();
        String utf8_string = new String(N_data);
        String string = "Get Data:" + utf8_string;
        textView.setText(string);
    }
    //向设备读数据
    public void get_char(View view){
        ReadCharacteristric = GattService.getCharacteristic(TRANSFER_CHARACTERISTIC_READ);
        if ( ReadCharacteristric != null) {
            textView = (TextView)findViewById(R.id.textView2);
            String string = "Succeed In Get Characteristric";
            textView.setText(string);
            gatt_connect.readCharacteristic(ReadCharacteristric);
        }
    }
    //向设备写数据
    public  void put_char(View view){
        String utf8_string = "Hello World!";
        WriteCharacteristric = GattService.getCharacteristic(TRANSFER_CHARACTERISTIC_WRITE);
        WriteCharacteristric.setValue(utf8_string);
        gatt_connect.writeCharacteristic(WriteCharacteristric);
    }

    //监听功能设置(开启)
    public  void notify_char(View view){
        NotifyCharacteristric = GattService.getCharacteristic(TRANSFER_CHARACTERISTIC_NOTIFY);
        gatt_connect.setCharacteristicNotification(NotifyCharacteristric,true);
    }

    //搜寻蓝牙设备的服务
    public void server(View view){
        gatt_connect.discoverServices();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
