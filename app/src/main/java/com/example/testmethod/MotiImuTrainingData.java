package com.example.testmethod;

/**
 * Created by Mslab on 2018/3/9.
 */

public class MotiImuTrainingData {
    private static final String MOTI_IMU_TRAINING_DATA = "@relation ads\n" +
            "\n" +
            "@attribute moti_x_acc_mean numeric\n" +
            "@attribute moti_y_acc_mean numeric\n" +
            "@attribute moti_z_acc_mean numeric\n" +
            "@attribute moti_x_acc_SD numeric\n" +
            "@attribute moti_y_acc_SD numeric\n" +
            "@attribute moti_z_acc_SD numeric\n" +
            "@attribute profit {左手向上, 左手向下, 左手向左, 左手向右, 左手向前伸, 左手縮回來}\n" +
            "\n" +
            "@data\n";
    public String getMotiImuTrainingData(){
        return MOTI_IMU_TRAINING_DATA;
    }
}
