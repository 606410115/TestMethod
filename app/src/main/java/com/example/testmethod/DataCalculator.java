package com.example.testmethod;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Mslab on 2017/12/8.
 */

public class DataCalculator {
    private static DataCalculator currentDataCalculator = null;

    private ArrayList<Integer> list_emgStart, list_emgEnd, list_imuStart, list_imuEnd, list_motiStart, list_motiEnd;

    private ArrayList<EmgData> list_emg = new ArrayList<>();
    private ArrayList<ImuData> list_imu = new ArrayList<>();
    private ArrayList<MOTiData> list_moti = new ArrayList<>();

    private String emgDataString = "";
    private String imuDataString = "";
    private String motiDataString = "";

    private String emgNumber = "";
    private String imuNumber = "";
    private String motiNumber = "";

    private String emgStartString = "";
    private String imuStartString = "";
    private String motiStartString = "";
    private String emgEndString = "";
    private String imuEndString = "";
    private String motiEndString = "";

    public static DataCalculator getCurrentDataCalculator(){
        if(currentDataCalculator == null){//只有在第一次建立實例時才會進入同步區，之後由於實例已建立，也就不用進入同步區進行鎖定。
            synchronized (DataCalculator.class){
                if(currentDataCalculator == null){
                    currentDataCalculator = new DataCalculator();
                }
            }
        }

        return currentDataCalculator;
    }

    public void init() {
        list_emgStart = null;
        list_emgEnd = null;
        list_imuStart = null;
        list_imuEnd = null;
        list_motiStart = null;
        list_motiEnd=  null;
        list_emg.clear();
        list_imu.clear();
        list_moti.clear();
    }

    public String getEmgStartString () {
        return emgStartString;
    }
    public String getImuStartString () {
        return imuStartString;
    }
    public String getMotiStartString () {
        return motiStartString;
    }
    public String getEmgEndString () {
        return emgEndString;
    }
    public String getImuEndString () {
        return imuEndString;
    }
    public String getMotiEndString () {
        return motiEndString;
    }

    public String getEmgDataString () {
        return emgDataString;
    }
    public String getImuDataString () {
        return imuDataString;
    }
    public String getMotiDataString () {
        return motiDataString;
    }
    public String getEmgNumber () {
        return emgNumber;
    }
    public String getImuNumber () {
        return imuNumber;
    }
    public String getMotiNumber() {
        return motiNumber;
    }

    synchronized public void setList(ArrayList<Integer> emgStart, ArrayList<Integer> emgEnd,ArrayList<Integer> imuStart,ArrayList<Integer> imuEnd,ArrayList<Integer> motiStart,ArrayList<Integer> motiEnd) {
        //先試試看只把指標放上去看會不會有同步問題，如果有再用迴圈複製
        list_emgStart = emgStart;
        list_emgEnd = emgEnd;
        list_imuStart = imuStart;
        list_imuEnd = imuEnd;
        list_motiStart = motiStart;
        list_motiEnd = motiEnd;
    }

