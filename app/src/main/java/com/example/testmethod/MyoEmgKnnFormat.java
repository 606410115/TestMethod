package com.example.testmethod;

/**
 * Created by Mslab on 2018/3/9.
 */

public class MyoEmgKnnFormat {
    private static final String MYO_EMG_KNN_FORMAT = "@relation ads\n" +
            "\n" +
            "@attribute emg_0_mean numeric\n" +
            "@attribute emg_1_mean numeric\n" +
            "@attribute emg_2_mean numeric\n" +
            "@attribute emg_3_mean numeric\n" +
            "@attribute emg_4_mean numeric\n" +
            "@attribute emg_5_mean numeric\n" +
            "@attribute emg_6_mean numeric\n" +
            "@attribute emg_7_mean numeric\n" +
            "@attribute profit {緊握, 張開, 放鬆}\n" +
            "\n" +
            "@data\n";
    public String getMyoEmgKnnFormatData(){
        return MYO_EMG_KNN_FORMAT;
    }
}
