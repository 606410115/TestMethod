package com.example.testmethod;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.testmethod.BluetoothLeService.CHARAC_WRITE;
import static com.example.testmethod.BluetoothLeService.CUSTOM_SERVICE;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{
    private final static String TAG = MainActivity.class.getSimpleName();
    //----BLE----//
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private static final int REQUEST_PERMISSION = 0;

    private Handler mHandler = new Handler();
    //Android 5.0 ↑
    private BluetoothLeScanner mBluetoothLeScanner;
    ScanCallback scanCallback;
    //Android 5.0 ↓
    private BluetoothAdapter.LeScanCallback myLEScanCallback;
    public BluetoothAdapter mMOTiBluetoothAdapter;

    public static BleBroadcastReceiver mReceiver;

    ArrayList<BLEDevice> BLEDevices = new ArrayList<>();

    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;
    //----BLE----//

    ListView lvDevice;
    private DeviceListAdapter lvDeviceAdapter;

    private SwipeRefreshLayout laySwipe;
    private SwipeRefreshLayout.OnRefreshListener onSwipeToRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            laySwipe.setRefreshing(true);
            scanLeDevice(true);
        }
    };

    private boolean checkPermission = false;
    private boolean checkBLE = false;
    private ProgressDialog pd = null;

    public byte speed = 0x14;   //default 20ms

    /////////////////////////MYO//////////////////////
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 1;

    private String deviceName;

    private Button mybtn;
    private TextView mTextView;
    private TextView myoStatusTextView;

    public BluetoothAdapter mMyoBluetoothAdapter;
    private BluetoothGatt    mBluetoothGatt;
    public static MyoGattCallback mMyoCallback;

    private MyoCommandList commandList = new MyoCommandList();

    TimeManager timeManager;

    //public static boolean emgState = false; //false represent static state

    //who get a new end threshold and start calculate length
    public static boolean endEmg = false;
    public static boolean endImu = false;
    public static boolean endMoti = false;

    //when id list need to remove list, use it
    public static boolean emgStartRemove = false;
    public static boolean emgEndRemove = false;

    public static boolean emgStart = false;
    public static boolean emgEnd = false;
    public static boolean imuStart = false;
    public static boolean imuEnd = false;
    public static boolean motiStart = false;
    public static boolean motiEnd = false;

    public static int startCount = 0;
    public static int endCount = 0;

    public static int currentStart = 0;
    public static int currentEnd = 0;