    synchronized public void startCalculating(LinkedList<EmgData> emgList, LinkedList<ImuData> imuList, LinkedList<MOTiData> motiList, int currentStart, int currentEnd) {
        int emgStartPoint, emgEndPoint, imuStartPoint, imuEndPoint, motiStartPoint, motiEndPoint;

        synchronized (MainActivity.getEmgStartLock()) {
            emgStartPoint = list_emgStart.get(currentStart);
            emgStartString = emgStartString + emgStartPoint + "\n";
        }
        synchronized (MainActivity.getEmgEndLock()) {
            emgEndPoint = list_emgEnd.get(currentEnd);
            emgEndString = emgEndString + emgEndPoint + "\n";
        }
        synchronized (MainActivity.getImuStartLock()) {
            imuStartPoint = list_imuStart.get(currentStart);
            imuStartString = imuStartString + imuStartPoint + "\n";
        }
        synchronized (MainActivity.getImuEndLock()) {
            imuEndPoint = list_imuEnd.get(currentEnd);
            imuEndString = imuEndString + imuEndPoint + "\n";
        }
        synchronized (MainActivity.getMotiStartLock()) {
            motiStartPoint = list_motiStart.get(currentStart);
            motiStartString = motiStartString + motiStartPoint + "\n";
        }
        synchronized (MainActivity.getMotiEndLock()) {
            motiEndPoint = list_motiEnd.get(currentEnd);
            motiEndString = motiEndString + motiEndPoint + "\n";
        }

        synchronized (MainActivity.getEmgListLock()) {
            Log.d("ID", "emg currentStart : " + emgStartPoint + " currentEnd : " + emgEndPoint);
            list_emg.clear();
            for (int i = emgStartPoint; i < emgEndPoint; i++) {
                //Log.d("emgList.get(i)", "" + emgList.get(i));
                emgNumber = emgNumber + i + "\n";
                list_emg.add(emgList.get(i));
                //Log.d("emg", "add!!! i : " + i);
            }
            emgNumber = emgNumber + "\n*****\n";
            Log.d("BUGGSIZE", "emg : " + list_emg.size());
        }

        synchronized (MainActivity.getImuListLock()) {
            Log.d("ID", "imu currentStart : " + imuStartPoint + " currentEnd : " + imuEndPoint);
            list_imu.clear();
            for (int j = imuStartPoint; j < imuEndPoint; j++) {
                //Log.d("imuList.get(j)", "" + imuList.get(j));
                imuNumber = imuNumber + j + "\n";
                list_imu.add(imuList.get(j));
                //Log.d("imu", "add!!! i : " + j);
            }
            imuNumber = imuNumber + "\n*****\n";
            Log.d("BUGGSIZE", "imu : " + list_imu.size());
        }

        synchronized (MainActivity.getMotiListLock()) {
            Log.d("ID", "moti currentStart : " + motiStartPoint + " currentEnd : " + motiEndPoint);
            list_moti.clear();
            for (int k = motiStartPoint; k < motiEndPoint; k++) {
                //Log.d("motiList.get(k)", "" + motiList.get(k));
                motiNumber = motiNumber + k + "\n";
                list_moti.add(motiList.get(k));
                //Log.d("moti", "add!!! i : " + k);
            }
            motiNumber = motiNumber + "\n*****\n";
            Log.d("BUGGSIZE", "moti : " + list_moti.size());
        }

        Thread tEmg = new Thread(rEmg);
        tEmg.start();

        Thread tImg = new Thread(rImu);
        tImg.start();

        Thread tMoti = new Thread(rMoti);
        tMoti.start();

        /*rEmgFunction();
        rImuFunction();
        rMotiFunction();
        Classify.getCurrentClassify().WekaKNN();*/

    }

