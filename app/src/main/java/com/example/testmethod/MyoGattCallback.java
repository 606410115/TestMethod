package com.example.testmethod;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by naoki on 15/04/15.
 */
 
public class MyoGattCallback extends BluetoothGattCallback {
    /** Service ID */
    private static final String MYO_CONTROL_ID  = "d5060001-a904-deb9-4748-2c7f4a124842";
    private static final String MYO_EMG_DATA_ID = "d5060005-a904-deb9-4748-2c7f4a124842";
    private static final String MYO_IMU_DATA_ID = "d5060002-a904-deb9-4748-2c7f4a124842";
    /** Characteristics ID */
    private static final String MYO_INFO_ID = "d5060101-a904-deb9-4748-2c7f4a124842";
    private static final String FIRMWARE_ID = "d5060201-a904-deb9-4748-2c7f4a124842";
    private static final String COMMAND_ID  = "d5060401-a904-deb9-4748-2c7f4a124842";
    private static final String EMG_0_ID    = "d5060105-a904-deb9-4748-2c7f4a124842";
    private static final String IMU_DATA_ID    = "d5060402-a904-deb9-4748-2c7f4a124842";
    /** android Characteristic ID (from Android Samples/BluetoothLeGatt/SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG) */
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private final static int EMG_WINDOW_LENGTH = 5;
    private final static int EMG_MIN_LENGTH = 30;
    private final static int EMG_START_THRESHOLD = 8;
    private final static int EMG_END_THRESHOLD = 5;

    private final static int IMU_WINDOW_LENGTH = 30;
    private final static double imuDifferenceThreshold = 2;

    private final static int emgListLength = 1000000;
    private final static int imuListLength = 1000000;

    private final static int emgSegmentLength = 15;
    private final static int imuSegmentLength = 30;

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic_command;
    private BluetoothGattCharacteristic mCharacteristic_emg0;
    private BluetoothGattCharacteristic mCharacteristic_imu;

    private MyoCommandList commandList = new MyoCommandList();

    private String TAG = "MyoGatt";

    //count motion textView
    private TextView countText1;
    private TextView countText2;
    private TextView countText3;
    private TextView countText4;
    private TextView countText5;
    private TextView countText6;

    //private TextView dataView;
    private TextView myoStatusView;
    private String callback_msg;
    //private Handler mHandler;
    private int[] emgDatas = new int[16];

    public String EMG_data="";

    private int emgStreamCount = 0;
    private boolean emgState = false; //false represent static state

    private int imuStreamCount = 0;
    private boolean imuState = false;
//    private static final float MYOHW_ORIENTATION_SCALE = 16384.0f;
//    private static final float MYOHW_ACCELEROMETER_SCALE = 2048.0f;
//    private static final float MYOHW_GYROSCOPE_SCALE = 16.0f;
//    private static final float G=9.8f;

    private LinkedList<EmgData> list_emg = new LinkedList<>();
    private LinkedList<ImuData> list_imu = new LinkedList<>();

    private LinkedList<EmgData> list_emgWindow = new LinkedList<>();//時間內能量大小的window
    private LinkedList<ImuData> list_imuWindow = new LinkedList<>();//時間內imu變化量
//arraylist

    private ArrayList<Integer> list_emgStart = new ArrayList<>();
    private ArrayList<Integer> list_emgEnd = new ArrayList<>();
    private ArrayList<Integer> list_imuStart = new ArrayList<>();
    private ArrayList<Integer> list_imuEnd = new ArrayList<>();

    private TimeManager timeManager;
    private Activity activity;

    //private boolean emgStart = false, emgEnd = false, imuStart = false, imuEnd = false;
    private boolean endEmg = false, endImu = false;

    private int emgId = -1, imuId = -1;

    public MyoGattCallback(HashMap<String,View> views, TimeManager tM, Activity mainActivity){
        //dataView = (TextView) views.get("result");
        myoStatusView = (TextView) views.get("myoStatus");

        countText1= (TextView) views.get("motion1");
        countText2= (TextView) views.get("motion2");
        countText3= (TextView) views.get("motion3");
        countText4= (TextView) views.get("motion4");
        countText5= (TextView) views.get("motion5");
        countText6= (TextView) views.get("motion6");

        timeManager = tM;
        activity = mainActivity;

        Classify.getCurrentClassify().setActivity(mainActivity);
        Classify.getCurrentClassify().setTextView(views);
    }

