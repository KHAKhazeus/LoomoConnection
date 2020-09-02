package com.kha.loomoconnection.restserver.model.requests;

import com.kha.loomoconnection.restserver.model.responses.BaseRsp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetBaseInfoRequest extends BaseRsp {
    public boolean trigger;
}
