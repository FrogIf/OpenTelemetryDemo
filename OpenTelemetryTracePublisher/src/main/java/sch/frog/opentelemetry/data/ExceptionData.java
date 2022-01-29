package sch.frog.opentelemetry.data;

import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.ArrayList;

public class ExceptionData {

    private static final String EVENT_ATTR_KEY_EXCEPTION_MSG = "exception.message";

    private static final String EVENT_ATTR_KEY_EXCEPTION_STACK_TRACE = "exception.stacktrace";

    private static final String EVENT_ATTR_KEY_EXCEPTION_TYPE = "exception.type";

    private static final String EVENT_NAME = "exception";

    private String exceptionMsg;

    private String exceptionStackTrace;

    private String exceptionType;

    public Span.Event buildEvent(){
        ArrayList<KeyValue> kvList = new ArrayList<>(3);
        CollectionUtil.addIfNotNull(kvList, OpenTelemetryProtoUtil.build(EVENT_ATTR_KEY_EXCEPTION_MSG, exceptionMsg));
        CollectionUtil.addIfNotNull(kvList, OpenTelemetryProtoUtil.build(EVENT_ATTR_KEY_EXCEPTION_STACK_TRACE, exceptionStackTrace));
        CollectionUtil.addIfNotNull(kvList, OpenTelemetryProtoUtil.build(EVENT_ATTR_KEY_EXCEPTION_TYPE, exceptionType));
        return Span.Event.newBuilder()
                .setName(EVENT_NAME)
                .addAllAttributes(kvList)
                .build();
    }

    public static class Builder {

        public static Builder newBuilder(){
            return new Builder();
        }

        private final ExceptionData exceptionData = new ExceptionData();

        public Builder setExceptionMsg(String msg){
            exceptionData.exceptionMsg = msg;
            return this;
        }

        public Builder setExceptionStackTrace(String stackTrace){
            exceptionData.exceptionStackTrace = stackTrace;
            return this;
        }

        public Builder setExceptionType(String exceptionType){
            exceptionData.exceptionType = exceptionType;
            return this;
        }

        public ExceptionData build(){
            return exceptionData;
        }
    }


}