    public void init() {
        emgStreamCount = 0;
        emgState = false;
        imuStreamCount = 0;
        imuState = false;
        list_emg.clear();
        list_imu.clear();
        list_emgWindow.clear();
        list_imuWindow.clear();
        list_emgStart.clear();
        list_emgEnd.clear();
        list_imuStart.clear();
        list_imuEnd.clear();
        endEmg = false;
        endImu = false;
        emgId = -1;
        imuId = -1;
    }

    public int getEmgListLength () {
        return emgListLength;
    }

    public int getImuListLength () {
        return imuListLength;
    }

    public LinkedList<EmgData> getList_emg () {
        //Log.d("myo", "emg size : " + list_emg.size());
        return list_emg;
    }

    public LinkedList<ImuData> getList_imu () {
        //Log.d("myo", "imu size : " + list_imu.size());
        return list_imu;
    }

    public ArrayList<Integer> getList_emgStart () {
        //Log.d("myo", "emgStart size : " + list_emgStart.size());
        return list_emgStart;
    }

    public ArrayList<Integer> getList_emgEnd () {
        //Log.d("myo", "emgEnd size : " + list_emgEnd.size());
        return list_emgEnd;
    }

    public ArrayList<Integer> getList_imuStart () {
        //Log.d("myo", "imuStart size : " + list_imuStart.size());
        return list_imuStart;
    }

    public ArrayList<Integer> getList_imuEnd () {
        //Log.d("myo", "imuEnd size : " + list_imuEnd.size());
        return list_imuEnd;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Log.d(TAG, "onConnectionStateChange: " + status + " -> " + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // GATT Connected
            // Searching GATT Service
            gatt.discoverServices();

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // GATT Disconnected
            stopCallback();
            Log.d(TAG,"Bluetooth Disconnected");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        boolean checkEmgCommand = false, checkImuCommand = false;

        Log.d(TAG, "onServicesDiscovered received: " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            // Find GATT Service
            BluetoothGattService service_emg = gatt.getService(UUID.fromString(MYO_EMG_DATA_ID));
            if (service_emg == null) {
                Log.d(TAG,"No Myo EMG-Data Service !!");
            } else {
                Log.d(TAG, "Find Myo EMG-Data Service !!");
                checkEmgCommand = true;
                // Getting CommandCharacteristic
                mCharacteristic_emg0 = service_emg.getCharacteristic(UUID.fromString(EMG_0_ID));
                if (mCharacteristic_emg0 == null) {
                    callback_msg = "Not Found EMG-Data Characteristic";
                } else {
                    // Setting the notification
                    boolean registered_0 = gatt.setCharacteristicNotification(mCharacteristic_emg0, true);
                    if (!registered_0) {
                        Log.d(TAG,"EMG-Data Notification FALSE !!");
                    } else {
                        Log.d(TAG,"EMG-Data Notification TRUE !!");
                        // Turn ON the Characteristic Notification
                        BluetoothGattDescriptor descriptor_0 = mCharacteristic_emg0.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        if (descriptor_0 != null ){
                            descriptor_0.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            writeGattDescriptor(descriptor_0);

                            Log.d(TAG,"Set descriptor");

                        } else {
                            Log.d(TAG,"No descriptor");
                        }
                    }
                }
            }

            // Find GATT Service(mIMU)
            BluetoothGattService service_imu = gatt.getService(UUID.fromString(MYO_IMU_DATA_ID));
            if (service_imu == null) {
                Log.d(TAG,"No Myo IMU-Data Service !!");
            } else {
                Log.d(TAG, "Find Myo IMU-Data Service !!");
                checkImuCommand = true;
                // Getting CommandCharacteristic
                mCharacteristic_imu = service_imu.getCharacteristic(UUID.fromString(IMU_DATA_ID));
                if (mCharacteristic_imu == null) {
                    callback_msg = "Not Found IMU-Data Characteristic";
                } else {
                    // Setting the notification
                    boolean registered_imu = gatt.setCharacteristicNotification(mCharacteristic_imu, true);
                    if (!registered_imu) {
                        Log.d(TAG,"IMU-Data Notification FALSE !!");
                    } else {
                        Log.d(TAG,"IMU-Data Notification TRUE !!");
                        // Turn ON the Characteristic Notification
                        BluetoothGattDescriptor descriptor_imu = mCharacteristic_imu.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        if (descriptor_imu != null ){
                            descriptor_imu.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                            writeGattDescriptor(descriptor_imu);

                            Log.d(TAG,"Set descriptor");

                        } else {
                            Log.d(TAG,"No descriptor");
                        }
                    }
                }
            }

            if(checkEmgCommand && checkImuCommand){
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        myoStatusView.setText("Find");
                        //init count
                        countText1.setText("0");
                        countText2.setText("0");
                        countText3.setText("0");
                        countText4.setText("0");
                        countText5.setText("0");
                        countText6.setText("0");
                    }
                });
            }

