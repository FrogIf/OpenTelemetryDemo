package sch.frog.opentelemetry.build;

import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.OpenTelemetryProtoUtil;
import sch.frog.opentelemetry.util.StringUtil;

import java.util.*;

public class TraceDataBuilder {

    private final String traceId = OpenTelemetryProtoUtil.genTraceId();

    private final long firstStartNanoTime;

    // 记录每一个applicationTrace的resourcesBuilder
    private final HashMap<ApplicationTrace, ResourceSpans.Builder> resourceBuilderMap = new HashMap<>();

    // 记录每一个applicationTrace的开始时间
    private final HashMap<ApplicationTrace, Long> startNanoTimeMap = new HashMap<>();

    // 跨trace调用, 记录一个applicationTrace是由哪个parentSpan调用的: applicationTrace --> parentSpanId
    private final HashMap<ApplicationTrace, String> callMap = new HashMap<>();

    public TraceDataBuilder() {
        firstStartNanoTime = System.currentTimeMillis() * 1000 * 1000;
    }

    public TraceDataBuilder(long firstStartNanoTime) {
        this.firstStartNanoTime = firstStartNanoTime;
    }

    public String getTraceId(){
        return traceId;
    }

    public String getCallerSpanId(ApplicationTrace applicationTrace){
        return callMap.get(applicationTrace);
    }

    public long getStartNanoTime(ApplicationTrace applicationTrace){
        Long startNanoTime = startNanoTimeMap.get(applicationTrace);
        if(startNanoTime == null){
            String parentSpanId = callMap.get(applicationTrace);
            if(StringUtil.isBlank(parentSpanId)){ // 入口trace
                startNanoTimeMap.put(applicationTrace, firstStartNanoTime);
                startNanoTime = firstStartNanoTime;
            }
            Set<Map.Entry<ApplicationTrace, ResourceSpans.Builder>> entrySet = resourceBuilderMap.entrySet();
            for (Map.Entry<ApplicationTrace, ResourceSpans.Builder> entry : entrySet) {
                ResourceSpans.Builder builder = entry.getValue();
                List<InstrumentationLibrarySpans> spanBoxList = builder.getInstrumentationLibrarySpansList();
                for (InstrumentationLibrarySpans librarySpans : spanBoxList) {
                    List<Span> spansList = librarySpans.getSpansList();
                    for (Span span : spansList) {
                        String spanId = OpenTelemetryProtoUtil.bytesToHexId(span.getSpanId().toByteArray());
                        if(parentSpanId.equals(spanId)){
                            startNanoTime = span.getStartTimeUnixNano();
                        }
                    }
                }
            }
            startNanoTimeMap.put(applicationTrace, startNanoTime);
        }
        return startNanoTime;
    }

    public void build(ApplicationTrace trace, List<Span> spans, InstrumentationLibrary instrumentationLibrary, String schemaUrl){
        ResourceSpans.Builder builder = resourceBuilderMap.get(trace);
        if(builder == null){
            builder = ResourceSpans.newBuilder().setResource(trace.getApplication().buildResource());
            resourceBuilderMap.put(trace, builder);
        }

        InstrumentationLibrarySpans librarySpans = InstrumentationLibrarySpans.newBuilder()
                .addAllSpans(spans)
                .setInstrumentationLibrary(instrumentationLibrary)
                .setSchemaUrl(schemaUrl == null ? "" : schemaUrl)
                .build();

        builder.addInstrumentationLibrarySpans(librarySpans);
    }

    public List<ResourceSpans> assembleSpans(){
        Collection<ResourceSpans.Builder> values = resourceBuilderMap.values();
        ArrayList<ResourceSpans> resourceSpansArrayList = new ArrayList<>(values.size());
        for (ResourceSpans.Builder builder : values) {
            resourceSpansArrayList.add(builder.build());
        }
        return resourceSpansArrayList;
    }

    public void build(ApplicationTrace applicationTrace, String parentSpanId){
        callMap.put(applicationTrace, parentSpanId);
        applicationTrace.getInstrumentationData().build(applicationTrace, parentSpanId, this);
    }
}
