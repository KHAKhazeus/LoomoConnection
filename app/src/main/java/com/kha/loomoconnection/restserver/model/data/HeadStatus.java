package com.kha.loomoconnection.restserver.model.data;

import lombok.Data;

@Data
public class HeadStatus {
    public boolean isPitchEnabled;
    public boolean isYawEnabled;
    public String headMode;
    public float worldPitch;
    public float worldYaw;
    public float yawAV;
    public float pitchAV;
}
