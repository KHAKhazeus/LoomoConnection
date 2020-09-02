package com.kha.loomoconnection.restserver.model.requests;

import com.kha.loomoconnection.restserver.model.data.Point2D;
import com.kha.loomoconnection.restserver.model.responses.BaseRsp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SetOriginRequest extends BaseRsp {
    public Point2D point2D;
}
