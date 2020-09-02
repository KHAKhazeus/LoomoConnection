package com.kha.loomoconnection.restserver.model.requests;

import com.kha.loomoconnection.restserver.model.data.Point2D;
import com.kha.loomoconnection.restserver.model.data.Point2Dtheta;
import com.kha.loomoconnection.restserver.model.responses.BaseRsp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AddCPThetaRequest extends BaseRsp {
    public float x;
    public float y;
    public float theta;
}