package sch.frog.opentelemetry.data;

import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.Collection;

public class HttpInstrumentationData extends AbstractInstrumentationData{

    private static final String NET_PORT = "net.peer.port";

    private static final String HTTP_URL = "http.url";

    private static final String STATUS_CODE = "http.status_code";

    private static final String HTTP_METHOD = "http.method";

    private String httpUrl;

    private int port;

    private String method;

    private int statusCode;

    private ApplicationTrace next;

    protected HttpInstrumentationData(long startOffset, long endOffset) {
        super(startOffset, endOffset);
    }

    @Override
    protected void buildComplete(ApplicationTrace applicationTrace, String lastSpanId, TraceDataBuilder builder) {
        if(this.next != null){
            builder.build(this.next, lastSpanId);
        }
    }

    @Override
    protected InstrumentationLibrary getInstrumentationLibrary() {
        return InstrumentationLibrary.newBuilder().setName("").setVersion("").build();
    }

    @Override
    protected String mainSpanName() {
        return this.method + " " + this.httpUrl;
    }

    @Override
    protected Span.SpanKind mainSpanKind(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder) {
        return Span.SpanKind.SPAN_KIND_CLIENT;
    }

    @Override
    public Collection<KeyValue> getAttributes() {
        Collection<KeyValue> attrs = super.getAttributes();
        CollectionUtil.addIfNotNull(attrs, OpenTelemetryProtoUtil.build(HTTP_METHOD, this.method));
        CollectionUtil.addIfNotNull(attrs, OpenTelemetryProtoUtil.build(NET_PORT, this.port));
        CollectionUtil.addIfNotNull(attrs, OpenTelemetryProtoUtil.build(STATUS_CODE, this.statusCode));
        CollectionUtil.addIfNotNull(attrs, OpenTelemetryProtoUtil.build(HTTP_URL, this.httpUrl));
        return attrs;
    }

    public static class Builder extends DataBuilder<Builder, HttpInstrumentationData>{

        private String httpUrl;

        private int port;

        private String method;

        private int statusCode;

        private ApplicationTrace next;

        public static Builder newBuilder(){
            return new Builder();
        }

        public Builder setHttpUrl(String httpUrl) {
            this.httpUrl = httpUrl;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder call(ApplicationTrace next) {
            this.next = next;
            return this;
        }

        @Override
        protected HttpInstrumentationData buildInfo() {
            HttpInstrumentationData httpInstrumentationData = new HttpInstrumentationData(this.startOffset, this.endOffset);
            httpInstrumentationData.httpUrl = httpUrl;
            httpInstrumentationData.method = method;
            httpInstrumentationData.port = port;
            httpInstrumentationData.statusCode = statusCode;
            httpInstrumentationData.next = next;
            return httpInstrumentationData;
        }
    }


}
