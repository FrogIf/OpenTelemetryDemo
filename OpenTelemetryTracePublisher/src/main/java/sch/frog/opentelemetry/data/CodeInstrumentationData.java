package sch.frog.opentelemetry.data;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.trace.v1.Span;
import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.ArrayList;
import java.util.List;

public class CodeInstrumentationData extends AbstractInstrumentationData {

    private String className;

    private String method;

    private List<InstrumentationData> children;

    protected CodeInstrumentationData(long startOffset, long endOffset) {
        super(startOffset, endOffset);
    }

    @Override
    public void build(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder) {
        String spanId = OpenTelemetryProtoUtil.genSpanId();
        long startNanoTime = builder.getStartNanoTime(applicationTrace);
        String callerSpanId = builder.getCallerSpanId(applicationTrace);
        Span span = Span.newBuilder()
                .setSpanId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(spanId)))
                .setKind(parentSpanId != null && parentSpanId.equals(callerSpanId) ? Span.SpanKind.SPAN_KIND_SERVER : Span.SpanKind.SPAN_KIND_INTERNAL)
                .setTraceId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(builder.getTraceId())))
                .setName(this.className + "." + this.method)
                .addAllAttributes(super.getAttributes())
                .setStartTimeUnixNano(startNanoTime + this.getStartOffset())
                .setEndTimeUnixNano(startNanoTime + this.getEndOffset())
                .setParentSpanId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(parentSpanId)))
                .build();

        builder.build(applicationTrace, List.of(span), InstrumentationLibrary.newBuilder().setName("").setVersion("").build(), "");

        if(children != null){
            for (InstrumentationData instrumentationData : children) {
                instrumentationData.build(applicationTrace, spanId, builder);
            }
        }
    }

    public static class Builder extends DataBuilder<Builder, CodeInstrumentationData> {

        private String className;

        private String method;

        private long duration;

        private final ArrayList<InstrumentationData> children = new ArrayList<>();

        public static Builder newBuilder(){
            return new Builder();
        }

        public Builder setClassName(String className){
            this.className = className;
            return this;
        }

        public Builder setMethod(String method){
            this.method = method;
            return this;
        }

        public Builder call(InstrumentationData instrumentationData){
            children.add(instrumentationData);
            return this;
        }

        @Override
        protected CodeInstrumentationData buildInfo() {
            CodeInstrumentationData data = new CodeInstrumentationData(this.startOffset, this.endOffset);
            data.className = this.className;
            data.method = this.method;
            data.children = this.children;
            return data;
        }
    }
}
