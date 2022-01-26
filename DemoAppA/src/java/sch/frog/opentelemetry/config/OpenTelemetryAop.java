package sch.frog.opentelemetry.config;

import java.net.HttpURLConnection;
import java.net.URI;
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
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
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
        
        openTelemetry =
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

    // @Around("execution(* org.springframework.web.client.RestTemplate.getForEntity(..))")
    // public Object aroundForRestTemplate(ProceedingJoinPoint pjp) throws Throwable{
    //     Span currentSpan = null;

    //     Span parent = Span.current();
    //     Signature signature = pjp.getSignature();
    //     Object[] args = pjp.getArgs();
    //     String url = "unknown";
    //     if(args != null && args.length > 0){
    //         Object urlObj = args[0];
    //         if(urlObj instanceof URI){
    //             url = ((URI)urlObj).getPath();
    //         }else if(urlObj instanceof String){
    //             url = urlObj.toString();
    //         }
    //     }

    //     TextMapSetter<HttpURLConnection> setter = new TextMapSetter<HttpURLConnection>() {
    //         @Override
    //         public void set(HttpURLConnection carrier, String key, String value) {
    //             carrier.setRequestProperty(key, value);
    //         }
    //     };

    //     currentSpan = tracer.spanBuilder(url).setSpanKind(SpanKind.CLIENT).setParent(Context.current().with(parent)).startSpan();

    //     openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), arg1, arg2);
    //     currentSpan.setAttribute("class", signature.getDeclaringTypeName());
    //     currentSpan.setAttribute("method", signature.getName());

    //     try(
    //         Scope scope = currentSpan.makeCurrent()
    //     ){
    //         return pjp.proceed();
    //     }finally{
    //         currentSpan.end();
    //     }
    // }

    
}
