package sch.frog.opentelemetry.data;

import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.ArrayList;
import java.util.Collection;

public class DatabaseInstrumentationData extends AbstractInstrumentationData{

    private static final String TABLE = "db.sql.table";

    private static final String OPERATION = "db.operation";

    private static final String CONNECTION_STRING = "db.connection_string";

    private static final String DB_SYSTEM = "db.system";

    private static final String DB_STATEMENT = "db.statement";

    private String sql;

    private String table;

    private String connectionUrl;

    private String dbSystem;

    private String operation;

    protected DatabaseInstrumentationData(long startOffset, long endOffset) {
        super(startOffset, endOffset);
    }

    @Override
    protected String mainSpanName() {
        return getName();
    }

    @Override
    protected Span.SpanKind mainSpanKind(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder) {
        return Span.SpanKind.SPAN_KIND_CLIENT;
    }

    public Collection<KeyValue> getAttributes(){
        ArrayList<KeyValue> attrs = new ArrayList<>(super.getAttributes());
        CollectionUtil.addIfNotNull(attrs,OpenTelemetryProtoUtil.build(TABLE, this.table));
        CollectionUtil.addIfNotNull(attrs,OpenTelemetryProtoUtil.build(OPERATION, this.operation));
        CollectionUtil.addIfNotNull(attrs,OpenTelemetryProtoUtil.build(CONNECTION_STRING, this.connectionUrl));
        CollectionUtil.addIfNotNull(attrs,OpenTelemetryProtoUtil.build(DB_STATEMENT, this.sql));
        CollectionUtil.addIfNotNull(attrs,OpenTelemetryProtoUtil.build(DB_SYSTEM, this.dbSystem));
        return attrs;
    }

    @Override
    protected InstrumentationLibrary getInstrumentationLibrary() {
        return InstrumentationLibrary.newBuilder().setName("").setVersion("").build();
    }

    private String getName(){
        if(this.sql == null){
            return "unknown";
        }else{
            if(this.sql.length() < 10){
                return this.sql;
            }else{
                return this.sql.substring(0, 9) + "*";
            }
        }
    }

    public static class Builder extends DataBuilder<Builder, DatabaseInstrumentationData>{

        public static Builder newBuilder(){
            return new Builder();
        }

        private String sql;

        private String table;

        private String connectionUrl;

        private String dbSystem;

        private String operation;

        public Builder setSql(String sql) {
            this.sql = sql;
            return this;
        }

        public Builder setTable(String table) {
            this.table = table;
            return this;
        }

        public Builder setConnectionUrl(String connectionUrl) {
            this.connectionUrl = connectionUrl;
            return this;
        }

        public Builder setDbSystem(String dbSystem) {
            this.dbSystem = dbSystem;
            return this;
        }

        public Builder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        @Override
        protected DatabaseInstrumentationData buildInfo() {
            DatabaseInstrumentationData instrumentation = new DatabaseInstrumentationData(this.startOffset, this.endOffset);
            instrumentation.sql = this.sql;
            instrumentation.connectionUrl = this.connectionUrl;
            instrumentation.dbSystem = this.dbSystem;
            instrumentation.operation = this.operation;
            instrumentation.table = this.table;
            return instrumentation;
        }
    }


}