    private Runnable rEmg = new Runnable() {
        @Override
        public void run() {
            Log.d("MyoEMG", "emg thread");
            ArrayList<EmgData> emg_motion = new ArrayList<>();
            ArrayList<Double> feature = new ArrayList<>();

            //emg_motion = list_emg;

            /*synchronized (this) {
                for (EmgData aList_emg : list_emg) {
                    emg_motion.add(aList_emg);
                }

                list_emg.clear();
            }*/

            synchronized (MainActivity.getEmgListLock()) {
                Log.d("BUGSiZE", "emg : " + list_emg.size());
            }
            synchronized (MainActivity.getEmgListLock()) {
                for (int i = 0; i < list_emg.size(); i ++) {
                    //assert list_emg.get(i) != null;
                    emg_motion.add(list_emg.get(i));
                }
                Log.d("BUGG", "emg_motion size : " + emg_motion.size());
                //list_emg.clear();
            }

            Log.d("BUG", "emg_motion size : " + emg_motion.size());

            //normalize emg
            /*for (EmgData aList_emg : emg_motion) {
                //assert aList_emg != null;
                for (int i_emg8 = 0; i_emg8 < 8; i_emg8++) {
                    aList_emg.setElement(i_emg8, aList_emg.getElement(i_emg8) / 256);
                }
            }*/
            synchronized (this) {
                for (EmgData aList_emg : emg_motion) {
                    for (int i_emg8 = 0; i_emg8 < 8; i_emg8++) {
                        emgDataString = emgDataString + aList_emg.getElement(i_emg8) + "\t";
                    }
                    emgDataString = emgDataString + "\n";
                }
                emgDataString = emgDataString + "\n****************************************\n";
            }


            //emg 每個sensor的平均值特徵(8 features)
            for(int j_sensor = 0; j_sensor < 8; j_sensor++){//MYO EMG的哪個sensor
                double sum =0.00, mean;

                for(int i_element = 0; i_element < emg_motion.size(); i_element++){//蒐集的數量
                    sum = sum + (emg_motion.get(i_element).getElement(j_sensor) / 256);
                }

                mean = sum / emg_motion.size();
                Log.d("Myo", "emg_mean : " + mean);
//                double normalize_mean = mean / 256;
//                Log.d("MYO_normalize", "EMG normalize_mean: " + normalize_mean);
//                feature.add(normalize_mean);

                feature.add(mean);
            }
            Classify.getCurrentClassify().emgList(feature);
            Classify.getCurrentClassify().WekaKNN();
        }
    };

    private Runnable rImu = new Runnable() {
        @Override
        public void run() {
            Log.d("MyoIMU", "imu thread");
            ArrayList<ImuData> imu_motion = new ArrayList<>();
            ArrayList<Double> feature = new ArrayList<>();

            double[] acc_mean = new double[3];


            //imu_motion = list_imu;

            /*synchronized (this) {
                for (ImuData aList_imu : list_imu) {
                    imu_motion.add(aList_imu);
                }

                list_imu.clear();
            }*/

            synchronized (MainActivity.getImuListLock()) {
                Log.d("BUGSiZE", "imu : " + list_imu.size());
            }
            synchronized (MainActivity.getImuListLock()) {
                for (int i = 0; i < list_imu.size(); i ++) {
                    //assert list_imu.get(i) != null;
                    imu_motion.add(list_imu.get(i));
                }
                Log.d("BUGG", "imu_motion size : " + imu_motion.size());
                //list_imu.clear();
            }

            Log.d("BUG", "imu_motion size : " + imu_motion.size());

            //normalize imu
            /*for (ImuData aList_imu : imu_motion) {
                //assert aList_imu != null;
                for (int i_imu_num = 0; i_imu_num < 10; i_imu_num++) {
                    if (i_imu_num < 4) {//quaternion
                        aList_imu.setElement(i_imu_num, aList_imu.getElement(i_imu_num) / 65536);
                    }
                    else if (i_imu_num >= 4 && i_imu_num < 7) {//accelerometer
                        aList_imu.setElement(i_imu_num, (aList_imu.getElement(i_imu_num) + 156.8) / 313.6);
                    }
                    else if (i_imu_num >= 7) {//gyroscope
                        aList_imu.setElement(i_imu_num, (aList_imu.getElement(i_imu_num) + 2000) / 4000);
                    }
                }
            }*/
            synchronized (this) {
                for (ImuData aList_imu : imu_motion) {
                    for (int i_imu_num = 0; i_imu_num < 10; i_imu_num++) {
                        if (i_imu_num >= 4 && i_imu_num < 7) {//accelerometer
                            imuDataString = imuDataString + aList_imu.getElement(i_imu_num) + "\t";
                        }
                        /*else if (i_imu_num < 4) {//quaternion
                            imuDataString = imuDataString + aList_imu.getElement(i_imu_num) + "\t";
                        }*/
                    }
                    imuDataString = imuDataString + "\n";
                }
                imuDataString = imuDataString + "\n****************************************\n";
            }

            //acc 每軸平均值(3 features) => feature[0~2]
            for(int i_axis = 4; i_axis < 7; i_axis++){//IMU的ACC
                double sum = 0.00, mean;

                for(int i_element = 0; i_element < imu_motion.size(); i_element++){
                    sum = sum + ((imu_motion.get(i_element).getElement(i_axis) + 156.8) / 313.6);
                }

                mean = sum / imu_motion.size();
                Log.d("Myo", "Imu_mean : " + mean);
//                double normalize_mean = (mean + 156.8) / 313.6;
//                Log.d("MYO_normalize", "IMU normalize_mean: " + normalize_mean);
//                feature.add(normalize_mean);

                feature.add(mean);

                switch (i_axis){
                    case 4:
                        acc_mean[0] = mean;//x mean
                        break;
                    case 5:
                        acc_mean[1] = mean;//y mean
                        break;
                    case 6:
                        acc_mean[2] = mean;//z mean
                        break;
                }
            }
            //acc 每軸標準差(3 features) => feature[3~5]
            for(int i_axis = 4; i_axis < 7; i_axis++){
                double SD_sum = 0.00, SD;

                for(int i_element = 0; i_element < imu_motion.size(); i_element++){
                    SD_sum = SD_sum + Math.pow( ((imu_motion.get(i_element).getElement(i_axis) + 156.8) / 313.6) - acc_mean[i_axis - 4] , 2);
                }

                SD = Math.sqrt(SD_sum / imu_motion.size());
                Log.d("Myo", "Imu_SD : " + SD);
//                double normalize_SD = SD / 24586.24;
//                Log.d("MYO_normalize", "IMU normalize_SD: " + normalize_SD);
//                feature.add(normalize_SD);

                feature.add(SD);
            }

            Classify.getCurrentClassify().imuList(feature);
            Classify.getCurrentClassify().WekaKNN();
        }
    };

