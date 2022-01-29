package sch.frog.opentelemetry.data;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import sch.frog.opentelemetry.app.ThreadInfo;
import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractInstrumentationData implements InstrumentationData {

    private static final String THREAD_NAME = "thread.name";

    private static final String THREAD_ID = "thread.id";

    private String threadName;

    private Integer threadId;

    private final long startOffset;

    private final long endOffset;

    protected final ArrayList<KeyValue> otherAttributes = new ArrayList<>();

    private ArrayList<Span.Event> events;

    private ArrayList<Span.Link> links;

    private Status status;

    private ExceptionData exceptionData;

    private String schemaUrl;

    protected AbstractInstrumentationData(long startOffset, long endOffset){
        if(endOffset < 0 || startOffset > endOffset){
            throw new IllegalArgumentException("illegal offset. startOffset : " + startOffset + ", endOffset : " + endOffset);
        }
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Collection<KeyValue> getAttributes(){
        ArrayList<KeyValue> kvList = new ArrayList<>(2);
        CollectionUtil.addIfNotNull(kvList, OpenTelemetryProtoUtil.build(THREAD_ID, threadId));
        CollectionUtil.addIfNotNull(kvList, OpenTelemetryProtoUtil.build(THREAD_NAME, threadName));
        kvList.addAll(otherAttributes);
        return kvList;
    }

    public Collection<Span.Event> getEvents(){
        return this.events;
    }

    public Collection<Span.Link> getLinks(){
        return this.links;
    }

    public void setThreadInfo(ThreadInfo threadInfo){
        this.threadId = threadInfo.getThreadId();
        this.threadName = threadInfo.getThreadName();
    }

    public String getThreadName() {
        return threadName;
    }

    public Integer getThreadId() {
        return threadId;
    }

    @Override
    public long getStartOffset() {
        return this.startOffset;
    }

    @Override
    public long getEndOffset() {
        return this.endOffset;
    }

    @Override
    public void addAttribute(String key, String value) {
        CollectionUtil.addIfNotNull(otherAttributes, OpenTelemetryProtoUtil.build(key, value));
    }

    public void setExceptionData(ExceptionData exceptionData){
        if(exceptionData != null){
            this.status = Status.newBuilder().setCode(Status.StatusCode.STATUS_CODE_ERROR).setMessage("exception").build();
            if(this.events == null){
                this.events = new ArrayList<>(1);
            }
            this.events.add(exceptionData.buildEvent());
        }
    }

    /**
     * 主span构建前执行
     */
    protected List<Span> buildPreMainSpan(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder){
        // do nothing
        return null;
    }

    /**
     * 主span构建中执行
     */
    protected void onMainSpanBuilder(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder, Span.Builder mainSpanBuilder){
        // do nothing
    }

    /**
     * 主span构建后执行
     */
    protected List<Span> buildAfterMainSpan(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder, Span mainSpan){
        // do nothing
        return null;
    }

    /**
     * 构建完成后执行
     * @param lastSpanId 此次构建所生成的最后一个span的spanId
     */
    protected void buildComplete(ApplicationTrace applicationTrace, String lastSpanId, TraceDataBuilder builder){
        // do nothing
    }

    protected abstract InstrumentationLibrary getInstrumentationLibrary();

    @Override
    public void build(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder){
        List<Span> preSpans = this.buildPreMainSpan(applicationTrace, parentSpanId, builder);
        Span.Builder spanBuilder = buildMainSpan(applicationTrace, parentSpanId, builder);
        this.onMainSpanBuilder(applicationTrace, parentSpanId, builder, spanBuilder);
        Span mainSpan = spanBuilder.build();
        List<Span> postSpans = this.buildAfterMainSpan(applicationTrace, parentSpanId, builder, mainSpan);

        ArrayList<Span> spans = new ArrayList<>();
        String postSpanId = OpenTelemetryProtoUtil.bytesToHexId(mainSpan.getSpanId().toByteArray());
        if(!CollectionUtil.isEmpty(preSpans)){
            spans.addAll(preSpans);
        }
        spans.add(mainSpan);
        if(!CollectionUtil.isEmpty(postSpans)){
            spans.addAll(postSpans);
            postSpanId = OpenTelemetryProtoUtil.bytesToHexId(postSpans.get(spans.size() - 1).getSpanId().toByteArray());
        }
        builder.build(applicationTrace, spans, this.getInstrumentationLibrary(), this.schemaUrl);

        this.buildComplete(applicationTrace, postSpanId, builder);
    }

    protected Span.Builder buildMainSpan(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder){
        String spanId = OpenTelemetryProtoUtil.genSpanId();
        long startNanoTime = builder.getStartNanoTime(applicationTrace);
        Span.Builder spanBuilder = Span.newBuilder()
                .setSpanId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(spanId)))
                .setKind(this.mainSpanKind(applicationTrace, parentSpanId, builder))
                .setTraceId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(builder.getTraceId())))
                .setName(this.mainSpanName())
                .addAllAttributes(this.getAttributes())
                .setStartTimeUnixNano(startNanoTime + this.getStartOffset())
                .setEndTimeUnixNano(startNanoTime + this.getEndOffset())
                .setParentSpanId(ByteString.copyFrom(OpenTelemetryProtoUtil.hexIdToBytes(parentSpanId)));
        if(!CollectionUtil.isEmpty(this.events)){
            spanBuilder.addAllEvents(events);
        }
        if(!CollectionUtil.isEmpty(this.links)){
            spanBuilder.addAllLinks(this.links);
        }
        if(this.status != null){
            spanBuilder.setStatus(this.status);
        }

        return spanBuilder;
    }

    protected abstract String mainSpanName();

    protected abstract Span.SpanKind mainSpanKind(ApplicationTrace applicationTrace, String parentSpanId, TraceDataBuilder builder);

    public static abstract class DataBuilder<T extends DataBuilder<T, D>, D extends AbstractInstrumentationData> {

        private final T self;

        public DataBuilder() {
            self = (T) this;
        }

        protected ApplicationTrace applicationTrace;

        private ThreadInfo threadInfo;

        protected long startOffset;

        protected long endOffset;

        protected String schemaUrl;

        private Status status;

        private ExceptionData exceptionData;

        private final ArrayList<KeyValue> otherAttributes = new ArrayList<>();

        private final ArrayList<Span.Event> events = new ArrayList<>();

        private final ArrayList<Span.Link> links = new ArrayList<>();

        public T setThreadInfo(ThreadInfo threadInfo){
            this.threadInfo = threadInfo;
            return self;
        }

        public T setStartOffset(long startOffset) {
            this.startOffset = startOffset;
            return self;
        }

        public T setEndOffset(long endOffset) {
            this.endOffset = endOffset;
            return self;
        }

        public T addAttribute(String key, String value){
            CollectionUtil.addIfNotNull(otherAttributes, OpenTelemetryProtoUtil.build(key, value));
            return self;
        }

        public T addEvent(Span.Event event){
            events.add(event);
            return self;
        }

        public T addLinks(Span.Link link){
            links.add(link);
            return self;
        }

        public T setStatus(Status status){
            this.status = status;
            return self;
        }

        public T setExceptionData(ExceptionData exceptionData){
            this.exceptionData = exceptionData;
            return self;
        }

        public T setSchemaUrl(String schemaUrl){
            this.schemaUrl = schemaUrl;
            return self;
        }

        protected void baseInfoSet(AbstractInstrumentationData abstractInstrumentationData){
            abstractInstrumentationData.otherAttributes.addAll(this.otherAttributes);
            abstractInstrumentationData.setThreadInfo(threadInfo);
            abstractInstrumentationData.events = this.events;
            abstractInstrumentationData.links = this.links;
            abstractInstrumentationData.status = this.status;
            abstractInstrumentationData.setExceptionData(exceptionData);
            abstractInstrumentationData.schemaUrl = this.schemaUrl;
        }

        public D build(){
            D d = this.buildInfo();
            this.baseInfoSet(d);
            return d;
        }

        protected abstract D buildInfo();

    }
}
