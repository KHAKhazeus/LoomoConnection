package com.kha.loomoconnection.restserver.model.data;

import lombok.Data;

@Data
public class BaseInfo {
    public String mode;
    public float angularVelocity;
    public float linearVelocity;
    public float angularVelocityLimit;
    public float linearVelocityLimit;
    public float totalMileage;
    public int power;
}