//    public static boolean addFlag = false;
//    public static boolean endFlag = false;
//    public static boolean cleanListFlag = false;
//
//    public static boolean myoEmgPreventEndAgain = false;
//    public static boolean myoImuPreventEndAgain = false;
//    public static boolean motiPreventEndAgain = false;
//
//    public static boolean myoEmgHaveCleaned= false;
//    public static boolean myoImuHaveCleaned = false;
//    public static boolean motiHaveCleaned = false;

    private TextView countText1;
    private TextView countText2;
    private TextView countText3;
    private TextView countText4;
    private TextView countText5;
    private TextView countText6;

    private static String trainingMotion = null;
    private static int trainingPosition = 0;
    private static int modeFlag = 1;// 1 is original mode(classify)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mybtn = (Button) findViewById(R.id.bWrite);
        mTextView= (TextView) findViewById(R.id.textView3);
        myoStatusTextView = (TextView) findViewById(R.id.myoStatusText);

        countText1= (TextView) findViewById(R.id.countText1);
        countText2= (TextView) findViewById(R.id.countText2);
        countText3= (TextView) findViewById(R.id.countText3);
        countText4= (TextView) findViewById(R.id.countText4);
        countText5= (TextView) findViewById(R.id.countText5);
        countText6= (TextView) findViewById(R.id.countText6);

        check();
        checkBLE();
        initView();

        mReceiver = new BleBroadcastReceiver(this, timeManager);
        mReceiver.setCurrentActivity(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice bluetoothDevice;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        bluetoothDevice = result.getDevice();
                        if(bluetoothDevice.getName() != null && bluetoothDevice.getName().contains("MOTi_")) {
                            lvDeviceAdapter.addDevice(bluetoothDevice);
                            lvDeviceAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
        }else{
            myLEScanCallback = new BluetoothAdapter.LeScanCallback()
            {
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
                {
                    if(device.getName() != null && device.getName().contains("MOTi_")) {//MOTi
                        lvDeviceAdapter.addDevice(device);
                        lvDeviceAdapter.notifyDataSetChanged();
                    }
                }

            };
        }

        /////////MYO
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mMyoBluetoothAdapter = mBluetoothManager.getAdapter();

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(ListActivity.TAG);

        if (deviceName != null) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mMyoBluetoothAdapter == null || !mMyoBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // Scanning Time out by Handler.
                // The device scanning needs high energy.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMyoBluetoothAdapter.stopLeScan(MainActivity.this);
                    }
                }, SCAN_PERIOD);
                mMyoBluetoothAdapter.startLeScan(this);
            }
        }

        try {

            File mSDFile = null;

            //檢查有沒有SD卡裝置
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
                //Toast.makeText(MainActivity.this, "沒有SD卡!!!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                //取得SD卡儲存路徑
                //mSDFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                mSDFile = Environment.getExternalStorageDirectory();
            }

            //建立文件檔儲存路徑
            //File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData");

            File mMotiImuTestFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MotiImuTestData");
            File mMyoImuTestFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MyoImuTestData");
            File mMyoEmgTestFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MyoEmgTestData");

            if (!mMotiImuTestFile.exists() || !mMyoImuTestFile.exists() || !mMyoEmgTestFile.exists()) {

                if(!mMotiImuTestFile.mkdirs() || !mMyoImuTestFile.mkdirs() || !mMyoEmgTestFile.mkdirs()){//無法建立檔案
                    throw new Error("mkdirs error");
                }
                else{
                    Log.d("MainActivity", "testData make success");
                }
            }

            File mMotiImuTrainingFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MotiImuTraining");
            File mMyoImuTrainingFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoImuTraining");
            File mMyoEmgTrainingFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoEmgTraining");

            //若沒有檔案儲存路徑時則建立此檔案路徑
            if (!mMotiImuTrainingFile.exists() || !mMyoImuTrainingFile.exists() || !mMyoEmgTrainingFile.exists()) {

                if(!mMotiImuTrainingFile.mkdirs() || !mMyoImuTrainingFile.mkdirs() || !mMyoEmgTrainingFile.mkdirs()){//無法建立檔案
                    throw new Error("mkdirs error");
                }
                else{
                    FileWriter motiImuTraining = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MotiImuTraining/TrainingData.txt");
                    MotiImuTrainingData motiImuTrainingData = new MotiImuTrainingData();
                    motiImuTraining.write(motiImuTrainingData.getMotiImuTrainingData());
                    motiImuTraining.close();

                    FileWriter myoImuTraining = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoImuTraining/TrainingData.txt");
                    MyoImuTrainingData myoImuTrainingData = new MyoImuTrainingData();
                    myoImuTraining.write(myoImuTrainingData.getMyoImuTrainingData());
                    myoImuTraining.close();

                    FileWriter myoEmgTraining = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoEmgTraining/TrainingData.txt");
                    MyoEmgTrainingData myoEmgTrainingData = new MyoEmgTrainingData();
                    myoEmgTraining.write(myoEmgTrainingData.getMyoEmgTrainingData());
                    myoEmgTraining.close();
                }
            }



            //Log.d("saveSuccess","已儲存文字");
        } catch (Exception e) {
            Log.e("save data error", e.getLocalizedMessage());
        }
    }

    ///////////////MYO
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0, MENU_LIST, 0, "Find Myo");
        menu.add(0, MENU_BYE, 0, "Good Bye");
        return true;
    }

    @Override
    public void onStop(){
        super.onStop();
        this.closeBLEGatt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case MENU_LIST:
//                Log.d("Menu","Select Menu A");
                Intent intent = new Intent(this,ListActivity.class);
                startActivity(intent);
                return true;

            case MENU_BYE:
//                Log.d("Menu","Select Menu B");
                closeBLEGatt();
                Toast.makeText(getApplicationContext(), "Close GATT", Toast.LENGTH_SHORT).show();

                return true;

        }
        return false;
    }

    /** Define of BLE Callback */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceName.equals(device.getName())) {
            mMyoBluetoothAdapter.stopLeScan(this);

            HashMap<String,View> views = new HashMap<String,View>();
            //put result textView
            views.put("result", mTextView);
            //put myo status textView
            views.put("myoStatus", myoStatusTextView);
            //put motion countTextView
            views.put("motion1", countText1);
            views.put("motion2", countText2);
            views.put("motion3", countText3);
            views.put("motion4", countText4);
            views.put("motion5", countText5);
            views.put("motion6", countText6);

            // Trying to connect GATT
            mMyoCallback = new MyoGattCallback(views, timeManager, this);
            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }
    }

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    @Override
    protected void onResume() {
        super.onResume();

        mReceiver.setCurrentActivity(this);

        if(checkPermission && checkBLE) {
            laySwipe.setRefreshing(true);
            scanLeDevice(true);
        }
    }

    private void initView(){
        laySwipe = (SwipeRefreshLayout) findViewById(R.id.laySwipe);
        laySwipe.setOnRefreshListener(onSwipeToRefresh);
        laySwipe.setColorSchemeResources(
                android.R.color.darker_gray);

        lvDevice = (ListView)findViewById(R.id.lvDevice);
        lvDeviceAdapter = new DeviceListAdapter(this);
        lvDevice.setAdapter(lvDeviceAdapter);
        lvDeviceAdapter.updateListData();
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(lvDeviceAdapter.getItem(position) instanceof BluetoothDevice) {
                    if (mReceiver.mBluetoothLeService == null)
                        return;
                    laySwipe.setRefreshing(false);
                    scanLeDevice(false);
                    BluetoothDevice device = (BluetoothDevice)lvDeviceAdapter.getItem(position);
                    final BLEDevice d = new BLEDevice(MainActivity.this, device);
                    BLEDevices.add(d);
                    connectBle(d);
                    lvDeviceAdapter.remove(device);
                    lvDeviceAdapter.updateConnectedDevice(BLEDevices);

                    pd = new ProgressDialog(MainActivity.this);
                    pd.setMessage("Connecting");
                    pd.setCancelable(false);
                    pd.show();
                }
            }
        });
        lvDevice.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    laySwipe.setEnabled(true);
                }else{
                    laySwipe.setEnabled(false);
                }
            }
        });
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(r, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ScanSettings settings = new ScanSettings.Builder()
                        .setReportDelay(0)
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                ArrayList<ScanFilter> filters = new ArrayList<>();
                mBluetoothLeScanner.startScan(filters, settings ,scanCallback);
            }else{
                mMOTiBluetoothAdapter.startLeScan(myLEScanCallback);
            }
        } else {
            mHandler.removeCallbacks(r);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothLeScanner.stopScan(scanCallback);
            }else {
                mMOTiBluetoothAdapter.stopLeScan(myLEScanCallback);
            }
        }
    }

    private void clear(){
        lvDeviceAdapter.clear();
        lvDeviceAdapter.notifyDataSetChanged();
    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothLeScanner.stopScan(scanCallback);
            }else{
                mMOTiBluetoothAdapter.stopLeScan(myLEScanCallback);
            }
            laySwipe.setRefreshing(false);
        }
    };

