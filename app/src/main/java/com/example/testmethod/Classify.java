package com.example.testmethod;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created by Mslab on 2017/10/10.
 */

public class Classify {
    private static Classify currentClassify = null;

    private ArrayList<ArrayList<Double>> myoEmgFeatureList = new ArrayList<>();
    private ArrayList<ArrayList<Double>> myoImuFeatureList = new ArrayList<>();
    private ArrayList<ArrayList<Double>> motiImuFeatureList = new ArrayList<>();

    //private boolean Myo_EMG = false, Myo_IMU = false, MOTi = false;

    private TextView textView;
    //count motion textView
    private TextView countText1;
    private TextView countText2;
    private TextView countText3;
    private TextView countText4;
    private TextView countText5;
    private TextView countText6;

    private Activity activity;

    private String result, rightHandMovement, rightHandStatus, leftHandMovement;

    private String classifyResult = "";

    public static Classify getCurrentClassify(){
        if(currentClassify == null){//只有在第一次建立實例時才會進入同步區，之後由於實例已建立，也就不用進入同步區進行鎖定。
            synchronized(Classify.class){
                if(currentClassify == null){
                    currentClassify = new Classify();
                }
            }

        }

        return currentClassify;
    }

    public void init() {
        myoEmgFeatureList.clear();
        myoImuFeatureList.clear();
        motiImuFeatureList.clear();
    }

    public String getClassifyResult () {
        return classifyResult;
    }

    public void setTextView(HashMap<String,View> views){
        textView = (TextView) views.get("result");

        countText1= (TextView) views.get("motion1");
        countText2= (TextView) views.get("motion2");
        countText3= (TextView) views.get("motion3");
        countText4= (TextView) views.get("motion4");
        countText5= (TextView) views.get("motion5");
        countText6= (TextView) views.get("motion6");
    }

    public void setActivity(Activity mainActivity){
        activity = mainActivity;
    }



    synchronized public void emgList(ArrayList<Double> emgF){
        myoEmgFeatureList.add(emgF);
        Log.d("Classify", "emg Ready");
        //Myo_EMG = true;
    }

    synchronized public void imuList(ArrayList<Double> imuF){
        myoImuFeatureList.add(imuF);
        Log.d("Classify", "imu Ready");
        //Myo_IMU = true;
    }

    synchronized public void motiList(ArrayList<Double> motiF){
        motiImuFeatureList.add(motiF);
        Log.d("Classify", "moti Ready");
        //MOTi = true;
    }

    //String featureString = "";
    private String motiImuFeatureString = "", myoImuFeatureString = "", myoEmgFeatureString = "";

