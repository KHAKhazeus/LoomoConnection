package com.kha.loomoconnection.loomocontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.kha.loomoconnection.EmojiActivity;
import com.kha.loomoconnection.MainActivity;
import com.kha.loomoconnection.R;
import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.kha.loomoconnection.restserver.model.data.HeadStatus;
import com.kha.loomoconnection.restserver.model.requests.HeadMovementRequest;
import com.kha.loomoconnection.utils.AngleUtils;
import com.kha.loomoconnection.utils.TimerUtils;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.emoji.BaseControlHandler;
import com.segway.robot.sdk.emoji.Emoji;
import com.segway.robot.sdk.emoji.EmojiPlayListener;
import com.segway.robot.sdk.emoji.EmojiView;
import com.segway.robot.sdk.emoji.HeadControlHandler;
import com.segway.robot.sdk.emoji.configure.BehaviorList;
import com.segway.robot.sdk.emoji.exception.EmojiException;
import com.segway.robot.sdk.emoji.player.RobotAnimator;
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory;
import com.segway.robot.sdk.locomotion.head.Angle;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.voice.Speaker;

import java.util.Objects;
import java.util.TimerTask;

public class HeadModule implements BaseModule{
    static public boolean isPitchEnabled = true;
    static public boolean isYawEnabled = true;
    static public boolean emojiMode = false;
    boolean mBind;
    public Head mHead;
    ServiceBinder.BindStateListener mBindStateListener;
    private static HeadModule instance = new HeadModule();
    public static HeadModule getInstance() {return instance;}
    public Emoji mEmoji;
    public HeadControlHandler mHeadControlHandler;

    public void rebindEmoji() {
        mEmoji = Emoji.getInstance();
        mEmoji.init(ContextBinder.context);
        EmojiView emojiView = ((Activity) ContextBinder.context).findViewById(R.id.face);
        mEmoji.setEmojiView(emojiView);
        mHeadControlHandler = new HeadControlManager(ContextBinder.context);
        mEmoji.setHeadControlHandler(mHeadControlHandler);
        mEmoji.setBaseControlHandler(new BaseControlManager(ContextBinder.context));
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
        mHead.unbindService();
        mHead.bindService(ContextBinder.context, mBindStateListener);
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
        mHead = Head.getInstance();
        mHead.bindService(ContextBinder.context, mBindStateListener);
    }

    public boolean setHeadLight(int range) {
        mHead.setHeadLightMode(range);
        return true;
    }

    public HeadStatus fetchHeadInfo() {
        HeadStatus rsp = new HeadStatus();
        rsp.setPitchEnabled(isPitchEnabled);
        rsp.setYawEnabled(isYawEnabled);
        switch (mHead.getMode()) {
            case Head.MODE_SMOOTH_TACKING:
                rsp.setHeadMode("smooth_tracking");
                break;
            case Head.MODE_ORIENTATION_LOCK:
                rsp.setHeadMode("orientation_lock");
                break;
        }
        rsp.setWorldPitch(AngleUtils.PiToNinety(mHead.getWorldPitch().getAngle()));
        rsp.setWorldYaw(AngleUtils.PiToNinety(mHead.getWorldYaw().getAngle()));
        rsp.setYawAV(AngleUtils.PiToNinety(mHead.getYawAngularVelocity().getVelocity()));
        rsp.setPitchAV(AngleUtils.PiToNinety(mHead.getPitchAngularVelocity().getVelocity()));
        return rsp;
    }

