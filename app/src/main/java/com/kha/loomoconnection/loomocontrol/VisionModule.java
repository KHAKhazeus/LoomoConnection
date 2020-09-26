package com.kha.loomoconnection.loomocontrol;

import android.app.Person;
import android.graphics.Bitmap;
import android.util.Log;

import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.kha.loomoconnection.restserver.model.data.ColorImageData;
import com.kha.loomoconnection.restserver.model.data.DepthImageData;
import com.kha.loomoconnection.restserver.model.data.ImageData;
import com.kha.loomoconnection.socketserver.SocketServer;
import com.kha.loomoconnection.utils.TimerUtils;
import com.segway.robot.algo.dts.DTSPerson;
import com.segway.robot.algo.dts.PersonTrackingListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.vision.DTS;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamType;
import com.segway.robot.sdk.voice.Speaker;

import android.util.Base64;
import android.view.Surface;

import java.util.Objects;
import java.util.TimerTask;

public class VisionModule implements BaseModule{
    private static VisionModule instance = new VisionModule();
    public static VisionModule getInstance() {
        return instance;
    }
    ServiceBinder.BindStateListener mBindStateListener;
    public DTS mDTS;
    boolean mBind;
    public static boolean DTSInitialized = false;
    public Vision mVision;
    private byte[] depthBuffer = new byte[153600];
    private byte[] colorBuffer = new byte[640*2 * 480*2];
    private Bitmap colorBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    DTSPerson personForTrack;
    boolean personReady;

    public void bootDTS() {
        if (!DTSInitialized) {
            mDTS = mVision.getDTS();
            mDTS.setVideoSource(DTS.VideoSource.CAMERA);
            mDTS.start();
            DTSInitialized = true;
        }
    }

    public boolean detectPersonandTrack() {
        DTSPerson[] persons = mDTS.detectPersons(3 * 1000 * 1000);
        if (persons.length <= 0) {
            personReady = false;
            return false;
        }
        personForTrack = persons[0];
        personReady = true;
        mDTS.startPersonTracking(persons[0], 10 * 1000, new PersonTrackingListener() {
            @Override
            public void onPersonTracking(DTSPerson person) {
                personForTrack = person;
                personReady = true;
                if (person.getDistance() > 0.35 && person.getDistance() < 5) {
                    LocomotionModule.getInstance().mBase.updateTarget((float) (person.getDistance() - 1.2), person.getTheta());
                }
            }

            @Override
            public void onPersonTrackingResult(DTSPerson person) {
                personForTrack = person;
                personReady = true;
            }

            @Override
            public void onPersonTrackingError(int errorCode, String message) {
            }
        });
        return true;
    }

    public void stopTrack() {
        mDTS.stopPersonTracking();
        personReady = false;
    }

    public void stopDTS() {
        mDTS = mVision.getDTS();
        mDTS.stop();
        personReady = false;
        DTSInitialized = false;
    }

    public byte[] getDepthData() {
        return depthBuffer.clone();
    }

    public byte[] getColorData() {
        return colorBuffer.clone();
    }

    public ImageData getImageData() {
        ImageData imgData = new ImageData();

        imgData.setColor(Base64.encodeToString(colorBuffer.clone(), Base64.DEFAULT));
        imgData.setDepth(Base64.encodeToString(depthBuffer.clone(), Base64.DEFAULT));
        return imgData;
    }

    public ColorImageData getColorImageData() {
        ColorImageData imgData = new ColorImageData();

        imgData.setColor(Base64.encodeToString(colorBuffer.clone(), Base64.DEFAULT));
        return imgData;
    }

    public DepthImageData getDepthImageData() {
        DepthImageData imgData = new DepthImageData();

        imgData.setDepth(Base64.encodeToString(depthBuffer.clone(), Base64.DEFAULT));
        return imgData;
    }

    public void unbindServices() {
        mVision.unbindService();
        mBind = false;
        mVision.stopListenFrame(StreamType.COLOR);
        mVision.stopListenFrame(StreamType.DEPTH);
    }

    @Override
    public void rebindServices() {
        mBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.i("Vision", "Start bind");
                mBind = true;
                mVision.startListenFrame(StreamType.DEPTH, new Vision.FrameListener(){

                    @Override
                    public void onNewFrame(int streamType, Frame frame) {
//                        byte[] tempb = new byte[frame.getByteBuffer().remaining()];
                        frame.getByteBuffer().get(depthBuffer);
//                        depthBuffer = tempb.clone();
//                        SocketServer.getInstance().broadCast(depthBuffer);
                    }
                });
                mVision.startListenFrame(StreamType.COLOR, new Vision.FrameListener() {
                    @Override
                    public void onNewFrame(int streamType, Frame frame) {
                        Log.i("COLOR", String.valueOf(frame.getByteBuffer().remaining()));
//                        colorBitmap.copyPixelsFromBuffer(frame.getByteBuffer());
//                        byte[] tempb = new byte[frame.getByteBuffer().remaining()];
                        frame.getByteBuffer().get(colorBuffer);
//                        colorBuffer = tempb.clone();
//                        SocketServer.getInstance().broadCast(tempb);
                    }
                });
            }

            @Override
            public void onUnbind(String reason) {
                Log.i("Vision", "bind end");
                mBind = false;
                mVision.stopListenFrame(StreamType.COLOR);
                mVision.stopListenFrame(StreamType.DEPTH);
            }
        };
        mVision.bindService(ContextBinder.context, mBindStateListener);
    }

    @Override
    public void moduleInit() {
        mBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.i("Vision", "Start bind");
                mBind = true;
                try {
                    bootDTS();
                } catch (IllegalStateException e) {
                    Log.i("Locomotion", Objects.requireNonNull(e.getMessage()));
                }
                mVision.startListenFrame(StreamType.DEPTH, new Vision.FrameListener(){

                    @Override
                    public void onNewFrame(int streamType, Frame frame) {
                        //153600
//                        byte[] tempb = new byte[frame.getByteBuffer().remaining()];
//                        Log.i("DEPTH", String.valueOf(frame.getByteBuffer().remaining()));
                        frame.getByteBuffer().get(depthBuffer);
//                        depthBuffer = tempb.clone();
//                        SocketServer.getInstance().broadCast(depthBuffer);
                    }
                });
                mVision.startListenFrame(StreamType.COLOR, new Vision.FrameListener() {
                    @Override
                    public void onNewFrame(int streamType, Frame frame) {
//                        Log.i("COLOR", String.valueOf(frame.getByteBuffer().remaining()));
//                        colorBitmap.copyPixelsFromBuffer(frame.getByteBuffer());
//                        byte[] tempb = new byte[frame.getByteBuffer().remaining()];
                        frame.getByteBuffer().get(colorBuffer);
//                        colorBuffer = tempb.clone();
//                        SocketServer.getInstance().broadCast(tempb);
                    }
                });
            }

            @Override
            public void onUnbind(String reason) {
                Log.i("Vision", "bind end");
                mBind = false;
                mVision.stopListenFrame(StreamType.COLOR);
                mVision.stopListenFrame(StreamType.DEPTH);
            }
        };
        mVision = Vision.getInstance();
        mVision.bindService(ContextBinder.context, mBindStateListener);
    }

}
