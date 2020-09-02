package com.kha.loomoconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.kha.loomoconnection.loomocontrol.HeadModule;

import java.util.Objects;

public class LoomoBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LoomoBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case "com.segway.robot.action.PITCH_LOCK":
                //锁定pitch功能
                HeadModule.isPitchEnabled = false;
                break;
            case "com.segway.robot.action.PITCH_UNLOCK":
                HeadModule.isPitchEnabled = true;
                break;
            case "com.segway.robot.action.YAW_LOCK":
                //锁定pitch功能
                HeadModule.isYawEnabled = false;
                break;
            case "com.segway.robot.action.YAW_UNLOCK":
                HeadModule.isYawEnabled = true;
                break;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.d(TAG, log);
    }
}
