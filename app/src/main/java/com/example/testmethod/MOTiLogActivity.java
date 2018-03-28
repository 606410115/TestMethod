package com.example.testmethod;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Mslab on 2017/7/9.
 */

public class MOTiLogActivity {

    private final static int MOTI_WINDOW_LENGTH = 30;
    private final static double motiDifferenceThreshold = 2;

    private final static int motiListLength = 1000000;

    private final static int motiSegmentLength = 30;

    private int motiStreamCount = 0;
    private boolean motiState = false;

    public String acc_x="",acc_y="",acc_z="",gyro_x="",gyro_y="",gyro_z=""/*,msgtime=""*/;
    private double[] mAccData=new double[3];
    private double[] mAccPreData=new double[3];
    private double[] mGyroData=new double[3];
    private double[] mGyroPreData=new double[3];
    private int mCount=0;

    private LinkedList<MOTiData> list_moti = new LinkedList<>();
    private LinkedList<MOTiData> list_motiWindow = new LinkedList<>();

    private ArrayList<Integer> list_motiStart = new ArrayList<>();
    private ArrayList<Integer> list_motiEnd = new ArrayList<>();

    private TimeManager timeManager;

    private int motiId = -1;

    //private boolean motiStart = false, motiEnd = false;
    private boolean endMoti = false;

    MOTiLogActivity(TimeManager tM){
        timeManager = tM;
    }

    public void init() {
        motiStreamCount = 0;
        motiState = false;
        list_moti = new LinkedList<>();
        list_motiWindow = new LinkedList<>();
        list_motiStart = new ArrayList<>();
        list_motiEnd = new ArrayList<>();
        motiId = -1;
        endMoti = false;
    }

    public int getMotiListLength () {
        return motiListLength;
    }


    public LinkedList<MOTiData> getList_moti () {
        //Log.d("moti", "moti size : " + list_moti.size());
        return list_moti;
    }

    public ArrayList<Integer> getList_motiStart () {
        //Log.d("moti", "motiStart size : " + list_motiStart.size());
        return list_motiStart;
    }

    public ArrayList<Integer> getList_motiEnd () {
        //Log.d("moti", "motiEnd size : " + list_motiEnd.size());
        return list_motiEnd;
    }

    public void addLogData(byte[] byteArrayExtra, String name){
        if(byteArrayExtra.length == 18) {
            MOTiData streamData = new MOTiData(new MOTiCharacteristicData(byteArrayExtra), timeManager, motiId + 1);

            if(motiStreamCount >= MOTI_WINDOW_LENGTH){
                list_motiWindow.removeFirst();
            }
            else{
                motiStreamCount++;
            }

            if(motiStreamCount > 1){
                streamData = motiLowPassFiliter(list_moti.getLast(), streamData, 0.2f);
            }

            if (motiId >= motiListLength) {//list_moti的維護
                list_moti.removeFirst();
            }

            synchronized (MainActivity.getMotiListLock()) {
                list_moti.add(streamData);
            }

            motiId ++;

            list_motiWindow.add(streamData);
            if (motiStreamCount == MOTI_WINDOW_LENGTH){
                double xMax = list_motiWindow.getFirst().getElement(0);
                double xMin = list_motiWindow.getFirst().getElement(0);
                double yMax = list_motiWindow.getFirst().getElement(1);
                double yMin = list_motiWindow.getFirst().getElement(1);
                double zMax = list_motiWindow.getFirst().getElement(2);
                double zMin = list_motiWindow.getFirst().getElement(2);

                for (MOTiData aList_moti : list_motiWindow) {
                    //x變化量的max和min
                    if (aList_moti.getElement(0) > xMax) {
                        xMax = aList_moti.getElement(0);
                    }
                    else if (aList_moti.getElement(0) < xMin) {
                        xMin = aList_moti.getElement(0);
                    }
                    //y變化量的max和min
                    if (aList_moti.getElement(1) > yMax) {
                        yMax = aList_moti.getElement(1);
                    }
                    else if (aList_moti.getElement(1) < yMin) {
                        yMin = aList_moti.getElement(1);
                    }
                    //z變化量的max和min
                    if (aList_moti.getElement(2) > zMax) {
                        zMax = aList_moti.getElement(2);
                    }
                    else if (aList_moti.getElement(2) < zMin) {
                        zMin = aList_moti.getElement(2);
                    }
                }


                if (xMax - xMin > motiDifferenceThreshold || yMax - yMin > motiDifferenceThreshold || zMax - zMin > motiDifferenceThreshold) {//moti start
                    //list_motiStart.add(motiId);
                    Log.d("start", "moti notify");
                    motiStreamCount = 0;
                    motiState = true;

                    list_motiWindow.clear();

                    int tempEmgId = MainActivity.mMyoCallback.getEmgId();
                    int tempImuId = MainActivity.mMyoCallback.getImuId();

                    motiStartAdd(motiId);
                    MainActivity.mMyoCallback.emgStartAdd(tempEmgId);
                    MainActivity.mMyoCallback.imuStartAdd(tempImuId);

                    //Log.d("SIZE", "motiStart : " + list_motiStart.size());

                    MainActivity.startCount ++;
                }

                if (xMax - xMin <= motiDifferenceThreshold && yMax - yMin <= motiDifferenceThreshold && zMax - zMin <= motiDifferenceThreshold && !list_motiStart.isEmpty() && motiState) {//moti end
                    //list_motiEnd.add(motiId);
                    Log.d("end", "moti notify");
                    motiState = false;

                    if (!list_motiStart.isEmpty()) {
                        int tempEmgId = MainActivity.mMyoCallback.getEmgId();
                        int tempImuId = MainActivity.mMyoCallback.getImuId();

                        motiEndAdd(motiId);
                        MainActivity.mMyoCallback.emgEndAdd(tempEmgId);
                        MainActivity.mMyoCallback.imuEndAdd(tempImuId);

                        //Log.d("SIZE", "motiEnd : " + list_motiEnd.size());

                        endMoti = true;

                        Thread  t = new Thread(r);//由最後收集到的end發起thread
                        t.start();
                    }

                    MainActivity.endCount ++;
                }
            }
        }
    }