    private Runnable rMoti = new Runnable() {
        @Override
        public void run() {
            Log.d("MOTi", "moti thread");
            ArrayList<MOTiData> moti_motion = new ArrayList<>();
            ArrayList<Double> feature = new ArrayList<>();

            double[] acc_mean = new double[3];


            //moti_motion = list_moti;

            /*synchronized (this) {
                for (MOTiData aList_moti : list_moti) {
                    moti_motion.add(aList_moti);
                }

                list_moti.clear();
            }*/

            synchronized (MainActivity.getMotiListLock()) {
                Log.d("BUGSiZE", "moti : " + list_moti.size());
            }
            synchronized (MainActivity.getMotiListLock()) {
                for (int i = 0; i < list_moti.size(); i ++) {
                    //assert list_moti.get(i) != null;
                    moti_motion.add(list_moti.get(i));
                }
                Log.d("BUGG", "moti_motion size : " + moti_motion.size());
                //list_moti.clear();
            }

            Log.d("BUG", "moti_motion size : " + moti_motion.size());

            //normalize moti
            /*for (MOTiData aList_moti : moti_motion) {
                //assert aList_moti != null;
                for (int i_moti_num = 0; i_moti_num < 6; i_moti_num++) {
                    if (i_moti_num < 3) {//accelerometer
                        aList_moti.setElement(i_moti_num, (aList_moti.getElement(i_moti_num) + 78.4) / 156.8);
                    }
                    else if (i_moti_num >= 3) {//gyroscope
                        aList_moti.setElement(i_moti_num, (aList_moti.getElement(i_moti_num) + 500) / 1000);
                    }
                }
            }*/
            synchronized (this) {
                for (MOTiData aList_moti : moti_motion) {
                    for (int i_moti_num = 0; i_moti_num < 10; i_moti_num++) {
                        if (i_moti_num < 3) {//accelerometer
                            motiDataString = motiDataString + aList_moti.getElement(i_moti_num) + "\t";
                        }
                    }
                    motiDataString = motiDataString + "\n";
                }
                motiDataString = motiDataString + "\n****************************************\n";
            }

            //acc 每軸平均值(3 features) => feature[0~2]
            for(int i_axis = 0; i_axis < 3; i_axis++){//MOTi的ACC
                double sum = 0.00, mean;

                for(int i_element = 0; i_element < moti_motion.size(); i_element++){
                    sum = sum + ((moti_motion.get(i_element).getElement(i_axis) + 78.4) / 156.8);//normalize acc
                }

                mean = sum / moti_motion.size();
                Log.d("MOTi", "mean : " + mean);

//                double normalize_mean = (mean + 78.4) / 156.8;
//                Log.d("MOTi_normalize", "normalize_mean: " + normalize_mean);
//                feature.add(normalize_mean);
                feature.add(mean);

                acc_mean[i_axis] = mean;
            }
            //acc 每軸標準差(3 features) => feature[3~5]
            for(int i_axis = 0; i_axis < 3; i_axis++){
                double SD_sum = 0.00, SD;

                for(int i_element = 0; i_element < moti_motion.size(); i_element++){
                    SD_sum = SD_sum + Math.pow( ((moti_motion.get(i_element).getElement(i_axis) + 78.4) / 156.8) - acc_mean[i_axis] , 2);
                }

                SD = Math.sqrt(SD_sum / moti_motion.size());
                Log.d("MOTi", "SD : " + SD);

//                double normalize_SD = SD / 6146.56;
//                Log.d("MOTi_normalize", "normalize_SD: " + normalize_SD);
                feature.add(SD);
            }

            Classify.getCurrentClassify().motiList(feature);
            Classify.getCurrentClassify().WekaKNN();
        }
    };


