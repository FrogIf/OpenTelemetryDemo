package sch.frog.opentelemetry.config;

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "frog.opentelemetry.enable", havingValue = "true", matchIfMissing = true)
public class OpenTelemetryEnv implements IOpenTelemetryOperator{

    @Value("${spring.application.name}")
    private String appName;

    @Value("${jaeger.collector.address}")
    private String jaegerCollectorAddress;

    private Tracer tracer;

    private OpenTelemetrySdk openTelemetry;

    @PostConstruct
    public void init(){
        JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
                .setEndpoint(jaegerCollectorAddress)
                .setTimeout(30, TimeUnit.SECONDS)
                .build();

        Resource serviceNameResource =
                Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, appName));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();

        openTelemetry = OpenTelemetrySdk.builder().setPropagators(
                        ContextPropagators.create(TextMapPropagator.composite(
                            W3CTraceContextPropagator.getInstance(),
                            W3CBaggagePropagator.getInstance())))
                        .setTracerProvider(tracerProvider).build();

        tracer = openTelemetry.getTracer("sch.frog.opentelemetry");

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
    }


    public Tracer getTracer() {
        return tracer;
    }

    public OpenTelemetrySdk getOpenTelemetry() {
        return openTelemetry;
    }

    @Override
    public void event(String name, Map<String, String> attributes){
        Span current = Span.current();
        AttributesBuilder builder = Attributes.builder();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        current.addEvent(name, builder.build());
    }
}
