package com.kha.loomoconnection.restserver.model.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoiceRequest extends BaseRequest {
    public String voiceline;
}
