package com.kha.loomoconnection.loomocontrol;

import android.util.Log;

import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.tts.TtsListener;

public class VoiceModule implements BaseModule {

    private static VoiceModule instance = new VoiceModule();
    ServiceBinder.BindStateListener mBindStateListener;
    boolean mBind;
    public Speaker mSpeaker;

    public static VoiceModule getInstance() {
        return instance;
    }

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
        mSpeaker.unbindService();
        mSpeaker.bindService(ContextBinder.context, mBindStateListener);
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
                Log.i("Voice", "bind end");
                mBind = false;
            }
        };
        mSpeaker = Speaker.getInstance();
        mSpeaker.bindService(ContextBinder.context, mBindStateListener);
    }

    public boolean speak(String voiceline){
        try {
            mSpeaker.speak(voiceline, new TtsListener() {
                @Override
                public void onSpeechStarted(String s) { }

                @Override
                public void onSpeechFinished(String s) { }

                @Override
                public void onSpeechError(String s, String s1) { }
            });
        } catch (VoiceException e){
            Log.e("Voice Module", "It should not happen!");
            return false;
        }
        return true;
    }

}
