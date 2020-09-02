package com.kha.loomoconnection.loomocontrol;


import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.kha.loomoconnection.restserver.model.data.RawSensorData;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.perception.sensor.Sensor;
import com.segway.robot.sdk.perception.sensor.SensorData;

import java.util.Arrays;

public class SensorModule implements BaseModule {
    public Sensor mSensor;
    boolean mBind;
    ServiceBinder.BindStateListener mBindStateListener;
    private static SensorModule instance = new SensorModule();

    public static SensorModule getInstance() {return instance;}

    @Override
    public void rebindServices() {
        mBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                mBind = true;
            }

            @Override
            public void onUnbind(String reason) {
                mBind = false;
            }
        };
        mSensor.unbindService();
        mSensor.bindService(ContextBinder.context, mBindStateListener);
    }

    @Override
    public void moduleInit() {
        mBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                mBind = true;
            }

            @Override
            public void onUnbind(String reason) {
                mBind = false;
            }
        };
        mSensor = Sensor.getInstance();
        mSensor.bindService(ContextBinder.context, mBindStateListener);
    }

    public RawSensorData fetchSensorData() {
        RawSensorData sensorData = new RawSensorData();
        SensorData mInfraredData = mSensor.querySensorData(Arrays.asList(Sensor.INFRARED_BODY)).get(0);
        float mInfraredDistanceLeft = mInfraredData.getIntData()[0];
        float mInfraredDistanceRight = mInfraredData.getIntData()[1];
        sensorData.setInfraredDistanceL(mInfraredDistanceLeft);
        sensorData.setInfraredDistanceR(mInfraredDistanceRight);

        SensorData mUltrasonicData = mSensor.querySensorData(Arrays.asList(Sensor.ULTRASONIC_BODY)).get(0);
        float mUltrasonicDistance = mUltrasonicData.getIntData()[0];
        sensorData.setUltrasonicDistance(mUltrasonicDistance);

        SensorData mHeadImu = mSensor.querySensorData(Arrays.asList(Sensor.HEAD_WORLD_IMU)).get(0);
        float mWorldPitch = mHeadImu.getFloatData()[0];
        float mWorldRoll = mHeadImu.getFloatData()[1];
        float mWorldYaw = mHeadImu.getFloatData()[2];
        sensorData.setHeadWorldPitch(mWorldPitch);
        sensorData.setHeadWorldRoll(mWorldRoll);
        sensorData.setHeadWorldYaw(mWorldYaw);

        SensorData mHeadPitch = mSensor.querySensorData(Arrays.asList(Sensor.HEAD_JOINT_PITCH)).get(0);
        float mJointPitch = mHeadPitch.getFloatData()[0];
        sensorData.setJointPitch(mJointPitch);

        SensorData mHeadYaw = mSensor.querySensorData(Arrays.asList(Sensor.HEAD_JOINT_YAW)).get(0);
        float mJointYaw = mHeadYaw.getFloatData()[0];
        sensorData.setJointYaw(mJointYaw);

        SensorData mHeadRoll = mSensor.querySensorData(Arrays.asList(Sensor.HEAD_JOINT_ROLL)).get(0);
        float mJointRoll = mHeadRoll.getFloatData()[0];
        sensorData.setJointRoll(mJointRoll);

        SensorData mBaseTick = mSensor.querySensorData(Arrays.asList(Sensor.ENCODER)).get(0);
        int mBaseTicksL = mBaseTick.getIntData()[0];
        int mBaseTicksR = mBaseTick.getIntData()[1];
        sensorData.setWheelTicksL(mBaseTicksL);
        sensorData.setWheelTicksR(mBaseTicksR);

        SensorData mBaseImu = mSensor.querySensorData(Arrays.asList(Sensor.BASE_IMU)).get(0);
        float mBasePitch = mBaseImu.getFloatData()[0];
        float mBaseRoll = mBaseImu.getFloatData()[1];
        float mBaseYaw = mBaseImu.getFloatData()[2];
        sensorData.setBasePitch(mBasePitch);
        sensorData.setBaseRoll(mBaseRoll);
        sensorData.setBaseYaw(mBaseYaw);

        SensorData mPose2DData = mSensor.querySensorData(Arrays.asList(Sensor.POSE_2D)).get(0);
        Pose2D pose2D = mSensor.sensorDataToPose2D(mPose2DData);
        float x = pose2D.getX();
        float y = pose2D.getY();
        float mTheta = pose2D.getTheta();
        float mLinearVelocity = pose2D.getLinearVelocity();
        float mAngularVelocity = pose2D.getAngularVelocity();
        sensorData.setX(x);
        sensorData.setY(y);
        sensorData.setTheta(mTheta);
        sensorData.setLinearVelocity(mLinearVelocity);
        sensorData.setAngularVelocity(mAngularVelocity);

        SensorData mWheelSpeed = mSensor.querySensorData(Arrays.asList(Sensor.WHEEL_SPEED)).get(0);
        float mWheelSpeedL = mWheelSpeed.getFloatData()[0];
        float mWheelSpeedR = mWheelSpeed.getFloatData()[1];
        sensorData.setWheelSpeedL(mWheelSpeedL);
        sensorData.setWheelSpeedR(mWheelSpeedR);
        return sensorData;
    }
}
