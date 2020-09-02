package com.kha.loomoconnection.loomocontrol;

import android.util.Log;
import android.widget.Toast;

import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.kha.loomoconnection.restserver.model.data.CPProgress;
import com.kha.loomoconnection.restserver.model.data.Point2D;
import com.kha.loomoconnection.restserver.model.data.Point2Dtheta;
import com.kha.loomoconnection.restserver.model.requests.BaseRawMovementRequest;
import com.kha.loomoconnection.utils.AngleUtils;
import com.kha.loomoconnection.utils.TimerUtils;
import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.PoseVLS;
import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.base.log.Logger;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener;
import com.segway.robot.sdk.vision.DTS;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LocomotionModule implements BaseModule {
    public Base mBase;
    boolean mBind;
    ServiceBinder.BindStateListener mBindStateListener;
    private static LocomotionModule instance = new LocomotionModule();
    public long checkPointsCalled = 0;
    public long checkPointsReached = 0;
    public long checkPointsMissed = 0;
    public double meters = 0;
    public double roughMeters = 0;
    CheckPointStateListener mCheckPointListener;
    boolean tracking = false;
    boolean meetObstacle = false;
//    public Thread baseTrackingThread;
    public Timer meterTimer;
    public TimerTask meterTimerTask;
    public boolean meterAvail = false;
    public boolean meterInitialized = false;
    public Pose2D previousPos;


    public static LocomotionModule getInstance() {return instance;}

    @Override
    public void rebindServices() {
        meterAvail = false;
        mBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                mBind = true;
                meterAvail = true;
            }

            @Override
            public void onUnbind(String reason) {
                mBind = false;
            }
        };
        mBase.unbindService();

        mBase.bindService(ContextBinder.context, mBindStateListener);
    }

    @Override
    public void moduleInit() {
        meterAvail = false;
        mBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                mBind = true;
                meterAvail = true;
                startMeterTimer();
            }

            @Override
            public void onUnbind(String reason) {
                Log.i("Voice", "bind end");
                mBind = false;
            }
        };
        mBase = Base.getInstance();
        mBase.bindService(ContextBinder.context, mBindStateListener);
    }

    public void startMeterTimer(){
        meterTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (meterAvail) {
                    if (!meterInitialized) {
                        previousPos = getLatestOdometry();
                        meterInitialized = true;
                    } else {
                        Pose2D curPos = getLatestOdometry();
                        double diff = Math.sqrt(Math.pow(curPos.getX() - previousPos.getX(), 2) + Math.pow(curPos.getY() - previousPos.getY(), 2));
                        meters += diff;
                        if (diff >= 0.1) {
                            roughMeters += diff;
                        }
                        previousPos = curPos;
                    }
                }
            }
        };
        meterTimer = new Timer();
        //半秒更新一次
        meterTimer.schedule(meterTimerTask, 0, 500);
    }

    public Pose2D getLatestOdometry() {
        return mBase.getOdometryPose(-1);
    }

    public Pose2D getOdometryByTime(long microseconds) {
        return mBase.getOdometryPose(microseconds);
    }

    public void setOrigin(Pose2D pose2D) {
        mBase.cleanOriginalPoint();
        mBase.setOriginalPoint(pose2D);
    }

    public void resetOrigin() {
        mBase.cleanOriginalPoint();
        Pose2D pose2D = getLatestOdometry();
        mBase.setOriginalPoint(pose2D);
    }

    public void switchMode(String mode) {
        meetObstacle = false;
        int currMode = mBase.getControlMode();
        String currModeString;
        switch (currMode) {
            case Base.CONTROL_MODE_RAW:
                currModeString = "raw";
                break;
            case Base.CONTROL_MODE_FOLLOW_TARGET:
                currModeString = "follow";
                break;
            case Base.CONTROL_MODE_NAVIGATION:
                currModeString = "navigation";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + currMode);
        }
        if (currModeString.equals("navigation") && !currModeString.equals(mode)) {
            //重置所有点
            cleanCheckPoints();
        } else if (currModeString.equals("follow") && !currModeString.equals(mode)) {
            //重置识别的人
            if (tracking) {
                VisionModule.getInstance().stopTrack();
            }
            VisionModule.getInstance().stopDTS();
        }
        switch (mode) {
            case "raw":
                mBase.setControlMode(Base.CONTROL_MODE_RAW);
                break;
            case "follow":
                mBase.setControlMode(Base.CONTROL_MODE_FOLLOW_TARGET);
                VisionModule.getInstance().bootDTS();
                break;
            case "navigation":
                mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                //activateVLS
                mBase.startVLS(true, true, new StartVLSListener() {
                    @Override
                    public void onOpened() {
                        // set navigation data source
                        mBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.d("VLS", "onError() called with: errorMessage = [" + errorMessage + "]");
                    }
                });
                mCheckPointListener = new CheckPointStateListener() {
                    @Override
                    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                        checkPointsReached += 1;
                    }

                    @Override
                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                        checkPointsMissed += 1;
                    }
                };
                mBase.setOnCheckPointArrivedListener(mCheckPointListener);
                break;
        }
    }

    public boolean startPersonTracking() {
        if (mBase.getControlMode() != Base.CONTROL_MODE_FOLLOW_TARGET) {
            return false;
        }
        tracking = true;
        meetObstacle = false;
        return VisionModule.getInstance().detectPersonandTrack();
    }

    public void stopTracking() {
        if (mBase.getControlMode() != Base.CONTROL_MODE_FOLLOW_TARGET) {
            return;
        }
        tracking = false;
        meetObstacle = false;
        VisionModule.getInstance().stopTrack();
    }

    public boolean checkPointsFinished() {
        return checkPointsReached + checkPointsMissed == checkPointsCalled;
    }

    public void cleanCheckPoints() {
        if (mBase.getControlMode() != Base.CONTROL_MODE_NAVIGATION) {
            return;
        }
        meetObstacle = false;
        mBase.clearCheckPointsAndStop();
        checkPointsCalled = 0;
        checkPointsReached = 0;
        checkPointsMissed = 0;
    }

    public double clearMeters() {
        meters = 0;
        roughMeters = 0;
        return meters;
    }

    public double getMeters() {
        return meters;
    }
    public double getRoughMeters() {return roughMeters;}

    public void batchAddCheckPoint2D(ArrayList<Point2D> array) {
        if (mBase.getControlMode() == Base.CONTROL_MODE_NAVIGATION) {
            for (Point2D point : array) {
                mBase.addCheckPoint(point.getX(), point.getY());
                checkPointsCalled += 1;
            }
        }
    }

    public void addCheckPoint2D(Point2D point2D) {
        if (mBase.getControlMode() != Base.CONTROL_MODE_NAVIGATION) {
            return;
        }
        mBase.addCheckPoint(point2D.getX(), point2D.getY());
        checkPointsCalled += 1;
    }

    public void batchAddCheckPoint2DWithTheta(ArrayList<Point2Dtheta> array) {
        if (mBase.getControlMode() == Base.CONTROL_MODE_NAVIGATION) {
            for (Point2Dtheta point : array) {
                mBase.addCheckPoint(point.getX(), point.getY(), point.getTheta());
                checkPointsCalled += 1;
            }
        }
    }

    public void addCheckPoint2DWithTheta(Point2Dtheta point2Dtheta) {
        if (mBase.getControlMode() != Base.CONTROL_MODE_NAVIGATION) {
            return;
        }
        mBase.addCheckPoint(point2Dtheta.getX(), point2Dtheta.getY(), point2Dtheta.getTheta());
        checkPointsCalled += 1;
    }

    public CPProgress fetchCPProgress() {
        CPProgress result = new CPProgress();
        result.calledNum = checkPointsCalled;
        result.missedNum = checkPointsMissed;
        result.reachedNum = checkPointsReached;
        if (checkPointsCalled == 0) {
            result.finishedPercent = 0;
        } else {
            result.finishedPercent = (checkPointsMissed + checkPointsReached) / (float) checkPointsCalled * 100;
        }
        result.finished = checkPointsFinished();
        return result;
    }

    public void toggleObstacleAvoidance(boolean trigger){
        if (trigger){
            Logger.i("loomo", "called");
            mBase.setUltrasonicObstacleAvoidanceEnabled(true);
            mBase.setUltrasonicObstacleAvoidanceDistance(0.5f);
            mBase.setObstacleStateChangeListener(new ObstacleStateChangedListener() {
                @Override
                public void onObstacleStateChanged(int ObstacleAppearance) {
                    VoiceModule.getInstance().speak("快撞啦！");
                    meetObstacle = true;
                }

            });
        } else {
            mBase.setUltrasonicObstacleAvoidanceEnabled(false);
        }
    }

    public boolean getmeetObstacle() {
        return meetObstacle;
    }

    public void setBaseMovement(BaseRawMovementRequest request) {
        if (mBase.getControlMode() != Base.CONTROL_MODE_RAW) {
            return;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mBase.getControlMode() == Base.CONTROL_MODE_RAW) {
                    mBase.setLinearVelocity(request.linearSpeed);
                    mBase.setAngularVelocity(AngleUtils.NinetyToPi(request.angularSpeed));
                    TimerTask linearOverTask = new TimerTask() {
                        @Override
                        public void run() {
                            mBase.setLinearVelocity(0);
                        }
                    };
                    TimerTask angularOverTask = new TimerTask() {
                        @Override
                        public void run() {
                            mBase.setAngularVelocity(0);
                        }
                    };
                    if (request.linearDuration > 0) {
                        TimerUtils.timer.schedule(linearOverTask, request.linearDuration);
                    }
                    if (request.angularDuration > 0) {
                        TimerUtils.timer.schedule(angularOverTask, request.angularDuration);
                    }
                }
            }
        };
    }



}