    synchronized private void rEmgFunction () {
        Log.d("MyoEMG", "emg thread");
        ArrayList<EmgData> emg_motion = new ArrayList<>();
        ArrayList<Double> feature = new ArrayList<>();

        //emg_motion = list_emg;

            /*synchronized (this) {
                for (EmgData aList_emg : list_emg) {
                    emg_motion.add(aList_emg);
                }

                list_emg.clear();
            }*/

        synchronized (MainActivity.getEmgListLock()) {
            Log.d("BUGSiZE", "emg : " + list_emg.size());
        }
        synchronized (MainActivity.getEmgListLock()) {
            for (int i = 0; i < list_emg.size(); i ++) {
                //assert list_emg.get(i) != null;
                emg_motion.add(list_emg.get(i));
            }
            Log.d("BUGG", "emg_motion size : " + emg_motion.size());
            //list_emg.clear();
        }

        Log.d("BUG", "emg_motion size : " + emg_motion.size());

        //normalize emg
            /*for (EmgData aList_emg : emg_motion) {
                //assert aList_emg != null;
                for (int i_emg8 = 0; i_emg8 < 8; i_emg8++) {
                    aList_emg.setElement(i_emg8, aList_emg.getElement(i_emg8) / 256);
                }
            }*/
        synchronized (this) {
            for (EmgData aList_emg : emg_motion) {
                for (int i_emg8 = 0; i_emg8 < 8; i_emg8++) {
                    emgDataString = emgDataString + aList_emg.getElement(i_emg8) + "\t";
                }
                emgDataString = emgDataString + "\n";
            }
            emgDataString = emgDataString + "\n****************************************\n";
        }


        //emg 每個sensor的平均值特徵(8 features)
        for(int j_sensor = 0; j_sensor < 8; j_sensor++){//MYO EMG的哪個sensor
            double sum =0.00, mean;

            for(int i_element = 0; i_element < emg_motion.size(); i_element++){//蒐集的數量
                sum = sum + (emg_motion.get(i_element).getElement(j_sensor) / 256);
            }

            mean = sum / emg_motion.size();
            Log.d("Myo", "emg_mean : " + mean);
//                double normalize_mean = mean / 256;
//                Log.d("MYO_normalize", "EMG normalize_mean: " + normalize_mean);
//                feature.add(normalize_mean);

            feature.add(mean);
        }
        Classify.getCurrentClassify().emgList(feature);
        //Classify.getCurrentClassify().WekaKNN();
    }

