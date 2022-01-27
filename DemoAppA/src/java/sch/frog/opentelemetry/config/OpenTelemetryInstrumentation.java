package sch.frog.opentelemetry.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

@Configuration
public class OpenTelemetryInstrumentation implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryInstrumentation.class);

    @Autowired
    private OpenTelemetryEnv openTelemetryEnv;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof RestTemplateBuilder){
            RestTemplateBuilder restTemplateBuilder = (RestTemplateBuilder) bean;
            return restTemplateBuilder.additionalInterceptors(new ClientHttpRequestInterceptor() {
                @Override
                public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                    URI uri = request.getURI();
                    HttpMethod method = request.getMethod();

                    Span parent = Span.current();
                    Span currentSpan = openTelemetryEnv.getTracer().spanBuilder(uri.toString()).setSpanKind(SpanKind.CLIENT).setParent(Context.current().with(parent)).startSpan();

                    try(
                            Scope scope = currentSpan.makeCurrent()
                    ){
                        try{
                            currentSpan.setAttribute("path", uri.getPath());
                            currentSpan.setAttribute("method", method == null ? "unknown" : method.name());
                            openTelemetryEnv.getOpenTelemetry().getPropagators().getTextMapPropagator().inject(Context.current(), request.getHeaders(), new TextMapSetter<>() {
                                @Override
                                public void set(HttpHeaders carrier, String key, String value) {
                                    carrier.add(key, value);
                                }
                            });
                        }catch (Exception e){
                            logger.warn("context inject failed.", e);
                        }
                        return execution.execute(request, body);
                    }finally{
                        currentSpan.end();
                    }
                }
            });
        }
        return bean;
    }

}
