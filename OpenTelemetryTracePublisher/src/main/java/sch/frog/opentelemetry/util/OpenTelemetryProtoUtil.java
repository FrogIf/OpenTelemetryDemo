package sch.frog.opentelemetry.util;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;

import java.util.Random;

public class OpenTelemetryProtoUtil {

    public static KeyValue build(String key, String value){
        if(key != null && value != null){
            return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(value).build()).build();
        }else {
            return null;
        }
    }

    public static KeyValue build(String key, Integer value){
        if(key != null && value != null){
            return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setIntValue(value).build()).build();
        }else{
            return null;
        }
    }

    public static KeyValue build(String key, AnyValue value){
        if(key != null && value != null){
            return KeyValue.newBuilder().setKey(key).setValue(value).build();
        }else{
            return null;
        }
    }

    public static void setParentSpanId(Span.Builder builder, String parentSpanId){
        if(!StringUtil.isBlank(parentSpanId)){
            builder.setParentSpanId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(parentSpanId)));
        }else{
            builder.setParentSpanId(ByteString.copyFrom(new byte[0]));
        }
    }

    public static String genSpanId(){
        return genHexId(16);
    }

    public static String genTraceId(){
        return genHexId(32);
    }

    public static String genHexId(int len){
        int byteLen = len / 2;
        Random r = new Random();
        byte[] bytes = new byte[byteLen];
        for(int i = 0; i < byteLen; i++){
            int n = r.nextInt(256);
            if(i == 0 && n == 0){
                n = n + 1;
            }
            bytes[i] = (byte) n;
        }

        return bytesToHexId(bytes);
    }

    private static final byte[] EMPTY_BYTES = new byte[0];

    public static byte[] hexIdToBytes(String hexId){
        if(StringUtil.isBlank(hexId)) { return EMPTY_BYTES; }
        char[] chars = hexId.toCharArray();
        byte[] bytes = new byte[chars.length / 2];
        for(int i = 0; i < chars.length; i += 2){
            byte b1 = Byte.parseByte(chars[i] + "", 16);
            byte b2 = Byte.parseByte(chars[i + 1] + "", 16);
            bytes[i / 2] = (byte) ((b1 << 4) | b2);
        }
        return bytes;
    }

    public static String bytesToHexId(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString(b >> 4 & 0xf, 16));
            sb.append(Integer.toString(b & 0xf, 16));
        }
        return sb.toString();
    }
}
