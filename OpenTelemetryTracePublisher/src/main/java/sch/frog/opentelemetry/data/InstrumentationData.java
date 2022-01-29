package sch.frog.opentelemetry.data;

import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.trace.ApplicationTrace;

public interface InstrumentationData {

    void build(ApplicationTrace belongTrace, String parentSpanId, TraceDataBuilder builder);

    long getStartOffset();

    long getEndOffset();

    void addAttribute(String key, String value);
}
