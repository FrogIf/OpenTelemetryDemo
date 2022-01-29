package sch.frog.opentelemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sch.frog.opentelemetry.app.ThreadInfo;
import sch.frog.opentelemetry.build.TraceDataBuilder;
import sch.frog.opentelemetry.config.GlobalConfiguration;
import sch.frog.opentelemetry.data.CodeInstrumentationData;
import sch.frog.opentelemetry.data.DatabaseInstrumentationData;
import sch.frog.opentelemetry.data.HttpInstrumentationData;
import sch.frog.opentelemetry.data.InstrumentationData;
import sch.frog.opentelemetry.service.TracePublisher;
import sch.frog.opentelemetry.trace.ApplicationTrace;
import sch.frog.opentelemetry.util.NanoDuration;

import java.io.IOException;
import java.util.Random;

public class OpenTelemetryTracePublisher {

    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryTracePublisher.class);

    public static void main(String[] args) throws IOException {
        String confFile = System.getProperty("confFile", "publisher.properties");
        GlobalConfiguration.init(confFile);
        TracePublisher tracePublisher = new TracePublisher(GlobalConfiguration.getProperty("trace.publish.host", "localhost"),
                Integer.parseInt(GlobalConfiguration.getProperty("trace.publish.port", "19890")));

        ApplicationTrace applicationTrace = traceA();
        TraceDataBuilder builder = new TraceDataBuilder();
        builder.build(applicationTrace, null);
        tracePublisher.publish(builder.assembleSpans());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tracePublisher.destroy();
    }

    private static ApplicationTrace traceA(){
        Random r = new Random();

        ThreadInfo threadInfo = ThreadInfo.Builder.newBuilder().setThreadId(r.nextInt()).setThreadName("test-thread-" + r.nextInt(10)).build();

        InstrumentationData instrumentationData = CodeInstrumentationData.Builder.newBuilder()
                .setThreadInfo(threadInfo)
                .setMethod("hello")
                .setClassName("sch.frog.test.Controller")
                .setStartOffset(NanoDuration.ofMillis(0))
                .setEndOffset(NanoDuration.ofMillis(100))
                .call(CodeInstrumentationData.Builder.newBuilder()
                        .setThreadInfo(threadInfo)
                        .setClassName("sch.frog.test.Service")
                        .setMethod("queryOne")
                        .setStartOffset(NanoDuration.ofMillis(20))
                        .setEndOffset(NanoDuration.ofMillis(50))
                        .call(
                                HttpInstrumentationData.Builder.newBuilder()
                                        .setHttpUrl("http://localhost:8080/hello")
                                        .setMethod("GET")
                                        .setPort(8080)
                                        .setStatusCode(200)
                                        .setThreadInfo(threadInfo)
                                        .setStartOffset(NanoDuration.ofMicros(23000))
                                        .setEndOffset(NanoDuration.ofMicros(44000))
                                        .call(traceB())
                                        .build()
                        )
                        .build())
                .call(CodeInstrumentationData.Builder.newBuilder()
                        .setThreadInfo(threadInfo)
                        .setMethod("save")
                        .setClassName("sch.frog.test.Service")
                        .setStartOffset(NanoDuration.ofMillis(60))
                        .setEndOffset(NanoDuration.ofMillis(90))
                        .call(DatabaseInstrumentationData.Builder.newBuilder()
                                .setSql("select * from test_table where a = 12")
                                .setConnectionUrl("jdbc:mysql://localhost:3306/frog")
                                .setOperation("select")
                                .setDbSystem("mysql")
                                .setTable("test_table")
                                .setStartOffset(NanoDuration.ofMicros(60500))
                                .setEndOffset(NanoDuration.ofMicros(70600))
                                .setThreadInfo(threadInfo)
                                .build())
                        .build())
                .build();

        return ApplicationTrace.Builder.newBuilder()
                .setApplication(AppHolder.frogAppA).setSchemaUrl("")
                .setInstrumentationData(instrumentationData)
                .build();
    }

    private static ApplicationTrace traceB(){
        Random r = new Random();

        ThreadInfo threadInfo = ThreadInfo.Builder.newBuilder().setThreadId(r.nextInt()).setThreadName("test-b-thread-" + r.nextInt(10)).build();

        InstrumentationData instrumentationData = CodeInstrumentationData.Builder.newBuilder()
                .setThreadInfo(threadInfo)
                .setMethod("hello")
                .setClassName("sch.frog.ppp.Controller")
                .setStartOffset(NanoDuration.ofMillis(0))
                .setEndOffset(NanoDuration.ofMillis(10))
                .build();

        return ApplicationTrace.Builder.newBuilder()
                .setApplication(AppHolder.frogAppB).setSchemaUrl("")
                .setInstrumentationData(instrumentationData)
                .build();
    }

}
