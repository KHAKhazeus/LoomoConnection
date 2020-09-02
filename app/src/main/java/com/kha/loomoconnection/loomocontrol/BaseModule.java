package com.kha.loomoconnection.loomocontrol;

import com.kha.loomoconnection.restserver.controller.ContextBinder;
import com.segway.robot.sdk.base.bind.ServiceBinder;

public interface BaseModule {
    public static Object getInstance() {
        return null;
    }
    public void rebindServices();
    public void moduleInit();
}
