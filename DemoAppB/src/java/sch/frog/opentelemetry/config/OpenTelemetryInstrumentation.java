package sch.frog.opentelemetry.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;

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

    @Bean
    public WebMvcConfigurer openTelemetryMvcConfigurer(){
        return new WebMvcConfigurer(){
            @Override
            public void addInterceptors(InterceptorRegistry registry){
                registry.addInterceptor(new OpenTelemetryHandlerInterceptor());
            }
        };
    }

    private static class WebInputTraceInfo{
        private Span span;
        private Scope scope;
    }


    private class OpenTelemetryHandlerInterceptor implements HandlerInterceptor {

        private final ThreadLocal<WebInputTraceInfo> traceInfoHolder = new ThreadLocal<>();

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            traceInfoHolder.remove();
            Context extractedContext = OpenTelemetryInstrumentation.this.openTelemetryEnv.getOpenTelemetry().getPropagators().getTextMapPropagator()
                    .extract(Context.current(), request, new TextMapGetter<HttpServletRequest>() {
                        @Override
                        public Iterable<String> keys(HttpServletRequest request) {
                            Enumeration<String> headerNames = request.getHeaderNames();
                            HashSet<String> keys = new HashSet<>();
                            while(headerNames.hasMoreElements()){
                                keys.add(headerNames.nextElement());
                            }
                            return keys;
                        }

                        @Override
                        public String get(HttpServletRequest request, String s) {
                            return request.getHeader(s);
                        }
                    });

            WebInputTraceInfo webInputTraceInfo = new WebInputTraceInfo();
            webInputTraceInfo.span = OpenTelemetryInstrumentation.this.openTelemetryEnv.getTracer()
                    .spanBuilder(request.getMethod() + " " + request.getRequestURI()).setSpanKind(SpanKind.SERVER)
                    .setParent(extractedContext)
                    .startSpan();
            webInputTraceInfo.scope = webInputTraceInfo.span.makeCurrent();
            webInputTraceInfo.span.setAttribute(SemanticAttributes.HTTP_METHOD, request.getMethod());
            webInputTraceInfo.span.setAttribute(SemanticAttributes.HTTP_SCHEME, request.getScheme());
            webInputTraceInfo.span.setAttribute(SemanticAttributes.HTTP_CLIENT_IP, request.getRemoteHost());
            webInputTraceInfo.span.setAttribute(SemanticAttributes.HTTP_URL, request.getRequestURI());
            traceInfoHolder.set(webInputTraceInfo);

            return true;
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
            WebInputTraceInfo webInputTraceInfo = traceInfoHolder.get();
            try{
                try{
                    webInputTraceInfo.span.end();
                }finally {
                    webInputTraceInfo.scope.close();
                }
            }finally {
                traceInfoHolder.remove();
            }
        }
    }

}
