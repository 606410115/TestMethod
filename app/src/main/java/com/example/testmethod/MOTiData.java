package com.example.testmethod;

import java.util.ArrayList;

/**
 * Created by Mslab on 2017/10/6.
 */

public class MOTiData{
    private ArrayList<Double> motiData = new ArrayList<>();

    private double time;
    private int num;

    double accX, accY, accZ;
    double gryoX, gryoY, gryoZ;

    public MOTiData() {
    }

    public MOTiData(MOTiCharacteristicData characteristicData, TimeManager timeManager, int num) {
        this.motiData = new ArrayList<>(characteristicData.covertRawData().getMOTiArray() );
        //time = timeManager.getTime();
        this.num = num;
    }

    public int getNum () {
        return this.num;
    }

    public void addElement(double element) {
        motiData.add(element);
    }

    public void setElement(int index ,double element) {
        motiData.set(index,element);
    }

    public Double getElement(int index) {
        if (index < 0 || index > motiData.size() - 1) {
            return null;
        } else {
            return motiData.get(index);
        }
    }

    public ArrayList<Double> getMOTiArray() {
        return this.motiData;
    }

    public double getTime(){
        return time;
    }

    public void logMoti(){
        accX=motiData.get(0);
        accY=motiData.get(1);
        accZ=motiData.get(2);
        gryoX=motiData.get(3);
        gryoY=motiData.get(4);
        gryoZ=motiData.get(5);
    }
}
