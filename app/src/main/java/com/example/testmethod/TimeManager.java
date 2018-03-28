package com.example.testmethod;

/**
 * Created by Mslab on 2017/10/5.
 */

public class TimeManager {
    private long initTime;

    TimeManager(){
        initTime = System.currentTimeMillis();
    }

    public double getTime(){
        long nowTime = System.currentTimeMillis();

        return (double)(nowTime-initTime)/1000;//開始到現在累積的時間
    }

}