    synchronized public void WekaKNN(){
        if(myoEmgFeatureList.size() != 0 && myoImuFeatureList.size() != 0 && motiImuFeatureList.size() != 0){//three devices have data
            Log.d("Classify", "start KNN");
            /*LinkedList<Double> all_feature = new LinkedList<>();

            synchronized (this) {
                for (int i = 0; i < motiFeatureList.get(0).size(); i++) {
                    all_feature.add(motiFeatureList.get(0).get(i));
                }
                for (int i = 0; i < imuFeatureList.get(0).size(); i++) {
                    all_feature.add(imuFeatureList.get(0).get(i));
                }
                for (int i = 0; i < emgFeatureList.get(0).size(); i++) {
                    all_feature.add(emgFeatureList.get(0).get(i));
                }
            }*/

            MotiImuKnnFormat motiImuKnnFormat = new MotiImuKnnFormat();
            MyoImuKnnFormat myoImuKnnFormat = new MyoImuKnnFormat();
            MyoEmgKnnFormat myoEmgKnnFormat = new MyoEmgKnnFormat();

            String motiImuTestData = "", myoImuTestData = "", myoEmgTestData = "";

            motiImuTestData = motiImuTestData + motiImuKnnFormat.getMotiImuKnnFormat();
            myoImuTestData = myoImuTestData + myoImuKnnFormat.getMyoImuKnnFormat();
            myoEmgTestData = myoEmgTestData + myoEmgKnnFormat.getMyoEmgKnnFormatData();

            for (int iMotiImuFeature = 0; iMotiImuFeature < motiImuFeatureList.get(0).size(); iMotiImuFeature ++) {
                motiImuTestData = motiImuTestData + motiImuFeatureList.get(0).get(iMotiImuFeature) + ",";
                synchronized (this) {
                    motiImuFeatureString = motiImuFeatureString + motiImuFeatureList.get(0).get(iMotiImuFeature) + ",";
                }
            }

            for (int iMyoImuFeature = 0; iMyoImuFeature < myoImuFeatureList.get(0).size(); iMyoImuFeature ++) {
                myoImuTestData = myoImuTestData + myoImuFeatureList.get(0).get(iMyoImuFeature) + ",";
                synchronized (this) {
                    myoImuFeatureString = myoImuFeatureString + myoImuFeatureList.get(0).get(iMyoImuFeature) + ",";
                }
            }

            for (int iMyoEmgFeature = 0; iMyoEmgFeature < myoEmgFeatureList.get(0).size(); iMyoEmgFeature ++) {
                myoEmgTestData = myoEmgTestData + myoEmgFeatureList.get(0).get(iMyoEmgFeature) + ",";
                synchronized (this) {
                    myoEmgFeatureString = myoEmgFeatureString + myoEmgFeatureList.get(0).get(iMyoEmgFeature) + ",";
                }
            }

            /*for (int i_feature = 0; i_feature < all_feature.size(); i_feature++){
                testData = testData + all_feature.get(i_feature) +",";
                synchronized (this) {
                    featureString = featureString + all_feature.get(i_feature) +",";
                }
            }*/

            if (MainActivity.getCurrentMode() == 1) {
                motiImuTestData = motiImuTestData + "?";
                myoImuTestData = myoImuTestData + "?";
                myoEmgTestData = myoEmgTestData + "?";
            }
            else if (MainActivity.getCurrentMode() == 0) {//training
                motiImuTestData = motiImuTestData + MainActivity.getTrainingMotion();
                myoImuTestData = myoImuTestData + MainActivity.getTrainingMotion();
                myoEmgTestData = myoEmgTestData + MainActivity.getTrainingMotion();

                synchronized (this) {
                    motiImuFeatureString = motiImuFeatureString + MainActivity.getTrainingMotion() + "\n";
                    myoImuFeatureString = myoImuFeatureString + MainActivity.getTrainingMotion() + "\n";
                    myoEmgFeatureString = myoEmgFeatureString + MainActivity.getTrainingMotion() + "\n";
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
                File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData");
                //File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/MyAndroid");

                //若沒有檔案儲存路徑時則建立此檔案路徑
                if (!mFile.exists()) {
                    if(!mFile.mkdirs()){
                        throw new Error("mkdirs error");
                    }
                }

                if (MainActivity.getCurrentMode() == 1) {
                    FileWriter motiImuTest = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MotiImuTestData/TestData.txt");
                    motiImuTest.write(motiImuTestData);
                    motiImuTest.close();

                    FileWriter myoImuTest = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MyoImuTestData/TestData.txt");
                    myoImuTest.write(myoImuTestData);
                    myoImuTest.close();

                    FileWriter myoEmgTest = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MyoEmgTestData/TestData.txt");
                    myoEmgTest.write(myoEmgTestData);
                    myoEmgTest.close();

                    //start classify myoEmg
                    Thread classify = new Thread(rClassify);
                    classify.start();
                }
                else if (MainActivity.getCurrentMode() == 0) {
                    if (MainActivity.getTrainingPosition () == 1) {//training emg
                        Log.d("ClassifyTraining", "training myoEmg");
                        FileWriter myoEmgTest = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoEmgTraining/TrainingData.txt", true);
                        synchronized (this) {
                            myoEmgTest.write(myoEmgFeatureString);
                        }
                        myoEmgTest.close();
                    }
                    else if (MainActivity.getTrainingPosition () == 2) {//training myoImu
                        Log.d("ClassifyTraining", "training myoImu");
                        FileWriter myoImuTest = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoImuTraining/TrainingData.txt", true);
                        synchronized (this) {
                            myoImuTest.write(myoImuFeatureString);
                        }
                        myoImuTest.close();
                    }
                    else {//training motiImu
                        Log.d("ClassifyTraining", "training motiImu");
                        FileWriter motiImuTest = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MotiImuTraining/TrainingData.txt", true);
                        synchronized (this) {
                            motiImuTest.write(motiImuFeatureString);
                        }
                        motiImuTest.close();
                    }
                    Log.d("Classify", "Training~  no testing");
                }

                //Log.d("saveSuccess","已儲存文字");
            } catch (Exception e) {
                Log.e("save data error", e.getLocalizedMessage());
            }


            /*if (MainActivity.getCurrentMode() == 1) {
                //start classify myoEmg
                Thread classify = new Thread(rClassify);
                classify.start();
            }
            else if (MainActivity.getCurrentMode() == 0) {
                Log.d("Classify", "Training~");
            }*/


            /*Myo_EMG = false;
            Myo_IMU = false;
            MOTi = false;*/

            myoEmgFeatureList.remove(0);
            myoImuFeatureList.remove(0);
            motiImuFeatureList.remove(0);

            // start to clean the list
            //MainActivity.cleanListFlag = true;
        }
    }

    /*public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }*/

    private Runnable rClassify = new Runnable() {
        @Override
        public void run() {

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
                File mMotiImuTrainingFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MotiImuTraining/TrainingData.txt");
                File mMyoImuTrainingFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoImuTraining/TrainingData.txt");
                File mMyoEmgTrainingFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TrainingData/MyoEmgTraining/TrainingData.txt");

                BufferedReader motiImuTrainingData = new BufferedReader(new FileReader(mMotiImuTrainingFile));
                BufferedReader myoImuTrainingData = new BufferedReader(new FileReader(mMyoImuTrainingFile));
                BufferedReader myoEmgTrainingData = new BufferedReader(new FileReader(mMyoEmgTrainingFile));

                Instances motiImuTraining = new Instances(motiImuTrainingData);
                motiImuTraining.setClassIndex(motiImuTraining.numAttributes() - 1);
                Instances myoImuTraining = new Instances(myoImuTrainingData);
                myoImuTraining.setClassIndex(myoImuTraining.numAttributes() - 1);
                Instances myoEmgTraining = new Instances(myoEmgTrainingData);
                myoEmgTraining.setClassIndex(myoEmgTraining.numAttributes() - 1);


                File mMotiImuTestFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MotiImuTestData/TestData.txt");
                File mMyoImuTestFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MyoImuTestData/TestData.txt");
                File mMyoEmgTestFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/TestMethod/TestData/MyoEmgTestData/TestData.txt");

                BufferedReader motiImuTestData = new BufferedReader(new FileReader(mMotiImuTestFile));
                BufferedReader myoImuTestData = new BufferedReader(new FileReader(mMyoImuTestFile));
                BufferedReader myoEmgTestData = new BufferedReader(new FileReader(mMyoEmgTestFile));

                Instances motiImuTest = new Instances(motiImuTestData);
                motiImuTest.setClassIndex(motiImuTest.numAttributes() - 1);
                Instances myoImuTest = new Instances(myoImuTestData);
                myoImuTest.setClassIndex(myoImuTest.numAttributes() - 1);
                Instances myoEmgTest = new Instances(myoEmgTestData);
                myoEmgTest.setClassIndex(myoEmgTest.numAttributes() - 1);

                Classifier motiImuIbk = new IBk();
                motiImuIbk.buildClassifier(motiImuTraining);
                Classifier myoImuIbk = new IBk();
                myoImuIbk.buildClassifier(myoImuTraining);
                Classifier myoEmgIbk = new IBk();
                myoEmgIbk.buildClassifier(myoEmgTraining);

                for (int i = 0; i < motiImuTest.numInstances(); i++) {
                    double clsLabel = motiImuIbk.classifyInstance(motiImuTest.instance(i));
                    motiImuTest.instance(i).setClassValue(clsLabel);
                }
                final Instance mMotiImuTest = motiImuTest.instance(0);
                String[] mMotiImuGesture = mMotiImuTest.toString().split(",");

                leftHandMovement = mMotiImuGesture[motiImuTest.numAttributes()-1];

                for (int i = 0; i < myoImuTest.numInstances(); i++) {
                    double clsLabel = myoImuIbk.classifyInstance(myoImuTest.instance(i));
                    myoImuTest.instance(i).setClassValue(clsLabel);
                }
                final Instance mMyoImuTest = myoImuTest.instance(0);
                String[] mMyoImuGesture = mMyoImuTest.toString().split(",");

                rightHandMovement = mMyoImuGesture[myoImuTest.numAttributes()-1];

                for (int i = 0; i < myoEmgTest.numInstances(); i++) {
                    double clsLabel = myoEmgIbk.classifyInstance(myoEmgTest.instance(i));
                    myoEmgTest.instance(i).setClassValue(clsLabel);
                }
                final Instance mMyoEmgTest = myoEmgTest.instance(0);
                String[] mMyoEmgGesture = mMyoEmgTest.toString().split(",");

                rightHandStatus = mMyoEmgGesture[myoEmgTest.numAttributes()-1];

                synchronized (this) {
                    classifyResult = classifyResult + leftHandMovement + " " + rightHandMovement + " " + rightHandStatus + "\n";
                }

                //classify and training in the same time
//                FileWriter addNewDataInTraining = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/Training2.0/TrainingData/TrainingData.txt", true);
//                synchronized (this){
//                    addNewDataInTraining.write("\n" + featureString + result);
//                }
//                addNewDataInTraining.close();

                /*synchronized (this) {
                    featureString = "";
                }

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        int tempCount;

                        textView.setText(result);

                        switch (mTest.classAttribute().indexOfValue(result)){

                            case 0://motion 1
                                tempCount = Integer.parseInt(countText1.getText().toString());
                                tempCount++;
                                countText1.setText(tempCount+"");
                                break;
                            case 1://motion 2
                                tempCount = Integer.parseInt(countText2.getText().toString());
                                tempCount++;
                                countText2.setText(tempCount+"");
                                break;
                            case 2://motion 3
                                tempCount = Integer.parseInt(countText3.getText().toString());
                                tempCount++;
                                countText3.setText(tempCount+"");
                                break;
                            case 3://motion 4
                                tempCount = Integer.parseInt(countText4.getText().toString());
                                tempCount++;
                                countText4.setText(tempCount+"");
                                break;
                            case 4://motion 5
                                tempCount = Integer.parseInt(countText5.getText().toString());
                                tempCount++;
                                countText5.setText(tempCount+"");
                                break;
                            case 5://motion 6
                                tempCount = Integer.parseInt(countText6.getText().toString());
                                tempCount++;
                                countText6.setText(tempCount+"");
                                break;
                        }
                    }
                });

                Thread.sleep(1000);

                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        textView.setText("NULL");
                    }
                });
                //textView.setText(result);
                Log.d("RESUlt", result);*/
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


}