            BluetoothGattService service = gatt.getService(UUID.fromString(MYO_CONTROL_ID));
            if (service == null) {
                Log.d(TAG,"No Myo Control Service !!");
            } else {
                Log.d(TAG, "Find Myo Control Service !!");
                // Get the MyoInfoCharacteristic
                BluetoothGattCharacteristic characteristic =
                        service.getCharacteristic(UUID.fromString(MYO_INFO_ID));
                if (characteristic == null) {
                } else {
                    Log.d(TAG, "Find read Characteristic !!");
                    //put the characteristic into the read queue
                    readCharacteristicQueue.add(characteristic);
                    //if there is only 1 item in the queue, then read it.  If more than 1, we handle asynchronously in the callback above
                    //GIVE PRECEDENCE to descriptor writes.  They must all finish first.
                    if((readCharacteristicQueue.size() == 1) && (descriptorWriteQueue.size() == 0)) {
                        mBluetoothGatt.readCharacteristic(characteristic);
                    }
/*                        if (gatt.readCharacteristic(characteristic)) {
                            Log.d(TAG, "Characteristic read success !!");
                        }
*/
                }

                // Get CommandCharacteristic
                mCharacteristic_command = service.getCharacteristic(UUID.fromString(COMMAND_ID));
                if (mCharacteristic_command == null) {
                } else {
                    Log.d(TAG, "Find command Characteristic !!");
                }
            }
        }
    }

    public void writeGattDescriptor(BluetoothGattDescriptor d){
        //put the descriptor into the write queue
        descriptorWriteQueue.add(d);
        //if there is only 1 item in the queue, then write it.  If more than 1, we handle asynchronously in the callback above
        if(descriptorWriteQueue.size() == 1){
            mBluetoothGatt.writeDescriptor(d);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
        }
        else{
            Log.d(TAG, "Callback: Error writing GATT Descriptor: "+ status);
        }
        descriptorWriteQueue.remove();  //pop the item that we just finishing writing
        //if there is more to write, do it!
        if(descriptorWriteQueue.size() > 0)
            mBluetoothGatt.writeDescriptor(descriptorWriteQueue.element());
        else if(readCharacteristicQueue.size() > 0)
            mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element());
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        readCharacteristicQueue.remove();
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (UUID.fromString(FIRMWARE_ID).equals(characteristic.getUuid())) {
                // Myo Firmware Infomation
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    ByteReader byteReader = new ByteReader();
                    byteReader.setByteData(data);

                    Log.d(TAG, String.format("This Version is %d.%d.%d - %d",
                            byteReader.getShort(), byteReader.getShort(),
                            byteReader.getShort(), byteReader.getShort()));

                }
                if (data == null) {
                    Log.d(TAG,"Characteristic String is " + characteristic.toString());
                }
            } else if (UUID.fromString(MYO_INFO_ID).equals(characteristic.getUuid())) {
                // Myo Device Information
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    ByteReader byteReader = new ByteReader();
                    byteReader.setByteData(data);

                    callback_msg = String.format("Serial Number     : %02x:%02x:%02x:%02x:%02x:%02x",
                            byteReader.getByte(), byteReader.getByte(), byteReader.getByte(),
                            byteReader.getByte(), byteReader.getByte(), byteReader.getByte()) +
                            '\n' + String.format("Unlock            : %d", byteReader.getShort()) +
                            '\n' + String.format("Classifier builtin:%d active:%d (have:%d)",
                            byteReader.getByte(), byteReader.getByte(), byteReader.getByte()) +
                            '\n' + String.format("Stream Type       : %d", byteReader.getByte());
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            dataView.setText(callback_msg);
//                        }
//                    });

                }
            }
        }
        else{
            Log.d(TAG, "onCharacteristicRead error: " + status);
        }

        if(readCharacteristicQueue.size() > 0)
            mBluetoothGatt.readCharacteristic(readCharacteristicQueue.element());
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onCharacteristicWrite success");
        } else {
            Log.d(TAG, "onCharacteristicWrite error: " + status);
        }
    }

    long last_send_never_sleep_time_ms = System.currentTimeMillis();
    //long pretime=0;
    final static long NEVER_SLEEP_SEND_TIME = 10000;  // Milli Second
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //Log.d("synchronized", "emg startList : " + list_emgStart.size() + " imu startList : " + list_imuStart.size() + " emg endList : " + list_emgEnd.size() + " imu endList : " + list_imuEnd.size());
        if (EMG_0_ID.equals(characteristic.getUuid().toString())) {
            long systemTime_ms = System.currentTimeMillis();
            /*byte[] */
            byte[] emg_data = characteristic.getValue();
            /////GestureDetectModelManager.getCurrentModel().event(systemTime_ms,emg_data);//GestureSaveModel use it!!
            /*Log.d("timeee","pre: "+pretime+" now: "+systemTime_ms);
            pretime=systemTime_ms;*/

            EmgData streamData = new EmgData(new EmgCharacteristicData(emg_data), timeManager, emgId + 1);//get current emgDataRows and then 16->8 in other class

            if(emgStreamCount >= EMG_WINDOW_LENGTH){
                list_emgWindow.removeFirst();
            }
            else{
                emgStreamCount++;
            }

            if(emgStreamCount > 1){
                streamData = emgLowPassFiliter(list_emg.getLast(), streamData, 0.2f);
            }

            if (emgId >= emgListLength) {//list_emg的維護
                list_emg.removeFirst();
            }

            synchronized (MainActivity.getEmgListLock()) {
                list_emg.add(streamData);
            }

            emgId ++;
            list_emgWindow.add(streamData);

            if (emgStreamCount == EMG_WINDOW_LENGTH){//5
                EmgData emgStreamingMaxData = list_emgWindow.getFirst();

                for(int i_window = 0; i_window < list_emgWindow.size(); i_window++){//window內的全部data
                    for (int i_element = 0; i_element < 8; i_element++) {
                        if (list_emgWindow.get(i_window).getElement(i_element) > emgStreamingMaxData.getElement(i_element)) {
                            emgStreamingMaxData.setElement(i_element, streamData.getElement(i_element));
                        }
                    }
                }

                double sum = 0.00, mean;

                for (int i = 0; i < 8; i++) {
                    sum = sum + emgStreamingMaxData.getElement(i);
                }
                mean = sum / 8;

                if (mean > EMG_START_THRESHOLD && !emgState) {//有意義的動作且是從靜態到動態(開始點)
                    emgState = true;
                    //list_emgStart.add(emgId);
                    Log.d("start", "emg notifiy");

                    int tempImuId = getImuId();
                    int tempMotiId = BleBroadcastReceiver.mLogActivity.getMotiId();

                    emgStartAdd(emgId);
                    imuStartAdd(tempImuId);
                    BleBroadcastReceiver.mLogActivity.motiStartAdd(tempMotiId);

                    //Log.d("SIZE", "emgStart : " + list_emgStart.size());

                    MainActivity.startCount ++;
                }
                else if (mean < EMG_END_THRESHOLD && emgState && !list_emgStart.isEmpty()) {//從動態到靜態(結束點) start特徵要有
                    emgState = false;
                    //list_emgEnd.add(emgId);
                    Log.d("end", "emg notifiy");

                    if (!list_emgStart.isEmpty()) {
                        int tempImuId = getImuId();
                        int tempMotiId = BleBroadcastReceiver.mLogActivity.getMotiId();

                        emgEndAdd(emgId);
                        imuEndAdd(tempImuId);
                        BleBroadcastReceiver.mLogActivity.motiEndAdd(tempMotiId);

                        //Log.d("SIZE", "emgEnd : " + list_emgEnd.size());

                        synchronized (this) {
                            endEmg = true;//who get end
                        }


                        Thread  t = new Thread(r);//由最後收集到的end發起thread
                        t.start();
                    }

                    MainActivity.endCount ++;
                }
            }
        }


        /********************************************************************************************************************/
        if (IMU_DATA_ID.equals(characteristic.getUuid().toString())){
            byte[] imu_data = characteristic.getValue();
            //Log.d("Mode","IMU : "+imu_data);
            ImuData streamData = new ImuData(new ImuCharacteristicData(imu_data), timeManager, imuId + 1);

            if (imuStreamCount >= IMU_WINDOW_LENGTH){
                list_imuWindow.removeFirst();
            }
            else {
                imuStreamCount++;
            }

            if (imuStreamCount > 1){
                streamData = imuLowPassFiliter(list_imu.getLast(), streamData, 0.2f);
            }

            if (imuId >= imuListLength) {//list_imu的維護
                list_imu.removeFirst();
            }

            synchronized (MainActivity.getImuListLock()) {
                list_imu.add(streamData);
            }

            imuId ++;
            list_imuWindow.add(streamData);

            if (imuStreamCount == IMU_WINDOW_LENGTH){
                double xMax = list_imuWindow.getFirst().getElement(4);
                double xMin = list_imuWindow.getFirst().getElement(4);
                double yMax = list_imuWindow.getFirst().getElement(5);
                double yMin = list_imuWindow.getFirst().getElement(5);
                double zMax = list_imuWindow.getFirst().getElement(6);
                double zMin = list_imuWindow.getFirst().getElement(6);

                for (ImuData aList_imu : list_imuWindow) {
                    //x變化量的max和min
                    if (aList_imu.getElement(4) > xMax) {
                        xMax = aList_imu.getElement(4);
                    }
                    else if (aList_imu.getElement(4) < xMin) {
                        xMin = aList_imu.getElement(4);
                    }
                    //y變化量的max和min
                    if (aList_imu.getElement(5) > yMax) {
                        yMax = aList_imu.getElement(5);
                    }
                    else if (aList_imu.getElement(5) < yMin) {
                        yMin = aList_imu.getElement(5);
                    }
                    //z變化量的max和min
                    if (aList_imu.getElement(6) > zMax) {
                        zMax = aList_imu.getElement(6);
                    }
                    else if (aList_imu.getElement(6) < zMin) {
                        zMin = aList_imu.getElement(6);
                    }
                }

                if (xMax - xMin > imuDifferenceThreshold || yMax - yMin > imuDifferenceThreshold || zMax - zMin > imuDifferenceThreshold) {//imu start
                    //list_imuStart.add(imuId);
                    Log.d("start", "imu notifiy");
                    imuState = true;
                    imuStreamCount = 0;
                    list_imuWindow.clear();

                    int tempEmgId = getEmgId();
                    int tempMotiId = BleBroadcastReceiver.mLogActivity.getMotiId();

                    imuStartAdd(imuId);
                    emgStartAdd(tempEmgId);
                    BleBroadcastReceiver.mLogActivity.motiStartAdd(tempMotiId);

                    //Log.d("SIZE", "imuStart : " + list_imuStart.size());

                    MainActivity.startCount ++;
                }

                if (xMax - xMin <= imuDifferenceThreshold && yMax - yMin <= imuDifferenceThreshold && zMax - zMin <= imuDifferenceThreshold && !list_imuStart.isEmpty() && imuState) {//imu end
                    //list_imuEnd.add(imuId);
                    Log.d("end", "imu notifiy");
                    imuState = false;

                    if (!list_imuStart.isEmpty()) {
                        int tempEmgId = getEmgId();
                        int tempMotiId = BleBroadcastReceiver.mLogActivity.getMotiId();

                        imuEndAdd(imuId);
                        emgEndAdd(tempEmgId);
                        BleBroadcastReceiver.mLogActivity.motiEndAdd(tempMotiId);

                        //Log.d("SIZE", "imuEnd : " + list_imuEnd.size());

                        synchronized (this) {
                            endImu = true;//who get end
                        }


                        Thread  t = new Thread(r);//由最後收集到的end發起thread
                        t.start();
                    }

                    MainActivity.endCount ++;
                }
            }
        }
    }

    public void emgStartAdd (int id) {
        synchronized (MainActivity.getEmgStartLock()) {
            list_emgStart.add(id);
        }
    }

    public void emgEndAdd (int id) {
        synchronized (MainActivity.getEmgEndLock()) {
            list_emgEnd.add(id);
        }
    }

    public void imuStartAdd (int id) {
        synchronized (MainActivity.getImuStartLock()) {
            list_imuStart.add(id);
        }
    }

    public void imuEndAdd (int id) {
        synchronized (MainActivity.getImuEndLock()) {
            list_imuEnd.add(id);
        }
    }

    public int getEmgId () {
        return emgId;
    }

    public int getImuId () {
        return imuId;
    }

    private EmgData emgLowPassFiliter( EmgData input, EmgData output ,double ALPHA) {
        for ( int i = 0; i < 8; i++ ){
            output.setElement(i, output.getElement(i) + ALPHA * (input.getElement(i) - output.getElement(i)));
        }

        return output;
    }

    private ImuData imuLowPassFiliter( ImuData input, ImuData output ,double ALPHA) {
        for ( int i = 0; i < 10; i++ ){
            output.setElement(i, output.getElement(i) + ALPHA * (input.getElement(i) - output.getElement(i)));
        }

        return output;
    }

    public void setBluetoothGatt(BluetoothGatt gatt) {
        mBluetoothGatt = gatt;
    }

    public boolean setMyoControlCommand(byte[] command) {
        if ( mCharacteristic_command != null) {
            mCharacteristic_command.setValue(command);
            int i_prop = mCharacteristic_command.getProperties();
            if (i_prop == BluetoothGattCharacteristic.PROPERTY_WRITE) {
                if (mBluetoothGatt.writeCharacteristic(mCharacteristic_command)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Runnable r = new Runnable() {

        @Override
        public void run() {
            if (endEmg) {//emg get a new end point
                synchronized (this) {
                    endEmg = false;
                }


                int startPoint, endPoint;
                synchronized (MainActivity.getEmgStartLock()) {
                    startPoint = list_emgStart.get(MainActivity.currentStart);
                }
                synchronized (MainActivity.getEmgEndLock()) {
                    endPoint = list_emgEnd.get(MainActivity.currentEnd);
                }
                Log.d("emgId", startPoint + ", " + endPoint);

                while (startPoint < endPoint) {//start 'id' < end 'id'
                    if (endPoint - startPoint > emgSegmentLength) {
                        int currentStart, currentEnd;

                        synchronized (MainActivity.getCurrentStartLock()) {
                            currentStart = MainActivity.currentStart;
                        }

                        synchronized (MainActivity.getCurrentEndLock()) {
                            currentEnd = MainActivity.currentEnd;
                        }
                        Log.d("DataCalculator", "emg calculator" + " start : " + startPoint + " end : " + endPoint);

                        DataCalculator.getCurrentDataCalculator().setList(list_emgStart, list_emgEnd, list_imuStart, list_imuEnd, BleBroadcastReceiver.mLogActivity.getList_motiStart(), BleBroadcastReceiver.mLogActivity.getList_motiEnd());
                        DataCalculator.getCurrentDataCalculator().startCalculating(list_emg, list_imu, BleBroadcastReceiver.mLogActivity.getList_moti(), currentStart, currentEnd);
                    }
                    //Log.d("DataCalculator", "emg calculator testtest1");
                    if (list_emgStart.size() > MainActivity.currentStart + 1) {
                        //Log.d("DataCalculator", "emg calculator ifif");
                        synchronized (MainActivity.getCurrentStartLock()) {
                            //Log.d("DataCalculator", "emg calculator synchronized ++ " + MainActivity.currentStart);
                            MainActivity.currentStart++;
                        }
                    }
                    else {
                        //Log.d("DataCalculator", "emg calculator break!!!");
                        break;
                    }
                    //Log.d("DataCalculator", "emg calculator testtest2");

                    synchronized (MainActivity.getEmgStartLock()) {
                        startPoint = list_emgStart.get(MainActivity.currentStart);
                        //Log.d("DataCalculator", "emg calculator" + " starttt : " + startPoint + " end : " + endPoint + " currentStart : " + MainActivity.currentStart);
                    }
                    synchronized (MainActivity.getEmgEndLock()) {
                        endPoint = list_emgEnd.get(MainActivity.currentEnd);
                        //Log.d("DataCalculator", "emg calculator" + " start : " + startPoint + " enddd : " + endPoint + " currentEnd : " + MainActivity.currentEnd);
                    }
                    //Log.d("DataCalculator", "emg calculator" + " starttt : " + startPoint + " enddd : " + endPoint);
                }
            }
            else if (endImu) {//imu get a new end point
                synchronized (this) {
                    endImu = false;
                }


                int startPoint, endPoint;
                synchronized (MainActivity.getImuStartLock()) {
                    startPoint = list_imuStart.get(MainActivity.currentStart);
                }
                synchronized (MainActivity.getImuEndLock()) {
                    endPoint = list_imuEnd.get(MainActivity.currentEnd);
                }
                //Log.d("imu id", startPoint + ", " + endPoint);

                while (startPoint < endPoint) {
                    if (endPoint - startPoint > imuSegmentLength) {
                        int currentStart, currentEnd;

                        synchronized (MainActivity.getCurrentStartLock()) {
                            currentStart = MainActivity.currentStart;
                        }

                        synchronized (MainActivity.getCurrentEndLock()) {
                            currentEnd = MainActivity.currentEnd;
                        }

                        Log.d("DataCalculator", "imu calculator" + " start : " + startPoint + " end : " + endPoint);

                        DataCalculator.getCurrentDataCalculator().setList(list_emgStart, list_emgEnd, list_imuStart, list_imuEnd, BleBroadcastReceiver.mLogActivity.getList_motiStart(), BleBroadcastReceiver.mLogActivity.getList_motiEnd());
                        DataCalculator.getCurrentDataCalculator().startCalculating(list_emg, list_imu, BleBroadcastReceiver.mLogActivity.getList_moti(), currentStart, currentEnd);
                    }
                    //Log.d("DataCalculator", "imu calculator testtest1");
                    if (list_imuStart.size() > MainActivity.currentStart + 1) {
                        //Log.d("DataCalculator", "emg calculator ifif");
                        synchronized (MainActivity.getCurrentStartLock()) {
                            //Log.d("DataCalculator", "emg calculator synchronized ++" + MainActivity.currentStart);
                            MainActivity.currentStart++;
                        }
                    }
                    else {
                        //Log.d("DataCalculator", "imu calculator break!!!");
                        break;
                    }

                    //Log.d("DataCalculator", "imu calculator testtest2");
                    synchronized (MainActivity.getImuStartLock()) {
                        startPoint = list_imuStart.get(MainActivity.currentStart);
                        //Log.d("DataCalculator", "imu calculator" + " starttt : " + startPoint + " end : " + endPoint + " currentStart : " + MainActivity.currentStart);
                    }
                    synchronized (MainActivity.getImuEndLock()) {
                        endPoint = list_imuEnd.get(MainActivity.currentEnd);
                        //Log.d("DataCalculator", "imu calculator" + " start : " + startPoint + " enddd : " + endPoint + " currentStart : " + MainActivity.currentEnd);
                    }
                    //Log.d("DataCalculator", "imu calculator" + " starttt : " + startPoint + " enddd : " + endPoint);
                }
            }

            synchronized (MainActivity.getCurrentEndLock()) {
                MainActivity.currentEnd++;
            }
        }
    };

    public void stopCallback() {
        // Before the closing GATT, set Myo [Normal Sleep Mode].
        setMyoControlCommand(commandList.sendNormalSleep());
        descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
        readCharacteristicQueue = new LinkedList<BluetoothGattCharacteristic>();
        if (mCharacteristic_command != null) {
            mCharacteristic_command = null;
        }
        if (mCharacteristic_emg0 != null) {
            mCharacteristic_emg0 = null;
        }
        if (mCharacteristic_imu != null) {
            mCharacteristic_imu = null;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt = null;
        }
    }
}