    synchronized private void rImuFunction () {
        Log.d("MyoIMU", "imu thread");
        ArrayList<ImuData> imu_motion = new ArrayList<>();
        ArrayList<Double> feature = new ArrayList<>();

        double[] acc_mean = new double[3];


        //imu_motion = list_imu;

            /*synchronized (this) {
                for (ImuData aList_imu : list_imu) {
                    imu_motion.add(aList_imu);
                }

                list_imu.clear();
            }*/

        synchronized (MainActivity.getImuListLock()) {
            Log.d("BUGSiZE", "imu : " + list_imu.size());
        }
        synchronized (MainActivity.getImuListLock()) {
            for (int i = 0; i < list_imu.size(); i ++) {
                //assert list_imu.get(i) != null;
                imu_motion.add(list_imu.get(i));
            }
            Log.d("BUGG", "imu_motion size : " + imu_motion.size());
            //list_imu.clear();
        }

        Log.d("BUG", "imu_motion size : " + imu_motion.size());

        //normalize imu
            /*for (ImuData aList_imu : imu_motion) {
                //assert aList_imu != null;
                for (int i_imu_num = 0; i_imu_num < 10; i_imu_num++) {
                    if (i_imu_num < 4) {//quaternion
                        aList_imu.setElement(i_imu_num, aList_imu.getElement(i_imu_num) / 65536);
                    }
                    else if (i_imu_num >= 4 && i_imu_num < 7) {//accelerometer
                        aList_imu.setElement(i_imu_num, (aList_imu.getElement(i_imu_num) + 156.8) / 313.6);
                    }
                    else if (i_imu_num >= 7) {//gyroscope
                        aList_imu.setElement(i_imu_num, (aList_imu.getElement(i_imu_num) + 2000) / 4000);
                    }
                }
            }*/
        synchronized (this) {
            for (ImuData aList_imu : imu_motion) {
                for (int i_imu_num = 0; i_imu_num < 10; i_imu_num++) {
                    if (i_imu_num >= 4 && i_imu_num < 7) {//accelerometer
                        imuDataString = imuDataString + aList_imu.getElement(i_imu_num) + "\t";
                    }
                }
                imuDataString = imuDataString + "\n";
            }
            imuDataString = imuDataString + "\n****************************************\n";
        }

        //acc 每軸平均值(3 features) => feature[0~2]
        for(int i_axis = 4; i_axis < 7; i_axis++){//IMU的ACC
            double sum = 0.00, mean;

            for(int i_element = 0; i_element < imu_motion.size(); i_element++){
                sum = sum + ((imu_motion.get(i_element).getElement(i_axis) + 156.8) / 313.6);
            }

            mean = sum / imu_motion.size();
            Log.d("Myo", "Imu_mean : " + mean);
//                double normalize_mean = (mean + 156.8) / 313.6;
//                Log.d("MYO_normalize", "IMU normalize_mean: " + normalize_mean);
//                feature.add(normalize_mean);

            feature.add(mean);

            switch (i_axis){
                case 4:
                    acc_mean[0] = mean;//x mean
                    break;
                case 5:
                    acc_mean[1] = mean;//y mean
                    break;
                case 6:
                    acc_mean[2] = mean;//z mean
                    break;
            }
        }
        //acc 每軸標準差(3 features) => feature[3~5]
        for(int i_axis = 4; i_axis < 7; i_axis++){
            double SD_sum = 0.00, SD;

            for(int i_element = 0; i_element < imu_motion.size(); i_element++){
                SD_sum = SD_sum + Math.pow( ((imu_motion.get(i_element).getElement(i_axis) + 156.8) / 313.6) - acc_mean[i_axis - 4] , 2);
            }

            SD = Math.sqrt(SD_sum / imu_motion.size());
            Log.d("Myo", "Imu_SD : " + SD);
//                double normalize_SD = SD / 24586.24;
//                Log.d("MYO_normalize", "IMU normalize_SD: " + normalize_SD);
//                feature.add(normalize_SD);

            feature.add(SD);
        }

        Classify.getCurrentClassify().imuList(feature);
        //Classify.getCurrentClassify().WekaKNN();
    }