//    private boolean connectBle(String address) {
//        return mBluetoothLeService.connect(address);
//    }

    private boolean connectBle(BLEDevice bled) {
        return mReceiver.mBluetoothLeService.connect(bled);
    }

    public void connect() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pd.cancel();
            }
        }, 1000);
    }

    public void disconnect(String name) {
        if(pd != null)
            pd.cancel();
        for (BLEDevice bleDevice : BLEDevices){
            if(bleDevice.device.getName() != null && bleDevice.device.getName().equals(name)) {
                BLEDevices.remove(bleDevice);
                lvDeviceAdapter.updateConnectedDevice(BLEDevices);
                return;
            }
        }
    }

//    public void addLogData(byte[] byteArrayExtra, String name) {
//        if(byteArrayExtra.length == 1){
//            for(BLEDevice device : BLEDevices){
//                if(device.device.getName().equals(name)) {
//                    device.battery = (int) byteArrayExtra[0];
//                    lvDeviceAdapter.updateListData();
//                }
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.close();
    }

    private void checkBLE(){
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            checkBLE = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }else {
                this.mMOTiBluetoothAdapter = mBluetoothAdapter;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            checkBLE();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void check(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionList = new ArrayList<>();

            int permissionCoarseLocation = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {//確認有沒有授予權限
                permissionList.add(ACCESS_COARSE_LOCATION);
            }else{
                checkPermission = true;
            }

            int permissionWriteExternalStorage = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {//確認有沒有授予權限
                permissionList.add(WRITE_EXTERNAL_STORAGE);
            }

            if(permissionList.size() != 0){
                ActivityCompat.requestPermissions(this,
                        permissionList.toArray(new String[permissionList.size()]), REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    checkPermission = true;
                } else {
                    check();
                }
                break;
        }
    }

    private class DeviceListAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        ArrayList<Object> allDevice = new ArrayList<>();
        ArrayList<BLEDevice> connected_devices = new ArrayList<>();
        ArrayList<BluetoothDevice> devices = new ArrayList<>();

        DeviceListAdapter (Context context){
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return allDevice.size();
        }

        @Override
        public Object getItem(int position) {
            return allDevice.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            DeviceListAdapter.ViewHolder holder = null;
            if(convertView==null){
                convertView = layoutInflater.inflate(R.layout.device_list_item, null);
                holder = new DeviceListAdapter.ViewHolder((TextView) convertView.findViewById(R.id.tvDeviceName),
                        (Button) convertView.findViewById(R.id.btStart),
                        (Button) convertView.findViewById(R.id.btDisconnect),
                        (TextView) convertView.findViewById(R.id.tvBattery));
                convertView.setTag(holder);
            }else{
                holder = (DeviceListAdapter.ViewHolder) convertView.getTag();
            }

            if(allDevice.get(position) instanceof String){
                holder.btStart.setVisibility(View.GONE);
                holder.btDisconnect.setVisibility(View.GONE);
                holder.tvBattery.setVisibility(View.GONE);
                holder.tvDeviceName.setText((String)allDevice.get(position));
                holder.tvDeviceName.setBackgroundColor(Color.LTGRAY);
            }else if(allDevice.get(position) instanceof BLEDevice){
                holder.btStart.setVisibility(View.VISIBLE);
                holder.btDisconnect.setVisibility(View.VISIBLE);
                holder.tvBattery.setVisibility(View.VISIBLE);
                holder.tvDeviceName.setBackgroundColor(Color.TRANSPARENT);

                final BLEDevice device = (BLEDevice)allDevice.get(position);
                holder.tvDeviceName.setText(device.device.getName());
                holder.tvBattery.setText(device.battery + "%");
                holder.btStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(v.getTag() == null){
                            v.setTag("stop");
                            ((Button)v).setText("Stop");

                            writeGattCharacteristic(device, BleBroadcastReceiver.START_RAW_DATA, new byte[]{0x07, speed});
                        }else{
                            v.setTag(null);
                            ((Button)v).setText("Start");

                            writeGattCharacteristic(device, BleBroadcastReceiver.STOP_RAW_DATA, new byte[]{0x08, 0x00});
                        }
                    }
                });
                holder.btDisconnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        device.gatt.disconnect();
                    }
                });
            }else if(allDevice.get(position) instanceof BluetoothDevice){
                holder.btStart.setVisibility(View.GONE);
                holder.btDisconnect.setVisibility(View.GONE);
                holder.tvBattery.setVisibility(View.GONE);
                holder.tvDeviceName.setBackgroundColor(Color.TRANSPARENT);

                BluetoothDevice device = (BluetoothDevice)allDevice.get(position);
                if("".equals(device.getName()) || device.getName() == null)
                    holder.tvDeviceName.setText("No Name");
                else
                    holder.tvDeviceName.setText(device.getName() + " " + device.getAddress());
            }

            return convertView;
        }

        void updateConnectedDevice(ArrayList<BLEDevice> device){
            connected_devices.clear();
            connected_devices.addAll(device);
            updateListData();
        }

        void addDevice(BluetoothDevice device){
            if(check(device.getAddress())) {
                devices.add(device);
                updateListData();
            }
        }

        void updateListData(){
            allDevice.clear();
            allDevice.add("Connected");
            allDevice.addAll(connected_devices);
            allDevice.add("Search Device");
            allDevice.addAll(devices);
            notifyDataSetChanged();
        }

        void remove(Object data){
            if(data instanceof BLEDevice)
                connected_devices.remove(data);
            else if(data instanceof BluetoothDevice)
                devices.remove(data);
            else
                Log.d(TAG, "Remove Fail");
        }

        void clear(){
            devices.clear();
        }

        private boolean check(String address){
            for (int i = 0; i < devices.size(); i++) {
                if((devices.get(i)).getAddress().equals(address))
                    return false;
            }
            return true;
        }

        private class ViewHolder{
            TextView tvDeviceName;
            Button btStart;
            Button btDisconnect;
            TextView tvBattery;

            ViewHolder(TextView tvDeviceName, Button btStart, Button btDisconnect, TextView tvBattery){
                this.tvDeviceName = tvDeviceName;
                this.btStart = btStart;
                this.btDisconnect = btDisconnect;
                this.tvBattery = tvBattery;
            }
        }
    }

    public class BLEDevice{
        Context mContext;
        BluetoothDevice device;
        BluetoothGatt gatt;
        int battery = 0;

        BLEDevice(Context mContext, BluetoothDevice device){
            this.device = device;
            this.mContext = mContext;
        }
    }

    public void OnStartClick(View v){//MYO just emg
        timeManager = new TimeManager();

        writeGattCharacteristic(BleBroadcastReceiver.START_RAW_DATA, new byte[]{0x07, speed});

        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendIMUandEMG())) {
            Log.d("BLE_MYO","False EMG");
        } else {
            Log.d("BLE_MYO","Success EMG");

            mMyoCallback.init();
            BleBroadcastReceiver.mLogActivity.init();
            DataCalculator.getCurrentDataCalculator().init();
            Classify.getCurrentClassify().init();
        }
    }

    public void OnStopClick(View v){
        writeGattCharacteristic(BleBroadcastReceiver.STOP_RAW_DATA, new byte[]{0x08, 0x00});

        if (mBluetoothGatt == null
                || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                || !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())) {
            Log.d(TAG,"False Data Stop");
        }

        if (modeFlag == 1) {//classify mode
            /*LayoutInflater factory=LayoutInflater.from(MainActivity.this);
            final View v1=factory.inflate(R.layout.alertdialog_list_classify_result, null);

            new AlertDialog.Builder(this)
                    .setTitle("Result")
                    .setView(v1)
                    .setMessage(Classify.getCurrentClassify().getClassifyResult())
                    .show();

            TextView alertDialogClassifyResult = v1.findViewById(R.id.alertdialog_list_classify_result);
            alertDialogClassifyResult.setText(Classify.getCurrentClassify().getClassifyResult());*/

            try {

                File mSDFile = null;

                //檢查有沒有SD卡裝置
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
                    //Toast.makeText(MainActivity.this, "沒有SD卡!!!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //取得SD卡儲存路徑
                    //mSDFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    mSDFile = Environment.getExternalStorageDirectory();
                }

                //建立文件檔儲存路徑
                File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/");

                //若沒有檔案儲存路徑時則建立此檔案路徑
                if (!mFile.exists()) {
                    if(!mFile.mkdirs()){//無法建立檔案
                        throw new Error("mkdirs error");
                    }
                }

                FileWriter result = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/Result.txt");
                result.write(Classify.getCurrentClassify().getClassifyResult());
                result.close();
        //Log.d("saveSuccess","已儲存文字");
            } catch (Exception e) {
                Log.e("save data error", e.getLocalizedMessage());
            }
        }

    }

    public void OnSettingClick(View v){
        if (modeFlag == 0) {
            final ArrayList<String> trainingMotionList = new ArrayList<>();
            trainingMotionList.add("緊握");
            trainingMotionList.add("張開");
            trainingMotionList.add("放鬆");
            trainingMotionList.add("右手向上");
            trainingMotionList.add("右手向下");
            trainingMotionList.add("右手向左");
            trainingMotionList.add("右手向右");
            trainingMotionList.add("右手向前伸");
            trainingMotionList.add("右手縮回來");
            trainingMotionList.add("左手向上");
            trainingMotionList.add("左手向下");
            trainingMotionList.add("左手向左");
            trainingMotionList.add("左手向右");
            trainingMotionList.add("左手向前伸");
            trainingMotionList.add("左手縮回來");
        /*final EditText etSpeed = new EditText(this);
        etSpeed.setHint(">= 10ms");*/
            new AlertDialog.Builder(this)
                    .setTitle("選擇Training動作")
                    .setItems(trainingMotionList.toArray(new String[trainingMotionList.size()]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int whichOption) {
                            trainingMotion = trainingMotionList.get(whichOption);

                            if (whichOption < 3) {//training emg
                                trainingPosition = 1;
                            }
                            else if (whichOption >=3 && whichOption < 9) {//training myoImu
                                trainingPosition = 2;
                            }
                            else {//training motiImu
                                trainingPosition = 3;
                            }

                            mTextView.setText(trainingMotion);
                        }
                    })
                    .show();
        }
        else if (modeFlag == 1) {
            Toast.makeText(MainActivity.this, "Please choose training mode", Toast.LENGTH_SHORT).show();
        }

    }

    public void OnModeClick (View v) {
        final ArrayList<String> appModeList = new ArrayList<>();
        appModeList.add("Training");//0
        appModeList.add("Classify");//1

        /*final EditText etSpeed = new EditText(this);
        etSpeed.setHint(">= 10ms");*/
        new AlertDialog.Builder(this)
                .setTitle("選擇模式")
                .setItems(appModeList.toArray(new String[appModeList.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichOption) {
                        modeFlag = whichOption;
                        myoStatusTextView.setText(appModeList.get(whichOption));
                    }
                })
                .show();
    }


    private void writeGattCharacteristic(String type, byte[] value){
        LinkedList<BluetoothLeService.Request> requests = new LinkedList<>();
        for(BLEDevice device : BLEDevices){
            BluetoothGattCharacteristic characteristic = device.gatt.getService(CUSTOM_SERVICE).getCharacteristic(CHARAC_WRITE);
            requests.add(BluetoothLeService.Request.newWriteRequest(characteristic, BleBroadcastReceiver.getCommandData(type, value), device.gatt));
        }
        mReceiver.mBluetoothLeService.writeGattCharacteristic(requests);
    }

    private void writeGattCharacteristic(BLEDevice device, String type, byte[] value){
        LinkedList<BluetoothLeService.Request> requests = new LinkedList<>();
        BluetoothGattCharacteristic characteristic = device.gatt.getService(CUSTOM_SERVICE).getCharacteristic(CHARAC_WRITE);
        requests.add(BluetoothLeService.Request.newWriteRequest(characteristic, BleBroadcastReceiver.getCommandData(type, value), device.gatt));
        mReceiver.mBluetoothLeService.writeGattCharacteristic(requests);
    }

    public void saveData(View v){
        try {

            File mSDFile = null;

            //檢查有沒有SD卡裝置
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
                //Toast.makeText(MainActivity.this, "沒有SD卡!!!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                //取得SD卡儲存路徑
                //mSDFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                mSDFile = Environment.getExternalStorageDirectory();
            }

            //建立文件檔儲存路徑
            File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel");
            //File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/MyAndroid");

            //若沒有檔案儲存路徑時則建立此檔案路徑
            if (!mFile.exists()) {
                mFile.mkdirs();
            }

            /*FileWriter emgData = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/emgData.txt");
            emgData.write(DataCalculator.getCurrentDataCalculator().getEmgDataString());
            emgData.close();

            FileWriter imuData = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/imuData.txt");
            imuData.write(DataCalculator.getCurrentDataCalculator().getImuDataString());
            imuData.close();

            FileWriter motiData = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/motiData.txt");
            motiData.write(DataCalculator.getCurrentDataCalculator().getMotiDataString());
            motiData.close();

            FileWriter emgNumber = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/emgNumber.txt");
            emgNumber.write(DataCalculator.getCurrentDataCalculator().getEmgNumber());
            emgNumber.close();

            FileWriter imuNumber = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/imuNumber.txt");
            imuNumber.write(DataCalculator.getCurrentDataCalculator().getImuNumber());
            imuNumber.close();

            FileWriter motiNumber = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/motiNumber.txt");
            motiNumber.write(DataCalculator.getCurrentDataCalculator().getMotiNumber());
            motiNumber.close();

            FileWriter emgStart = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/emgStart.txt");
            emgStart.write(DataCalculator.getCurrentDataCalculator().getEmgStartString());
            emgStart.close();

            FileWriter imuStart = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/imuStart.txt");
            imuStart.write(DataCalculator.getCurrentDataCalculator().getImuStartString());
            imuStart.close();

            FileWriter motiStart = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/motiStart.txt");
            motiStart.write(DataCalculator.getCurrentDataCalculator().getMotiStartString());
            motiStart.close();

            FileWriter emgEnd = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/emgEnd.txt");
            emgEnd.write(DataCalculator.getCurrentDataCalculator().getEmgEndString());
            emgEnd.close();

            FileWriter imuEnd = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/imuEnd.txt");
            imuEnd.write(DataCalculator.getCurrentDataCalculator().getImuEndString());
            imuEnd.close();

            FileWriter motiEnd = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/motiEnd.txt");
            motiEnd.write(DataCalculator.getCurrentDataCalculator().getMotiEndString());
            motiEnd.close();

            FileWriter result = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/excel/result.txt");
            result.write(Classify.getCurrentClassify().getClassifyResult());
            result.close();*/

            Log.d("saveSuccess","已儲存文字");
        } catch (Exception e) {
        }
    }

    private static final Object emgStartLock = new Object();
    private static final Object emgEndLock = new Object();
    private static final Object imuStartLock = new Object();
    private static final Object imuEndLock = new Object();
    private static final Object motiStartLock = new Object();
    private static final Object motiEndLock = new Object();
    private static final Object emgListLock = new Object();
    private static final Object imuListLock = new Object();
    private static final Object motiListLock = new Object();

    private static final Object currentStartLock = new Object();
    private static final Object currentEndLock = new Object();

    public static Object getEmgStartLock () {
        return emgStartLock;
    }
    public static Object getEmgEndLock () {
        return emgEndLock;
    }
    public static Object getImuStartLock () {
        return imuStartLock;
    }
    public static Object getImuEndLock () {
        return imuEndLock;
    }
    public static Object getMotiStartLock () {
        return motiStartLock;
    }
    public static Object getMotiEndLock () {
        return motiEndLock;
    }

    public static Object getEmgListLock () {
        return emgListLock;
    }
    public static Object getImuListLock () {
        return imuListLock;
    }
    public static Object getMotiListLock () {
        return motiListLock;
    }

    public static Object getCurrentStartLock () {
        return currentStartLock;
    }
    public static Object getCurrentEndLock () {
        return currentEndLock;
    }


    public static String getTrainingMotion () {
        return trainingMotion;
    }

    public static int getTrainingPosition () {
        return trainingPosition;
    }


    public static int getCurrentMode () {
        return modeFlag;
    }

}
