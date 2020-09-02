package com.kha.loomoconnection.restserver.model.data;

import lombok.Data;

@Data
public class RawSensorData {
    public float infraredDistanceL;
    public float infraredDistanceR;
    public float ultrasonicDistance;
    public float headWorldPitch;
    public float headWorldRoll;
    public float headWorldYaw;
    public float jointPitch;
    public float jointYaw;
    public float jointRoll;
    public int wheelTicksL;
    public int wheelTicksR;
    public float basePitch;
    public float baseRoll;
    public float baseYaw;
    public float x;
    public float y;
    public float theta;
    public float linearVelocity;
    public float angularVelocity;
    public float wheelSpeedL;
    public float wheelSpeedR;
}
