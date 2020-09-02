package com.kha.loomoconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.kha.loomoconnection.loomocontrol.HeadModule;
import com.kha.loomoconnection.loomocontrol.LocomotionModule;
import com.kha.loomoconnection.loomocontrol.SensorModule;
import com.kha.loomoconnection.loomocontrol.VisionModule;
import com.kha.loomoconnection.loomocontrol.VoiceModule;
import com.kha.loomoconnection.restserver.ServerManager;
import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.kha.loomoconnection.socketserver.SocketServer;

public class MainActivity extends AppCompatActivity {

    public boolean firstInitialize = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContextBinder.bindContext(this);
        VoiceModule.getInstance().moduleInit();
        HeadModule.getInstance().moduleInit();
        VisionModule.getInstance().moduleInit();
        LocomotionModule.getInstance().moduleInit();
        SensorModule.getInstance().moduleInit();
        ServerManager.getInstance().bindActivity(this);
        ServerManager.getInstance().startServer();
        SocketServer.getInstance().bindActivity(this);
        SocketServer.getInstance().startServer();
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
        if (!firstInitialize) {
            ContextBinder.bindContext(this);
            VoiceModule.getInstance().rebindServices();
            HeadModule.getInstance().rebindServices();
            VisionModule.getInstance().rebindServices();
            LocomotionModule.getInstance().rebindServices();
            SensorModule.getInstance().rebindServices();
            ServerManager.getInstance().refreshText();
            HeadModule.emojiMode = false;
        } else {
            firstInitialize = false;
        }
    }

    public void switchToEmoji() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, EmojiActivity.class);
        startActivity(intent);
    }
}
