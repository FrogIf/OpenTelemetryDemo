package sch.frog.opentelemetry.util;

public class NanoDuration {

    public static long ofNano(long duration){
        return duration;
    }

    public static long ofMicros(long duration){
        return duration * 1000;
    }

    public static long ofMillis(long duration){
        return duration * 1000 * 1000;
    }

    public static long ofSeconds(long duration){
        return duration * 1000 * 1000 * 1000;
    }
}