    public boolean updateHeadMovement(HeadMovementRequest headMovementRequest) {
        if (headMovementRequest.changeMode) {
            switch (headMovementRequest.headMode) {
                case "smooth_tracking":
                    mHead.setMode(Head.MODE_SMOOTH_TACKING);
                    break;
                case "orientation_lock":
                    mHead.setMode(Head.MODE_ORIENTATION_LOCK);
                    break;
                default:
                    return false;
            }
        }
        if (isPitchEnabled) {
            if (mHead.getMode() == Head.MODE_SMOOTH_TACKING) {
                mHead.setWorldPitch(AngleUtils.NinetyToPi(headMovementRequest.worldPitch));
            } else {
                mHead.setPitchAngularVelocity(AngleUtils.NinetyToPi(headMovementRequest.pitchAV));
            }
        }
        if (isYawEnabled) {
            if (mHead.getMode() == Head.MODE_SMOOTH_TACKING) {
                mHead.setWorldYaw(AngleUtils.NinetyToPi(headMovementRequest.worldYaw));
            } else {
                mHead.setYawAngularVelocity(0);
            }
        }
        //loomo sdk在这里有个bug，这个为了专门解决bug而写的补充代码
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mHead.getMode() == Head.MODE_ORIENTATION_LOCK) {
                    mHead.setPitchAngularVelocity(AngleUtils.NinetyToPi(headMovementRequest.pitchAV));
                    mHead.setYawAngularVelocity(AngleUtils.NinetyToPi(headMovementRequest.yawAV));
                    TimerTask pitchOverTask = new TimerTask() {
                        @Override
                        public void run() {
                            mHead.setPitchAngularVelocity(0);
                        }
                    };
                    TimerTask yawOverTask = new TimerTask() {
                        @Override
                        public void run() {
                            mHead.setYawAngularVelocity(0);
                        }
                    };
                    if (headMovementRequest.yawMovDuration > 0) {
                        TimerUtils.timer.schedule(yawOverTask, headMovementRequest.yawMovDuration);
                    }
                    if (headMovementRequest.pitchMovDuration > 0) {
                        TimerUtils.timer.schedule(pitchOverTask, headMovementRequest.pitchMovDuration);
                    }
                }
            }
        };
        TimerUtils.timer.schedule(task, 1000);
        return true;
    }

    public void switchEmoji(boolean emojiMode) {
        if (emojiMode) {
            ((MainActivity)ContextBinder.context).switchToEmoji();
            HeadModule.emojiMode = true;
        } else {
            ((EmojiActivity)ContextBinder.context).switchToMain();
            HeadModule.emojiMode = false;
        }
    }

    public boolean makeEmoji(String behavior) {
        try {
            int choosenBehavior;
            switch (behavior) {
                case "look_around":
                    choosenBehavior = BehaviorList.LOOK_AROUND;
                    break;
                case "look_comfort":
                    choosenBehavior = BehaviorList.LOOK_COMFORT;
                    break;
                case "look_curious":
                    choosenBehavior = BehaviorList.LOOK_CURIOUS;
                    break;
                case "look_no_no":
                    choosenBehavior = BehaviorList.LOOK_NO_NO;
                    break;
                case "look_up":
                    choosenBehavior = BehaviorList.LOOK_UP;
                    break;
                case "look_down":
                    choosenBehavior = BehaviorList.LOOK_DOWN;
                    break;
                case "look_left":
                    choosenBehavior = BehaviorList.LOOK_LEFT;
                    break;
                case "look_right":
                    choosenBehavior = BehaviorList.LOOK_RIGHT;
                    break;
                case "turn_left":
                    choosenBehavior = BehaviorList.TURN_LEFT;
                    break;
                case "turn_right":
                    choosenBehavior = BehaviorList.TURN_RIGHT;
                    break;
                case "turn_around":
                    choosenBehavior = BehaviorList.TURN_AROUND;
                    break;
                case "turn_full":
                    choosenBehavior = BehaviorList.TURN_FULL;
                    break;
                case "apple_wow":
                    choosenBehavior = BehaviorList.APPLE_WOW_EMOTION;
                    break;
                case "apple_like":
                    choosenBehavior = BehaviorList.APPLE_LIKE_EMOTION;
                    break;
                case "apple_love":
                    choosenBehavior = BehaviorList.APPLE_LOVE_EMOTION;
                    break;
                case "apple_lose":
                    choosenBehavior = BehaviorList.APPLE_LOSE_EMOTION;
                    break;
                case "apple_halo":
                    choosenBehavior = BehaviorList.APPLE_HALO_EMOTION;
                    break;
                case "avatar_hello":
                    choosenBehavior = BehaviorList.AVATAR_HELLO_EMOTION;
                    break;
                case "avatar_curious":
                    choosenBehavior = BehaviorList.AVATAR_CURIOUS_EMOTION;
                    break;
                case "avatar_blink":
                    choosenBehavior = BehaviorList.AVATAR_BLINK_EMOTION;
                    break;
                default:
                    choosenBehavior = BehaviorList.IDEA_BEHAVIOR_RANDOM;
            }
            mEmoji.startAnimation(RobotAnimatorFactory.getReadyRobotAnimator(choosenBehavior), new EmojiPlayListener() {
                @Override
                public void onAnimationStart(RobotAnimator animator) { }
                @Override
                public void onAnimationEnd(RobotAnimator animator) { }
                @Override
                public void onAnimationCancel(RobotAnimator animator) { }
            });
            return true;
        } catch (EmojiException e) {
            Log.e("EmojiModule", Objects.requireNonNull(e.getMessage()));
            return false;
        }
    }

}