    synchronized private void rMotiFunction () {
        Log.d("MOTi", "moti thread");
        ArrayList<MOTiData> moti_motion = new ArrayList<>();
        ArrayList<Double> feature = new ArrayList<>();

        double[] acc_mean = new double[3];


        //moti_motion = list_moti;

            /*synchronized (this) {
                for (MOTiData aList_moti : list_moti) {
                    moti_motion.add(aList_moti);
                }

                list_moti.clear();
            }*/

        synchronized (MainActivity.getMotiListLock()) {
            Log.d("BUGSiZE", "moti : " + list_moti.size());
        }
        synchronized (MainActivity.getMotiListLock()) {
            for (int i = 0; i < list_moti.size(); i ++) {
                //assert list_moti.get(i) != null;
                moti_motion.add(list_moti.get(i));
            }
            Log.d("BUGG", "moti_motion size : " + moti_motion.size());
            //list_moti.clear();
        }

        Log.d("BUG", "moti_motion size : " + moti_motion.size());

        //normalize moti
            /*for (MOTiData aList_moti : moti_motion) {
                //assert aList_moti != null;
                for (int i_moti_num = 0; i_moti_num < 6; i_moti_num++) {
                    if (i_moti_num < 3) {//accelerometer
                        aList_moti.setElement(i_moti_num, (aList_moti.getElement(i_moti_num) + 78.4) / 156.8);
                    }
                    else if (i_moti_num >= 3) {//gyroscope
                        aList_moti.setElement(i_moti_num, (aList_moti.getElement(i_moti_num) + 500) / 1000);
                    }
                }
            }*/
        synchronized (this) {
            for (MOTiData aList_moti : moti_motion) {
                for (int i_moti_num = 0; i_moti_num < 10; i_moti_num++) {
                    if (i_moti_num < 3) {//accelerometer
                        motiDataString = motiDataString + aList_moti.getElement(i_moti_num) + "\t";
                    }
                }
                motiDataString = motiDataString + "\n";
            }
            motiDataString = motiDataString + "\n****************************************\n";
        }

        //acc 每軸平均值(3 features) => feature[0~2]
        for(int i_axis = 0; i_axis < 3; i_axis++){//MOTi的ACC
            double sum = 0.00, mean;

            for(int i_element = 0; i_element < moti_motion.size(); i_element++){
                sum = sum + ((moti_motion.get(i_element).getElement(i_axis) + 78.4) / 156.8);//normalize acc
            }

            mean = sum / moti_motion.size();
            Log.d("MOTi", "mean : " + mean);

//                double normalize_mean = (mean + 78.4) / 156.8;
//                Log.d("MOTi_normalize", "normalize_mean: " + normalize_mean);
//                feature.add(normalize_mean);
            feature.add(mean);

            acc_mean[i_axis] = mean;
        }
        //acc 每軸標準差(3 features) => feature[3~5]
        for(int i_axis = 0; i_axis < 3; i_axis++){
            double SD_sum = 0.00, SD;

            for(int i_element = 0; i_element < moti_motion.size(); i_element++){
                SD_sum = SD_sum + Math.pow( ((moti_motion.get(i_element).getElement(i_axis) + 78.4) / 156.8) - acc_mean[i_axis] , 2);
            }

            SD = Math.sqrt(SD_sum / moti_motion.size());
            Log.d("MOTi", "SD : " + SD);

//                double normalize_SD = SD / 6146.56;
//                Log.d("MOTi_normalize", "normalize_SD: " + normalize_SD);
            feature.add(SD);
        }

        Classify.getCurrentClassify().motiList(feature);
        //Classify.getCurrentClassify().WekaKNN();
    }
}
