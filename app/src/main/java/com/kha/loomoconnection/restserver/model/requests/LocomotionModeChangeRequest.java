package com.kha.loomoconnection.restserver.model.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LocomotionModeChangeRequest extends BaseRequest {
    public String mode;
}
