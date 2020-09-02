package com.kha.loomoconnection.utils;

public class AngleUtils {
    public static float PiToNinety(float piForm) {
        return (float) (piForm / Math.PI * 180);
    }

    public static float NinetyToPi(float ninetyForm) {
        return (float) (ninetyForm * Math.PI / 180);
    }
}
