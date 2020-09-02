package com.kha.loomoconnection.restserver.model.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HeadMovementRequest extends BaseRequest{
    public boolean changeMode;
    public String headMode;
    public float worldPitch;
    public float worldYaw;
    public float yawAV;
    public float pitchAV;
    public long yawMovDuration;
    public long pitchMovDuration;
}
