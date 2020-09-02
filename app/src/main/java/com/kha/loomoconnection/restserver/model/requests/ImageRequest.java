package com.kha.loomoconnection.restserver.model.requests;

import com.segway.robot.sdk.locomotion.sbv.Base;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ImageRequest extends BaseRequest {
    public String imageType;
}