class BaseControlManager implements BaseControlHandler {
    private static final String TAG = "BaseControlManager";

    private Base mBase;
    private boolean mIsBindSuccess = false;

    public BaseControlManager(Context context) {
        Log.d(TAG, "BaseControlHandler() called");
        mBase = Base.getInstance();
        mBase.bindService(context.getApplicationContext(), mBindStateListener);
    }

    private ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBind() called");
            mIsBindSuccess = true;
        }

        @Override
        public void onUnbind(String reason) {
            Log.d(TAG, "onUnbind() called with: reason = [" + reason + "]");
            mIsBindSuccess = false;
        }
    };

    @Override
    public void setLinearVelocity(float velocity) {
        if (mIsBindSuccess) {
            mBase.setLinearVelocity(velocity);
        }

    }

    @Override
    public void setAngularVelocity(float velocity) {
        if (mIsBindSuccess) {
            mBase.setAngularVelocity(velocity);
        }
    }

    @Override
    public void stop() {
        if (mIsBindSuccess) {
            mBase.stop();
        }
    }

    @Override
    public Ticks getTicks() {
        return null;
    }
}

class HeadControlManager implements HeadControlHandler {
    private static final String TAG = "HeadControlManager";
    private Head mHead;
    private boolean mIsBindSuccess = false;

    public HeadControlManager(Context context) {
        Log.d(TAG, "HeadControlHandler() called");
        mHead = Head.getInstance();
        mHead.bindService(context.getApplicationContext(), mBindStateListener);
    }

    private ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBind() called");
            mIsBindSuccess = true;
            setMode(HeadControlHandler.MODE_EMOJI);
        }

        @Override
        public void onUnbind(String reason) {
            Log.d(TAG, "onUnbind() called with: reason = [" + reason + "]");
            mIsBindSuccess = false;
        }
    };

    @Override
    public int getMode() {
        if (mIsBindSuccess) {
            return mHead.getMode();
        }
        return 0;
    }

    @Override
    public void setMode(int mode) {
        if (mIsBindSuccess) {
            mHead.setMode(mode);
        }
    }

    @Override
    public void setWorldPitch(float angle) {
        if (mIsBindSuccess) {
            mHead.setWorldPitch(angle);
        }
    }

    @Override
    public void setWorldYaw(float angle) {
        if (mIsBindSuccess) {
            mHead.setWorldYaw(angle);
        }
    }

    @Override
    public float getWorldPitch() {
        if (mIsBindSuccess) {
            return mHead.getWorldPitch().getAngle();
        }
        return 0;
    }

    @Override
    public float getWorldYaw() {
        if (mIsBindSuccess) {
            return mHead.getWorldYaw().getAngle();
        }
        return 0;
    }
}