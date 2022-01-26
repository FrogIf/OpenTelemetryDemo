package sch.frog.opentelemetry.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

@Component
@Aspect
public class OpenTelemetryAop {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${jaeger.collector.address}")
    private String jaegerCollectorAddress;

    private Tracer tracer;

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
        
        OpenTelemetrySdk openTelemetry =
            OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        
        tracer = openTelemetry.getTracer("sch.frog.opentelemetry");

        Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));
    }

    @Around("within(sch.frog.opentelemetry..*) && !within(sch.frog.opentelemetry.config..*)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        Span currentSpan = null;

        Span parent = Span.current();
        Signature signature = pjp.getSignature();
        String spanName = signature.getDeclaringTypeName() + "." + signature.getName();
        currentSpan = tracer.spanBuilder(spanName).setParent(Context.current().with(parent)).startSpan();

        currentSpan.setAttribute("class", signature.getDeclaringTypeName());
        currentSpan.setAttribute("method", signature.getName());

        try(
            Scope scope = currentSpan.makeCurrent()
        ){
            return pjp.proceed();
        }finally{
            currentSpan.end();
        }
    }

    
}
