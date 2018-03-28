package com.example.testmethod;

/**
 * Created by Mslab on 2018/3/9.
 */

public class MyoImuKnnFormat {
    private static final String MYO_IMU_KNN_FORMAT = "@relation ads\n" +
            "\n" +
            "@attribute imu_x_acc_mean numeric\n" +
            "@attribute imu_y_acc_mean numeric\n" +
            "@attribute imu_z_acc_mean numeric\n" +
            "@attribute imu_x_acc_SD numeric\n" +
            "@attribute imu_y_acc_SD numeric\n" +
            "@attribute imu_z_acc_SD numeric\n" +
            /*"@attribute imu_quaternion_w numeric\n" +
            "@attribute imu_quaternion_x numeric\n" +
            "@attribute imu_quaternion_y numeric\n" +
            "@attribute imu_quaternion_z numeric\n" +*/
            "@attribute profit {右手向上, 右手向下, 右手向左, 右手向右, 右手向前伸, 右手縮回來}\n" +
            "\n" +
            "@data\n";
    public String getMyoImuKnnFormat(){
        return MYO_IMU_KNN_FORMAT;
    }
}
