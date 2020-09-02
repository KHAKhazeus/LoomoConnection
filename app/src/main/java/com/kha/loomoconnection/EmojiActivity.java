package com.kha.loomoconnection;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.kha.loomoconnection.loomocontrol.HeadModule;
import com.kha.loomoconnection.loomocontrol.LocomotionModule;
import com.kha.loomoconnection.loomocontrol.SensorModule;
import com.kha.loomoconnection.loomocontrol.VisionModule;
import com.kha.loomoconnection.loomocontrol.VoiceModule;
import com.kha.loomoconnection.restserver.ServerManager;
import com.kha.loomoconnection.restserver.controller.ContextBinder;

public class EmojiActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emoji);
        ContextBinder.bindContext(this);
        VoiceModule.getInstance().rebindServices();
        HeadModule.getInstance().rebindServices();
        VisionModule.getInstance().rebindServices();
        LocomotionModule.getInstance().rebindServices();
        SensorModule.getInstance().rebindServices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServerManager.getInstance().stopServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VisionModule.getInstance().unbindServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextBinder.bindContext(this);
        VoiceModule.getInstance().rebindServices();
        HeadModule.getInstance().rebindServices();
        VisionModule.getInstance().rebindServices();
        LocomotionModule.getInstance().rebindServices();
        SensorModule.getInstance().rebindServices();
        HeadModule.emojiMode = true;
        HeadModule.getInstance().rebindEmoji();
    }

    public void switchToMain() {
        Intent intent = new Intent();
        intent.setClass(EmojiActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
