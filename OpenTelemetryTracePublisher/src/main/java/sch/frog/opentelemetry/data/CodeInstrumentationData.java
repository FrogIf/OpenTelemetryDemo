package sch.frog.opentelemetry.data;

import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.trace.v1.Span;
import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.trace.ApplicationTrace;

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
    protected void buildComplete(ApplicationTrace applicationTrace, String lastSpanId, TraceDataBuilder builder) {
        if(children != null){
            for (InstrumentationData instrumentationData : children) {
                instrumentationData.build(applicationTrace, lastSpanId, builder);
            }
        }
    }

    @Override
    protected InstrumentationLibrary getInstrumentationLibrary() {
        return InstrumentationLibrary.newBuilder().setName("").setVersion("").build();
    }

    @Override
    protected String mainSpanName() {
        return this.className + "." + this.method;
    }

    @Override
    protected Span.SpanKind mainSpanKind(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder) {
        String callerSpanId = builder.getCallerSpanId(applicationTrace);
        return parentSpanId != null && parentSpanId.equals(callerSpanId) ? Span.SpanKind.SPAN_KIND_SERVER : Span.SpanKind.SPAN_KIND_INTERNAL;
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
