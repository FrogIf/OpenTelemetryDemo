package sch.frog.opentelemetry.data;

import io.opentelemetry.proto.common.v1.KeyValue;
import sch.frog.opentelemetry.app.ThreadInfo;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.CollectionUtil;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractInstrumentationData implements InstrumentationData {

    private static final String THREAD_NAME = "thread.name";

    private static final String THREAD_ID = "thread.id";

    private String threadName;

    private Integer threadId;

    private final long startOffset;

    private final long endOffset;

    protected final ArrayList<KeyValue> otherAttributes = new ArrayList<>();

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

    public static abstract class DataBuilder<T extends DataBuilder<T, D>, D extends AbstractInstrumentationData> {

        private final T self;

        public DataBuilder() {
            self = (T) this;
        }

        protected ApplicationTrace applicationTrace;

        private ThreadInfo threadInfo;

        protected long startOffset;

        protected long endOffset;

        private final ArrayList<KeyValue> otherAttributes = new ArrayList<>();

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

        protected void baseInfoSet(AbstractInstrumentationData abstractInstrumentationData){
            abstractInstrumentationData.otherAttributes.addAll(this.otherAttributes);
            abstractInstrumentationData.setThreadInfo(threadInfo);
        }

        public D build(){
            D d = this.buildInfo();
            this.baseInfoSet(d);
            return d;
        }

        protected abstract D buildInfo();

    }
}