    public void motiStartAdd (int id) {
        synchronized (MainActivity.getMotiStartLock()) {
            list_motiStart.add(id);
        }
    }

    public void motiEndAdd (int id) {
        synchronized (MainActivity.getMotiEndLock()) {
            list_motiEnd.add(id);
        }
    }

    public int getMotiId () {
        return motiId;
    }

    private Runnable r = new Runnable() {

        @Override
        public void run() {
            if (endMoti) {//emg get a new end point
                endMoti = false;

                int startPoint, endPoint;
                synchronized (MainActivity.getMotiStartLock()) {
                    startPoint = list_motiStart.get(MainActivity.currentStart);
                }
                synchronized (MainActivity.getMotiEndLock()) {
                    endPoint = list_motiEnd.get(MainActivity.currentEnd);
                }
                Log.d("motiId", startPoint + ", " + endPoint);

                while (startPoint < endPoint) {//start 'id' < end 'id'
                    if (endPoint - startPoint > motiSegmentLength) {
                        int currentStart, currentEnd;

                        synchronized (MainActivity.getCurrentStartLock()) {
                            currentStart = MainActivity.currentStart;
                        }

                        synchronized (MainActivity.getCurrentEndLock()) {
                            currentEnd = MainActivity.currentEnd;
                        }
                        Log.d("DataCalculator", "moti calculator" + " start : " + startPoint + " end : " + endPoint);
                        DataCalculator.getCurrentDataCalculator().setList(MainActivity.mMyoCallback.getList_emgStart(), MainActivity.mMyoCallback.getList_emgEnd(), MainActivity.mMyoCallback.getList_imuStart(), MainActivity.mMyoCallback.getList_imuEnd(), list_motiStart, list_motiEnd);
                        DataCalculator.getCurrentDataCalculator().startCalculating(MainActivity.mMyoCallback.getList_emg(), MainActivity.mMyoCallback.getList_imu(), list_moti, currentStart, currentEnd);
                    }

                    if (list_motiStart.size() > MainActivity.currentStart + 1) {
                        synchronized (MainActivity.getCurrentStartLock()) {
                            MainActivity.currentStart++;
                        }
                    }
                    else {
                        break;
                    }

                    synchronized (MainActivity.getMotiStartLock()) {
                        startPoint = list_motiStart.get(MainActivity.currentStart);
                    }
                    synchronized (MainActivity.getMotiEndLock()) {
                        endPoint = list_motiEnd.get(MainActivity.currentEnd);
                    }
                }
            }

            synchronized (MainActivity.getCurrentEndLock()) {
                MainActivity.currentEnd++;
            }
        }
    };

    private MOTiData motiLowPassFiliter( MOTiData input, MOTiData output ,double ALPHA) {
        for ( int i = 0; i < 6; i++ ){
            output.setElement(i, output.getElement(i) + ALPHA * (input.getElement(i) - output.getElement(i)));
        }

        return output;
    }
}
