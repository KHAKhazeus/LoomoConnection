package com.kha.loomoconnection.restserver.controller;

import android.content.Context;

import com.kha.loomoconnection.loomocontrol.VoiceModule;

public class ContextBinder {
    static public Context context;
    static public void bindContext (Context bindContext) {
        context = bindContext;
    }
    static public void refreshModuleBind() {
        VoiceModule.getInstance().rebindServices();
    }
}
